package io.github.bobocodebreskul.context.registry;

import static java.lang.reflect.Modifier.isAbstract;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.InstanceCreationException;
import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;
import io.github.bobocodebreskul.context.scan.RecursiveClassPathAnnotatedBeanScanner;
import io.github.bobocodebreskul.context.scan.utils.ScanUtilsImpl;
import io.github.bobocodebreskul.server.TomcatServer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the {@link ObjectFactory} as Bring beans container. Creates and holds all found
 * and registered beans.
 *
 * @author Ruslan Hladchenko
 * @author Roman Pryshchepa
 * @author Mykola Filimonov
 * @author Vitalii Katkov
 */
@Slf4j
public class BringContainer implements ObjectFactory {

  private final Map<Class<?>, Object> storageByClass = new ConcurrentHashMap<>();
  private final Map<String, Object> storageByName = new ConcurrentHashMap<>();
  private final BeanDefinitionRegistry definitionRegistry;

  public BringContainer(BeanDefinitionRegistry definitionRegistry) {
    this.definitionRegistry = definitionRegistry;
  }

  /**
   * Collect all bean definitions by specified scan packages and build container to create and hold
   * all found beans.
   *
   * @param scanPackages packages where to search beans
   * @return created beans container
   */
  public static BringContainer run(String... scanPackages) {
    BeanDefinitionRegistry definitionRegistry = new SimpleBeanDefinitionRegistry();
    AnnotatedBeanDefinitionReader beanDefinitionReader = new AnnotatedBeanDefinitionReader(
        definitionRegistry);
    RecursiveClassPathAnnotatedBeanScanner scanner = new RecursiveClassPathAnnotatedBeanScanner(
        new ScanUtilsImpl(), beanDefinitionReader);
    scanner.scan(scanPackages);

    BringContainer container = new BringContainer(definitionRegistry);

    definitionRegistry.getBeanDefinitions()
        .forEach(beanDefinition -> container.getBean(beanDefinition.getName()));

    ExecutorService executor = Executors.newFixedThreadPool(1);
    executor.submit(() -> TomcatServer.run(container));

    return container;
  }

  // TODO: 1. add dependency injection by @Autowired field

  @Override
  public Object getBean(String name) {
    if (storageByName.containsKey(name)) {
      return storageByName.get(name);
    }

    BeanDefinition beanDefinition = definitionRegistry.getBeanDefinition(name);
    if (beanDefinition == null) {
      throw new NoSuchBeanDefinitionException(
          "BeanDefinition for bean with name %s is not found! Check configuration and register this bean".formatted(
              name));
    }
    Class<?> beanClass = beanDefinition.getBeanClass();
    try {
      Constructor<?> declaredConstructor = beanDefinition.getInitConstructor();
      Object[] dependentBeans = prepareDependencies(beanDefinition);
      Object newInstance = declaredConstructor.newInstance(dependentBeans);
      storageByClass.put(beanClass, newInstance);
      storageByName.put(beanDefinition.getName(), newInstance);
      return newInstance;
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException
             | IllegalArgumentException e) {
      // TODO: add additional logging with some input parameters
      throw new InstanceCreationException(
          "Could not create an instance of \"%s\" class!".formatted(name), e);
    }
  }

  private Object[] prepareDependencies(BeanDefinition beanDefinition) {
    List<BeanDependency> dependencies = beanDefinition.getDependencies();
    List<Object> result = new LinkedList<>();
    for (BeanDependency dependency : dependencies) {
      result.add(getDependency(dependency));
    }
    return result.toArray();
  }

  private Object getDependency(BeanDependency dependency) {
    String qualifier = dependency.qualifier();

    if (qualifier != null && definitionRegistry.containsBeanDefinition(qualifier)) {
      return getDependencyFromQualifier(qualifier, dependency);
    }

    if (definitionRegistry.containsBeanDefinition(dependency.name())) {
      return getBean(dependency.name());
    }

    if (dependency.type().isAnnotation() || isAbstract(dependency.type().getModifiers())) {
      return getDependencyForType(dependency.type());
    }

    log.error("No suitable dependency found for {}", dependency);
    throw new RuntimeException("No suitable dependency found for " + dependency);
  }

  private Object getDependencyFromQualifier(String qualifier, BeanDependency dependency) {
    BeanDefinition beanDefinition = definitionRegistry.getBeanDefinition(qualifier);

    if (dependency.type().isAssignableFrom(beanDefinition.getBeanClass())) {
      return getBean(qualifier);
    }

    log.error("Mismatched type for dependency {}. Expected: {}, Actual: {}",
        dependency, dependency.type(), beanDefinition.getBeanClass());
    throw new RuntimeException("Mismatched type for dependency " + dependency +
        ". Expected: " + dependency.type() + ", Actual: " + beanDefinition.getBeanClass());
  }

  private Object getDependencyForType(Class<?> type) {
    List<BeanDefinition> beanDefinitionByType = definitionRegistry.getBeanDefinitionByType(
        type);

    if (beanDefinitionByType.isEmpty()) {
      log.error("No bean definition found for type {}", type);
      throw new RuntimeException("No bean definition found for type " + type);
    }

    if (beanDefinitionByType.size() == 1) {
      return getBean(beanDefinitionByType.get(0).getName());
    }

    List<BeanDefinition> primaryBeans = beanDefinitionByType.stream()
        .filter(BeanDefinition::isPrimary)
        .toList();

    if (primaryBeans.size() == 1) {
      return getBean(primaryBeans.get(0).getName());
    }

    log.error("Multiple qualifying beans found for type {}", type);
    throw new RuntimeException("Multiple qualifying beans found for type " + type);
  }

  @Override
  public Object getBean(Class<?> clazz) {
      return getDependencyForType(clazz);
  }

  public List<Object> getAllBeans() {
    return storageByName.values().stream().toList();
  }
}
