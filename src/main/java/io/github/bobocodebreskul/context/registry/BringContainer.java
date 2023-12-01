package io.github.bobocodebreskul.context.registry;

import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.ConfigurationBeanDefinition;
import io.github.bobocodebreskul.context.exception.InstanceCreationException;
import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;
import io.github.bobocodebreskul.context.scan.RecursiveClassPathAnnotatedBeanScanner;
import io.github.bobocodebreskul.context.scan.utils.ScanUtilsImpl;
import io.github.bobocodebreskul.context.support.BeanDependencyUtils;
import io.github.bobocodebreskul.server.Banner;
import io.github.bobocodebreskul.server.TomcatServer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

/**
 * Implementation of the {@link ObjectFactory} as Bring beans container. Creates and holds all founded
 * and registered beans.
 *
 * @author Ruslan Hladchenko
 * @author Roman Pryshchepa
 * @author Mykola Filimonov
 * @author Vitalii Katkov
 */
public class BringContainer implements ObjectFactory {

  private final static Logger log = LoggerFactory.getLogger(BringContainer.class);
  private final Map<String, Object> storageByName = new ConcurrentHashMap<>();

  private final BeanDefinitionRegistry definitionRegistry;
  private final BeanDependencyUtils dependencyUtils;

  private BringContainer(BeanDefinitionRegistry definitionRegistry,
      BeanDependencyUtils dependencyUtils) {
    this.definitionRegistry = definitionRegistry;
    this.dependencyUtils = dependencyUtils;
  }

  /**
   * Collect all bean definitions by specified scan packages and build container to create and hold
   * all founded beans.
   *
   * @param configClass configuration class annotated @BringComponentScan with information where to
   *                    search beans
   * @return created beans container
   */
  public static BringContainer run(Class<?> configClass) {
    Banner.printBanner();

    log.info("Initializing BringContainer...");

    //prepare base context classes
    BeanDependencyUtils beanDependencyUtils = new BeanDependencyUtils();
    BeanDefinitionRegistry definitionRegistry = new SimpleBeanDefinitionRegistry();
    BeanDefinitionValidator beanDefinitionValidator = new BeanDefinitionValidator(definitionRegistry, beanDependencyUtils);
    ScanUtilsImpl scanUtils = new ScanUtilsImpl();
    BeanDefinitionReader beanDefinitionReader = new BeanDefinitionReader(definitionRegistry);
    RecursiveClassPathAnnotatedBeanScanner scanner = new RecursiveClassPathAnnotatedBeanScanner(scanUtils, beanDefinitionReader);
    BringContainer container = new BringContainer(definitionRegistry, beanDependencyUtils);

    //run initial scan for all project
    scanner.scan(configClass);
    beanDefinitionValidator.validateBeanDefinitions();
    //register all founded beans
    definitionRegistry.getBeanDefinitions()
        .forEach(beanDefinition -> container.getBean(beanDefinition.getName()));

    TomcatServer.run(container);
    log.info("BringContainer initialized successfully.");
    log.debug("All created beans:%n%s".formatted(container.storageByName.keySet().stream().reduce("", (s1,s2) -> s1 + System.lineSeparator() + s2)));
    return container;
  }

  @Override
  public Object getBean(String name) {
    if (storageByName.containsKey(name)) {
      return storageByName.get(name);
    }

    BeanDefinition beanDefinition = definitionRegistry.getBeanDefinition(name);
    if (beanDefinition == null) {
      String errorMessage = "BeanDefinition for bean with name %s is not found! Check configuration and register this bean".formatted(name);
      log.error(errorMessage);
      throw new NoSuchBeanDefinitionException(errorMessage);
    }

    if (beanDefinition instanceof AnnotatedGenericBeanDefinition) {
      return getBeanByConstructor(name, beanDefinition);
    }
    if (beanDefinition instanceof ConfigurationBeanDefinition) {
      return getBeanByMethod(name, (ConfigurationBeanDefinition) beanDefinition);
    }
    String errorMessage = "Cannot create bean with name %s, not supported BeanDefinition class: %s".formatted(name, beanDefinition.getClass());
    log.error(errorMessage);
    throw new InstanceCreationException(errorMessage);
  }

  private Object getBeanByConstructor(String name, BeanDefinition beanDefinition) {
    log.debug("Started create bean by constructor with name: %s".formatted(name));
    try {
      Constructor<?> declaredConstructor = beanDefinition.getInitConstructor();
      Object[] dependentBeans = findOrCreateBeanDependencies(beanDefinition);
      Object newInstance = declaredConstructor.newInstance(dependentBeans);

      if (beanDefinition.isPrototype()) {
        return newInstance;
      }

      storageByName.put(beanDefinition.getName(), newInstance);
      return newInstance;
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
             IllegalArgumentException e) {
      String errorMessage = "Could not create an instance of \"%s\" class, please check constructors and their parameters.".formatted(name);
      log.error(errorMessage, e);
      throw new InstanceCreationException(errorMessage, e);
    }
  }

  private Object getBeanByMethod(String name, ConfigurationBeanDefinition beanDefinition) {
    log.debug("Started create bean by method with name: %s".formatted(name));
    try {
      Method initMethod = beanDefinition.getBeanMethod();
      Object[] dependentBeans = findOrCreateBeanDependencies(beanDefinition);

      Object newInstance = initMethod.invoke(beanDefinition.getConfigurationInstance(),
          dependentBeans);

      storageByName.put(beanDefinition.getName(), newInstance);
      return newInstance;
    } catch (IllegalAccessException | InvocationTargetException e) {
      String errorMessage = "Could not create an instance of \"%s\" class, please check method from configuration class and their parameters ".formatted(name);
      log.error(errorMessage, e);
      throw new InstanceCreationException(errorMessage, e);
    }
  }

  private Object[] findOrCreateBeanDependencies(BeanDefinition beanDefinition) {
    List<BeanDefinition> dependentDefinitions = dependencyUtils.prepareDependencies(beanDefinition,
        definitionRegistry);
    return dependentDefinitions.stream()
        .map(dependentDefinition -> getBean(dependentDefinition.getName())).toArray();
  }

  public List<Object> getAllBeans() {
    return storageByName.values().stream().toList();
  }
}
