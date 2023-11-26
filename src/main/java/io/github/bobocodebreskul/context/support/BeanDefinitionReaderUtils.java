package io.github.bobocodebreskul.context.support;

import static io.github.bobocodebreskul.context.support.ReflectionUtils.getClassAnnotationValue;
import static io.github.bobocodebreskul.context.support.ReflectionUtils.getConstructorsAnnotatedWith;
import static io.github.bobocodebreskul.context.support.ReflectionUtils.getDefaultConstructor;
import static io.github.bobocodebreskul.context.support.ReflectionUtils.hasDefaultConstructor;
import static java.util.Objects.nonNull;

import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.annotations.Qualifier;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.BeanDefinitionCreationException;
import io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods that are useful for bean definition reader implementations.
 */
@Slf4j
@UtilityClass
public class BeanDefinitionReaderUtils {

  // todo: tip for the end-user to fix smth like: use @Autowired or create default constructor
  static final String NO_DEFAULT_CONSTRUCTOR_MESSAGE = "Error creating bean with name '%s'. Failed to instantiate [%s]: No default constructor found.";
  static final String MULTIPLE_AUTOWIRED_CONSTRUCTORS_MESSAGE = "Error creating bean with name '%s': Invalid autowire-marked constructor: %s. Found constructor with Autowired annotation already: %s";
  static final String CLASS_WITHOUT_CONSTRUCTORS_MESSAGE = "Error creating bean with name '%s'. Failed to instantiate [%s]: No constructors found, target type is one of the list: [interface; a primitive type; an array class; void]";
  static final String UNCERTAIN_BEAN_NAME_EXCEPTION_MSG = "For bean %s was found several different names definitions: [%s]. Please choose one.";
  private final static String COMPONENT_NAME_FIELD = "value";
  private final static String QUALIFIER_NAME_FIELD = "value";

  /**
   * Extract bean name from component annotation or generate a unique bean name for
   * the given bean definition
   *
   * @param beanClass the bean to generate a bean name for
   *
   * @return the generated bean name
   */
  public String getBeanName(Class<?> beanClass) {

    validateBeanClassNonNull(beanClass);

    Set<String> namesFromAnnotations = extractBeanNamesFromAnnotations(beanClass);

    if (namesFromAnnotations.isEmpty()) {
      return generateBeanName(beanClass);
    }
    if (namesFromAnnotations.size() == 1) {
      return namesFromAnnotations.stream().findFirst().get();
    }

    String beanNames = String.join(", ", namesFromAnnotations);
    log.error(
      "For bean {} was found several different names definitions: [{}]. Please choose one.",
      beanClass.getName(), beanNames);
    throw new IllegalStateException(
      UNCERTAIN_BEAN_NAME_EXCEPTION_MSG.formatted(beanClass.getName(), beanNames));
  }

  private static String generateBeanName(Class<?> beanClass) {

    String beanName = uncapitalize(beanClass);

    log.trace("Generated bean name: {} for class {}", beanName,
        beanClass.getName());

    return beanName;
  }

  /**
   * Returns a list of bean constructor dependencies (as {@link BeanDependency}) this bean depends
   * on by reading constructor argument types.
   *
   * @param beanConstructor the constructor of the bean to analyze for dependencies
   * @return a list of bean constructor dependency types
   */
  public static List<BeanDependency> getConstructorBeanDependencies(Constructor<?> beanConstructor) {
    Objects.requireNonNull(beanConstructor, () -> {
      log.error("Failed to get bean constructor dependencies for nullable constructor");
      return "Bean constructor has not been specified";
    });
    log.trace("Scanning class {} for @Autowire candidates", beanConstructor.getDeclaringClass());

    Map<String, String> parameterNameByAnnotationValue =
      ReflectionUtils.extractMethodAnnotationValues(beanConstructor, Qualifier.class,
        QUALIFIER_NAME_FIELD, String.class);

    return Arrays.stream(beanConstructor.getParameters())
      .map(parameter -> new BeanDependency(getBeanName(parameter.getType()), parameterNameByAnnotationValue.get(parameter.getName()), parameter.getType()))
      .collect(Collectors.toList());
  }


