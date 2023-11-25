package io.github.bobocodebreskul.context.registry;

import static io.github.bobocodebreskul.context.config.BeanDefinition.PROTOTYPE_SCOPE;
import static io.github.bobocodebreskul.context.config.BeanDefinition.SINGLETON_SCOPE;
import static io.github.bobocodebreskul.context.support.BeanDefinitionReaderUtils.findBeanInitConstructor;
import static io.github.bobocodebreskul.context.support.BeanDefinitionReaderUtils.generateBeanName;
import static io.github.bobocodebreskul.context.support.BeanDefinitionReaderUtils.getConstructorBeanDependencies;
import static io.github.bobocodebreskul.context.support.BeanDefinitionReaderUtils.isBeanAutowireCandidate;
import static java.util.Objects.nonNull;

import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.Primary;
import io.github.bobocodebreskul.context.annotations.Scope;
import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.BeanDefinitionCreationException;
import io.github.bobocodebreskul.context.exception.DuplicateBeanDefinitionException;
import io.github.bobocodebreskul.context.support.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class AnnotatedBeanDefinitionReader {

  final static String UNCERTAIN_BEAN_NAME_EXCEPTION_MSG = "For bean %s was found several different names definitions: [%s]. Please choose one.";
  private final static String COMPONENT_NAME_FIELD = "value";
  private final BeanDefinitionRegistry beanDefinitionRegistry;

  /**
   * Create a new AnnotatedBeanDefinitionReader for the given registry.
   *
   * @param registry the storage to load bean definitions into, in the form of a
   *                 BeanDefinitionRegistry
   */
  public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
    this.beanDefinitionRegistry = registry;
  }

  /**
   * Register one or more component classes to be processed. Adding the same component class more
   * than once causes {@link DuplicateBeanDefinitionException}.
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
    String name = extractBeanName(beanClass).orElse(null);
    doRegisterBean(beanClass, name);
  }

  /**
   * Retrieves the associated Bean Definition Registry.
   *
   * @return The Bean Definition Registry to which this reader is operates on.
   */
  public final BeanDefinitionRegistry getBeanDefinitionRegistry() {
    return beanDefinitionRegistry;
  }

  private Optional<String> extractBeanName(Class<?> beanClass) {
    Set<String> componentAnnotations = Arrays.stream(beanClass.getAnnotations())
        .filter(ReflectionUtils::isComponentAnnotation)
        .map(annotation -> ReflectionUtils.getClassAnnotationValue(beanClass,
            annotation.annotationType(), COMPONENT_NAME_FIELD, String.class))
        .filter(beanName -> nonNull(beanName) && !beanName.isBlank())
        .collect(Collectors.toSet());
    if (componentAnnotations.isEmpty()) {
      return Optional.empty();
    } else if (componentAnnotations.size() == 1) {
      return componentAnnotations.stream().findFirst();
    }
    String beanNames = String.join(", ", componentAnnotations);
    log.error(
        "For bean {} was found several different names definitions: [{}]. Please choose one.",
        beanClass.getName(), beanNames);
    throw new IllegalStateException(
        UNCERTAIN_BEAN_NAME_EXCEPTION_MSG.formatted(beanClass.getName(), beanNames));
  }

  private <T> void doRegisterBean(Class<T> beanClass, String beanName) {
    log.debug("doRegisterBean method invoked: beanClass={}, name={}",
        beanClass.getName(),
        beanName);
    log.info("Registering the bean definition of class {}", beanClass.getName());

    var annotatedBeanDefinition = new AnnotatedGenericBeanDefinition(beanClass);
    beanName = beanName != null ?
        beanName : generateBeanName(annotatedBeanDefinition, beanDefinitionRegistry);
    annotatedBeanDefinition.setName(beanName);

    if (beanDefinitionRegistry.isBeanNameInUse(beanName)) {
      log.error("The specified bean name is already in use");
      throw new DuplicateBeanDefinitionException(
          "The bean definition with specified name %s already exists".formatted(beanName));
    }

    if (ReflectionUtils.isAnnotationPresentForClass(Primary.class, beanClass)) {
      log.trace("Found @Primary annotation on the beanName={}", beanName);
      annotatedBeanDefinition.setPrimary(true);
    }

    annotatedBeanDefinition.setScope(getBeanDefinitionScope(beanClass, beanName));

    Constructor<?> beanConstructor = findBeanInitConstructor(beanClass, beanName);
    log.debug("Constructor found for bean class [{}]: [{}]", beanClass.getName(), beanConstructor);
    annotatedBeanDefinition.setInitConstructor(beanConstructor);
    List<BeanDependency> dependencies = getConstructorBeanDependencies(beanConstructor);
    log.debug("{} dependencies found for beanClass={} with beanName={}",
        dependencies.size(), beanClass.getName(), beanName);
    annotatedBeanDefinition.setDependencies(dependencies);
    annotatedBeanDefinition.setAutowireCandidate(
        isBeanAutowireCandidate(beanClass, beanDefinitionRegistry));

    beanDefinitionRegistry.registerBeanDefinition(beanName, annotatedBeanDefinition);
    log.trace("Registered bean definition: {}", annotatedBeanDefinition);
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
}
