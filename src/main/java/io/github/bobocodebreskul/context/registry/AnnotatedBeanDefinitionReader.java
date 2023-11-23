package io.github.bobocodebreskul.context.registry;

import static io.github.bobocodebreskul.context.support.BeanDefinitionReaderUtils.generateBeanName;
import static io.github.bobocodebreskul.context.support.BeanDefinitionReaderUtils.getBeanDependencies;
import static io.github.bobocodebreskul.context.support.BeanDefinitionReaderUtils.isBeanAutowireCandidate;
import static java.util.Objects.nonNull;

import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.Primary;
import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.DuplicateBeanDefinitionException;
import io.github.bobocodebreskul.context.support.ReflectionUtils;
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
      .filter(beanName -> nonNull(beanName) && !beanName.isEmpty())
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

  private <T> void doRegisterBean(Class<T> beanClass, String name) {
    log.debug("doRegisterBean method invoked: beanClass={}, name={}", beanClass.getName(), name);
    log.info("Registering the bean definition of class {}", beanClass.getName());
    // todo: create beanDefinitionValidator that validate things such as:
    //  bean name validity (not allowed characters), check for circular dependency

    var annotatedBeanDefinition = new AnnotatedGenericBeanDefinition(beanClass);
    name = name != null ? name : generateBeanName(annotatedBeanDefinition, beanDefinitionRegistry);
    annotatedBeanDefinition.setName(name);

    if (beanDefinitionRegistry.isBeanNameInUse(name)) {
      log.error("The specified bean name is already in use");
      throw new DuplicateBeanDefinitionException(
        "The bean definition with specified name %s already exists".formatted(name));
    }

    annotatedBeanDefinition.setScope(BeanDefinition.SINGLETON_SCOPE);

    if (ReflectionUtils.isAnnotationExistsFor(Primary.class, beanClass)) {
      log.trace("Found @Primary annotation on the beanName={}", name);
      annotatedBeanDefinition.setPrimary(true);
    }

    List<BeanDependency> dependencies = getBeanDependencies(beanClass);
    log.debug("{} dependencies found for beanClass={} with beanName={}",
        dependencies.size(), beanClass.getName(), name);
    annotatedBeanDefinition.setDependencies(dependencies);
    annotatedBeanDefinition.setAutowireCandidate(
      isBeanAutowireCandidate(beanClass, beanDefinitionRegistry));

    beanDefinitionRegistry.registerBeanDefinition(name, annotatedBeanDefinition);
    log.trace("Registered bean definition: {}", annotatedBeanDefinition);
  }

}
