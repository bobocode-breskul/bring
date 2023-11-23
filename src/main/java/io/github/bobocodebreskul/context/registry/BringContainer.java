package io.github.bobocodebreskul.context.registry;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.FeatureNotImplementedException;
import io.github.bobocodebreskul.context.exception.InstanceCreationException;
import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;
import io.github.bobocodebreskul.context.scan.RecursiveClassPathAnnotatedBeanScanner;
import io.github.bobocodebreskul.context.scan.utils.ScanUtilsImpl;
import io.github.bobocodebreskul.server.TomcatServer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
      Constructor<?> declaredConstructor = beanClass.getDeclaredConstructors()[0];
      if (beanClass.getDeclaredConstructors().length > 1) {
        // TODO: multiple constructor logic not implemented
        throw new FeatureNotImplementedException(
            "Bean instantiation with multiple constructors is not implemented");
      }
      Object[] dependentBeans = beanDefinition.getDependencies().stream()
          .map(BeanDependency::name)
          .map(this::getBean)
          .toArray();
      Object newInstance = declaredConstructor.newInstance(dependentBeans);

      // TODO update java docs
      // TODO: add test when bean is Prototype then bean should not be stored in storageByClass and in storageByName
      if (beanDefinition.isPrototype()) {
        return newInstance;
      }

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

  @Override
  public Object getBean(Class<?> clazz) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getAllBeans() {
    return storageByName.values().stream().toList();
  }
}
