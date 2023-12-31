package io.github.bobocodebreskul.context.registry;

import static io.github.bobocodebreskul.context.config.BeanDefinition.PROTOTYPE_SCOPE;
import static io.github.bobocodebreskul.context.config.BeanDefinition.SINGLETON_SCOPE;
import static io.github.bobocodebreskul.context.support.BeanDefinitionReaderUtils.findBeanInitConstructor;
import static io.github.bobocodebreskul.context.support.BeanDefinitionReaderUtils.getBeanMethodDependencies;

import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.BringConfiguration;
import io.github.bobocodebreskul.context.annotations.Primary;
import io.github.bobocodebreskul.context.annotations.Scope;
import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.config.ConfigurationBeanDefinition;
import io.github.bobocodebreskul.context.exception.BeanDefinitionCreationException;
import io.github.bobocodebreskul.context.exception.BeanDefinitionDuplicateException;
import io.github.bobocodebreskul.context.support.BeanDefinitionReaderUtils;
import io.github.bobocodebreskul.context.support.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;

/**
 * This class designed to read bean meta-information from annotated classes and subsequently
 * register corresponding bean definitions in a Bean Definition Registry.
 *
 * <p>The primary goal of this class is to simplify the integration of classes annotated with
 * {@link BringComponent} into a custom Inversion of Control (IoC) container by automating the bean
 * definition registration process. It analyzes meta-information from provided bean classes, such as
 * scope, dependencies, and autowiring details, and registers them as bean definitions within the
 * specified {@link BeanDefinitionRegistry}.
 *
 * <p>Usage Example:
 * <pre>
 * {@code
 *   BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
 *   AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(registry);
 *   reader.register(MyComponent.class, AnotherComponent.class);
 * }
 * </pre>
 *
 * <p>The bean definitions are generated based on the provided classes and their meta-information.
 * This involves looking at annotated class elements like constructors, fields, and methods,
 * especially those with {@link Autowired} or {@link Primary}.
 *
 * @see BeanDefinitionRegistry
 * @see AnnotatedGenericBeanDefinition
 * @see Autowired
 * @see Primary
 * @see BringComponent
 */
public class BeanDefinitionReader {

  private final static Logger log = LoggerFactory.getLogger(BeanDefinitionReader.class);
  private final BeanDefinitionRegistry beanDefinitionRegistry;

  /**
   * Create a new AnnotatedBeanDefinitionReader for the given registry.
   *
   * @param registry the storage to load bean definitions into, in the form of a
   *                 BeanDefinitionRegistry
   */
  public BeanDefinitionReader(BeanDefinitionRegistry registry) {
    this.beanDefinitionRegistry = registry;
  }

  private static <T> String getBeanDefinitionScope(Class<T> beanClass, String beanName) {
    if (ReflectionUtils.isAnnotationPresentForClass(Scope.class, beanClass)) {
      String scopeName = beanClass.getAnnotation(Scope.class).value();
      log.trace("Found @Scope annotation on the beanName={}", beanName);
      if (PROTOTYPE_SCOPE.equals(scopeName)) {
        log.trace("Retrieve prototype scope for bean: {}", beanName);
        return PROTOTYPE_SCOPE;
      } else if (SINGLETON_SCOPE.equals(scopeName)) {
        log.trace("Retrieve singleton scope for bean: {}", beanName);
        return SINGLETON_SCOPE;
      } else if ("".equals(scopeName)) {
        log.trace("Retrieve default singleton scope for bean: {}", beanName);
        return SINGLETON_SCOPE;
      } else {
        log.error("Invalid scope name provided: {} for bean: {}", scopeName, beanName);
        throw new BeanDefinitionCreationException(
            "Invalid scope name provided %s".formatted(scopeName));
      }
    }

    log.trace("Retrieve default singleton scope for bean: {}", beanName);
    return SINGLETON_SCOPE;
  }