  /**
   * Finds and returns the initialization constructor for a given bean class. The initialization
   * constructor is determined based on the presence of the {@link Autowired} annotation and the
   * number of available constructors.
   *
   * @param beanClass the class of the bean to find the initialization constructor for.
   * @param beanName  the name of the bean, used for logging and error handling.
   * @return the initialization constructor for the specified bean class.
   * @throws BeanDefinitionCreationException If there are issues with the bean's constructors, such
   *                                         as having more than one constructor without the
   *                                         {@link Autowired} annotation or multiple constructors
   *                                         marked with {@link Autowired}.
   */
  // TODO: need to discuss: remove beanName
  public static Constructor<?> findBeanInitConstructor(Class<?> beanClass, String beanName) {
    Constructor<?>[] declaredConstructors = beanClass.getDeclaredConstructors();

    // zero constructor: interface; a primitive type; an array class; void
    if (declaredConstructors.length == 0) {
      throw new BeanDefinitionCreationException(
          CLASS_WITHOUT_CONSTRUCTORS_MESSAGE.formatted(beanName, beanClass.getName()));
    }

    // One constructor with or without @Autowired.
    if (declaredConstructors.length == 1) {
      log.trace("One init constructor found and registered [{}] for bean candidate [{}]",
          declaredConstructors[0], beanClass.getName());
      return declaredConstructors[0];
    }

    log.trace("Multiple constructors found for bean candidate [{}]", beanClass.getName());

    List<Constructor<?>> autowiredConstructors =
        getConstructorsAnnotatedWith(Autowired.class, declaredConstructors);
    if (autowiredConstructors.size() > 1) {
      // Multiple constructors with multiple @Autowired annotations
      log.error(
          "Bean candidate [{}] of type [{}] has more then 1 constructor marked with @Autowired.",
          beanName, beanClass);
      throw new BeanDefinitionCreationException(MULTIPLE_AUTOWIRED_CONSTRUCTORS_MESSAGE
          .formatted(beanName, autowiredConstructors.get(1), autowiredConstructors.get(0)));
    }

    if (autowiredConstructors.size() == 1) {
      // Multiple constructors with only one @Autowired
      Constructor<?> initConstructor = autowiredConstructors.get(0);
      log.trace("@Autowired constructor found for bean candidate [{}]: [{}]", beanClass.getName(),
          initConstructor);
      return initConstructor;
    }

    // Multiple  constructors without @Autowired and with default constructor
    if (hasDefaultConstructor(beanClass)) {
      log.trace(
          "No @Autowired constructor found for bean candidate [{}], default constructor registered as init one",
          beanName);
      return getDefaultConstructor(beanClass);
    }

    // Multiple constructors without @Autowired and a default constructor
    log.error("Bean candidate [{}] of type [{}] has more then 1 constructor declared.",
        beanName, beanClass);
    throw new BeanDefinitionCreationException(
        NO_DEFAULT_CONSTRUCTOR_MESSAGE.formatted(beanName, beanClass.getName()));
  }

  /**
   * Determines whether a given bean class is a candidate for autowiring by checking its
   * dependencies against the registered bean definitions in the provided
   * {@link BeanDefinitionRegistry}.
   *
   * @param beanClass the class of the bean to check for autowire candidacy
   * @param registry  the BeanDefinitionRegistry containing the registered bean definitions
   * @return true if the bean is a candidate for autowiring, false otherwise
   */
  public static boolean isBeanAutowireCandidate(Class<?> beanClass,
      BeanDefinitionRegistry registry) {
    log.trace("Check if bean class {} is autowire candidate", beanClass.getName());
    boolean isAutowiredCandidate = registry.getBeanDefinitions().stream()
        .filter(beanDefinition -> !beanDefinition.getBeanClass().equals(beanClass))
        .flatMap(beanDefinition -> beanDefinition.getDependencies().stream())
        .anyMatch(beanDefinitionClass -> beanDefinitionClass.type().equals(beanClass));
    if (isAutowiredCandidate) {
      log.trace("Bean class {} is registered as autowired candidate", beanClass.getName());
    }
    return isAutowiredCandidate;
  }

  // TODO: possible move to separate general validation class
  private static void validateBeanClassNonNull(Class<?> beanClass) {
    Objects.requireNonNull(beanClass, () -> {
      log.error("Failed to generate bean name for nullable bean class");
      return "Bean class has not been specified";
    });
  }

  public static void validateBeanName(String name, BeanDefinitionRegistry registry) {

    if (registry.isBeanNameInUse(name)) {
      log.error("Bean definition with name {} already exists", name);
      throw new BeanDefinitionDuplicateException(
          "Bean definition %s already exist".formatted(name));
    }
  }

  private static Set<String> extractBeanNamesFromAnnotations(Class<?> beanClass) {
    return Arrays.stream(beanClass.getAnnotations())
      .filter(ReflectionUtils::isComponentAnnotation)
      .map(annotation -> getClassAnnotationValue(beanClass,
        annotation.annotationType(), COMPONENT_NAME_FIELD, String.class))
      .filter(beanName -> nonNull(beanName) && !beanName.isBlank())
      .collect(Collectors.toSet());
  }

  private static String uncapitalize(Class<?> beanClass) {
    return StringUtils.uncapitalize(beanClass.getSimpleName());
  }

  private static boolean isSameBeanNameFromAnotherPackage(BeanDefinitionRegistry registry,
      Class<?> beanClass,
      String beanName) {
    return registry.isBeanNameInUse(beanName)
        && !Objects.equals(beanClass.getPackageName(),
        registry.getBeanDefinition(beanName).getBeanClass().getPackageName());

  }
}
