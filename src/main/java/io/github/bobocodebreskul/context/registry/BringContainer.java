package io.github.bobocodebreskul.context.registry;

import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.ConfigurationBeanDefinition;
import io.github.bobocodebreskul.context.exception.InstanceCreationException;
import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;
import io.github.bobocodebreskul.context.scan.RecursiveClassPathAnnotatedBeanScanner;
import io.github.bobocodebreskul.context.scan.utils.ScanUtilsImpl;
import io.github.bobocodebreskul.context.support.BeanDependencyUtils;
import io.github.bobocodebreskul.server.TomcatServer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

  private final Map<String, Object> storageByName = new ConcurrentHashMap<>();

  private final BeanDefinitionRegistry definitionRegistry;
  private final BeanDependencyUtils dependencyUtils;

  public BringContainer(BeanDefinitionRegistry definitionRegistry, BeanDependencyUtils dependencyUtils) {
    this.definitionRegistry = definitionRegistry;
    this.dependencyUtils = dependencyUtils;
  }

  /**
   * Collect all bean definitions by specified scan packages and build container to create and hold
   * all found beans.
   *
   * @param configClass configuration class annotated @BringComponentScan with information where to
   *                    search beans
   * @return created beans container
   */
  public static BringContainer run(Class<?> configClass) {
    BeanDefinitionRegistry definitionRegistry = new SimpleBeanDefinitionRegistry();
    BeanDefinitionReader beanDefinitionReader = new BeanDefinitionReader(
        definitionRegistry);
    BeanDependencyUtils beanDependencyUtils = new BeanDependencyUtils();
    BeanDefinitionValidator beanDefinitionValidator =
        new BeanDefinitionValidator(definitionRegistry, beanDependencyUtils);
    RecursiveClassPathAnnotatedBeanScanner scanner = new RecursiveClassPathAnnotatedBeanScanner(
        new ScanUtilsImpl(), beanDefinitionReader);
    scanner.scan(configClass);
    beanDefinitionValidator.validateBeanDefinitions();

    BringContainer container = new BringContainer(definitionRegistry, beanDependencyUtils);

    definitionRegistry.getBeanDefinitions()
        .forEach(beanDefinition -> container.getBean(beanDefinition.getName()));

    TomcatServer.run(container);

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

    if (beanDefinition instanceof AnnotatedGenericBeanDefinition) {
      return getBeanByConstructor(name, beanDefinition);
    }
    if (beanDefinition instanceof ConfigurationBeanDefinition) {
      return getBeanByMethod(name, (ConfigurationBeanDefinition) beanDefinition);
    }
    throw new RuntimeException("Can not create bean, no init method to create bean");
  }

  private Object getBeanByConstructor(String name, BeanDefinition beanDefinition) {
    try {
      Constructor<?> declaredConstructor = beanDefinition.getInitConstructor();
      Object[] dependentBeans = findOrCreateBeanDependencies(beanDefinition);
      Object newInstance = declaredConstructor.newInstance(dependentBeans);

      if (beanDefinition.isPrototype()) {
        return newInstance;
      }

      storageByName.put(beanDefinition.getName(), newInstance);
      return newInstance;
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException
             | IllegalArgumentException e) {
      // TODO: add additional logging with some input parameters
      throw new InstanceCreationException(
          "Could not create an instance of \"%s\" class!".formatted(name), e);
    }
  }

  private Object getBeanByMethod(String name, ConfigurationBeanDefinition beanDefinition) {
    try {
      Method initMethod = beanDefinition.getBeanMethod();
      Object[] dependentBeans = findOrCreateBeanDependencies(beanDefinition);

      Object newInstance = initMethod.invoke(beanDefinition.getConfigurationInstance(), dependentBeans);

      storageByName.put(beanDefinition.getName(), newInstance);
      return newInstance;
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new InstanceCreationException(
          "Could not create an instance of \"%s\" class!".formatted(name), e);
    }
  }

  private Object[] findOrCreateBeanDependencies(BeanDefinition beanDefinition) {
    List<BeanDefinition> dependentDefinitions = dependencyUtils.prepareDependencies(
        beanDefinition, definitionRegistry);
    return dependentDefinitions.stream()
        .map(dependentDefinition -> getBean(dependentDefinition.getName()))
        .toArray();
  }

  public List<Object> getAllBeans() {
    return storageByName.values().stream().toList();
  }
}