  /**
   * Register one or more component classes to be processed. Adding the same component class more
   * than once causes {@link BeanDefinitionDuplicateException}.
   *
   * @param componentClasses one or more component classes
   */
  public void register(Class<?>... componentClasses) {
    Arrays.stream(componentClasses)
        .forEach(this::registerBean);
  }

  /**
   * Register a bean from the given bean class, deriving its metadata from declared annotations.
   *
   * @param beanClass the class of the bean
   */
  public void registerBean(Class<?> beanClass) {
    if (ReflectionUtils.isAnnotationPresentForClass(BringConfiguration.class, beanClass)) {
      doRegisterConfigurationBean(beanClass);
    } else {
      doRegisterBean(beanClass);
    }
  }

  private <T> void doRegisterBean(Class<T> beanClass) {
    log.debug("doRegisterBean method invoked: beanClass={}", beanClass);
    log.debug("Registering the bean definition of class {}", beanClass.getName());
    String name = BeanDefinitionReaderUtils.getBeanName(beanClass);

    var annotatedBeanDefinition = new AnnotatedGenericBeanDefinition(beanClass);
    annotatedBeanDefinition.setName(name);

    if (ReflectionUtils.isAnnotationPresentForClass(Primary.class, beanClass)) {
      log.trace("Found @Primary annotation on the beanName={}", name);
      annotatedBeanDefinition.setPrimary(true);
    }

    annotatedBeanDefinition.setScope(getBeanDefinitionScope(beanClass, name));

    Constructor<?> beanConstructor = findBeanInitConstructor(beanClass, name);
    log.debug("Constructor found for bean class [{}]: [{}]", beanClass.getName(), beanConstructor);
    annotatedBeanDefinition.setInitConstructor(beanConstructor);
    List<BeanDependency> dependencies = getBeanMethodDependencies(beanConstructor);
    log.debug("{} dependencies found for beanClass={} with beanName={}",
        dependencies.size(), beanClass.getName(), name);
    annotatedBeanDefinition.setDependencies(dependencies);

    beanDefinitionRegistry.registerBeanDefinition(name, annotatedBeanDefinition);
    log.trace("Registered bean definition: {}", annotatedBeanDefinition);
  }

  private <T> void doRegisterConfigurationBean(Class<T> beanClass) {
    BeanDefinitionReaderUtils.getBeanMethods(beanClass).stream()
        .map(this::mapBeanMethodToBeanDefinition)
        .forEach(beanDefinition -> beanDefinitionRegistry.registerBeanDefinition(
            beanDefinition.getName(), beanDefinition));
  }

  private ConfigurationBeanDefinition mapBeanMethodToBeanDefinition(Method beanMethod) {

    log.debug("mapBeanMethodToBeanDefinition method invoked: beanClass={}",
        beanMethod.getReturnType());
    log.info("Registering the bean definition of class {}", beanMethod.getReturnType().getName());
    Class<?> beanType = beanMethod.getReturnType();
    Object configClassInstance = instantiateConfigurationClass(beanMethod.getDeclaringClass());

    String name = BeanDefinitionReaderUtils.getMethodBeanName(beanMethod);
    var configurationBeanDefinition = new ConfigurationBeanDefinition(beanType, beanMethod,
        configClassInstance);
    configurationBeanDefinition.setName(name);
    configurationBeanDefinition.setScope(SINGLETON_SCOPE);
    configurationBeanDefinition.setBeanClass(beanMethod.getReturnType());

    if (beanMethod.isAnnotationPresent(Primary.class)) {
      log.trace("Found @Primary annotation on the beanName={}", name);
      configurationBeanDefinition.setPrimary(true);
    }

    List<BeanDependency> dependencies = getBeanMethodDependencies(beanMethod);
    configurationBeanDefinition.setDependencies(dependencies);

    return configurationBeanDefinition;
  }

  private Object instantiateConfigurationClass(Class<?> configClass) {
    try {
      return configClass.getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException e) {
      throw new BeanDefinitionCreationException(
          "Default constructor invoke for configuration fails: %s. Configuration class use only default constructor, and not support injections.".formatted(
              configClass), e);
    }
  }
}
