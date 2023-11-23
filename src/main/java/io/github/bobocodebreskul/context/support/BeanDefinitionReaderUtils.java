package io.github.bobocodebreskul.context.support;

import static io.github.bobocodebreskul.context.support.ReflectionUtils.getConstructorsAnnotatedWith;
import static io.github.bobocodebreskul.context.support.ReflectionUtils.getDefaultConstructor;
import static io.github.bobocodebreskul.context.support.ReflectionUtils.hasDefaultConstructor;
import static io.github.bobocodebreskul.context.support.ReflectionUtils.isAnnotationPresentForAnyConstructor;
import static io.github.bobocodebreskul.context.support.ReflectionUtils.isAnnotationPresentForSingleConstructorOnly;

import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.BeanDefinitionCreationException;
import io.github.bobocodebreskul.context.exception.BeanDefinitionDuplicateException;
import io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods that are useful for bean definition reader implementations.
 */
@Slf4j
@UtilityClass
public class BeanDefinitionReaderUtils {

  static final String NO_DEFAULT_CONSTRUCTOR_MESSAGE = "Error creating bean with name '%s'. Failed to instantiate [%s]: No default constructor found";
  static final String MULTIPLE_AUTOWIRED_CONSTRUCTORS_MESSAGE = "Error creating bean with name '%s': Invalid autowire-marked constructor: %s. Found constructor with Autowired annotation already: %s";
  static final String CLASS_WITHOUT_CONSTRUCTORS_MESSAGE = "Error creating bean with name '%s'. Failed to instantiate [%s]: No constructors found, target type is one of the list: [interface; a primitive type; an array class; void]";


  /**
   * Generates a unique bean name for the given bean definition,
   *
   * @param beanDefinition the bean definition to generate a bean name for
   * @param registry       the storage of previously registered bean definition instances (to check
   *                       for existing bean names)
   * @return the generated bean name
   */
  public static String generateBeanName(BeanDefinition beanDefinition,
      BeanDefinitionRegistry registry) {
    Objects.requireNonNull(beanDefinition, () -> {
      log.error("Failed to generate bean name for nullable bean definition");
      return "Null bean definition specified";
    });
    validateBeanClassNonNull(beanDefinition.getBeanClass());
    validateBeanClassName(beanDefinition, registry);

    String beanName = uncapitalize(beanDefinition.getBeanClass());

    log.trace("Generated bean name: {} for class {}", beanName,
        beanDefinition.getBeanClass().getName());

    return beanName;
  }

  /**
   * Generate bean name for the given class type.
   *
   * @param beanClass bean class to generate bean name for
   * @return the generated bean name
   */
  public static String generateClassBeanName(Class<?> beanClass) {
    validateBeanClassNonNull(beanClass);
    return uncapitalize(beanClass);
  }

  /**
   * Returns a list of bean constructor dependencies (as {@link BeanDependency}) this bean depends
   * on by reading constructor argument types.
   *
   * @param beanConstructor the constructor of the bean to analyze for dependencies
   * @return a list of bean constructor dependency types
   */
  public static List<BeanDependency> getConstructorBeanDependencies(
      Constructor<?> beanConstructor) {
    Objects.requireNonNull(beanConstructor, () -> {
      log.error("Failed to get bean constructor dependencies for nullable constructor");
      return "Bean constructor has not been specified";
    });
    log.trace("Scanning class {} for @Autowire candidates", beanConstructor.getDeclaringClass());

    return Arrays.stream(beanConstructor.getParameterTypes())
        .map(dependency -> new BeanDependency(generateClassBeanName(dependency), dependency))
        .toList();
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
  public static Constructor<?> findBeanInitConstructor(Class<?> beanClass, String beanName) {
    Constructor<?>[] declaredConstructors = beanClass.getDeclaredConstructors();

    // One constructor with or without @Autowired.
    if (declaredConstructors.length == 1) {
      log.trace("One init constructor found and registered [{}] for bean candidate [{}]",
          declaredConstructors[0], beanClass.getName());
      return declaredConstructors[0];
    } else if (declaredConstructors.length > 1) {
      log.trace("Multiple constructors found for bean candidate [{}]", beanClass.getName());
      if (isAnnotationPresentForAnyConstructor(Autowired.class, declaredConstructors)) {
        // Multiple constructors with only one @Autowired
        // TODO: checked
        if (isAnnotationPresentForSingleConstructorOnly(Autowired.class, declaredConstructors)) {
          Constructor<?> initConstructor =
              getConstructorsAnnotatedWith(Autowired.class, declaredConstructors).get(0);
          log.trace("@Autowired constructor found for bean candidate [{}]: [{}]",
              beanClass.getName(),
              initConstructor);
          return initConstructor;
        }
        // TODO: checked
        // Multiple constructors with multiple @Autowired annotations
        List<Constructor<?>> autowiredConstructors =
            getConstructorsAnnotatedWith(Autowired.class, declaredConstructors);
        log.error(
            "Bean candidate [{}] of type [{}] has more then 1 constructor marked with @Autowired.",
            beanName, beanClass);
        throw new BeanDefinitionCreationException(MULTIPLE_AUTOWIRED_CONSTRUCTORS_MESSAGE
            .formatted(beanName, autowiredConstructors.get(1), autowiredConstructors.get(0)));
      }

      // Multiple  constructors without @Autowired and with default constructor
      // TODO: checked
      if (hasDefaultConstructor(beanClass)) {
        log.trace(
            "No @Autowired constructor found for bean candidate [{}], default constructor registered as init one",
            beanName);
        return getDefaultConstructor(beanClass);
      }
      // TODO: checked
      // Multiple constructors without @Autowired and a default constructor
      log.error("Bean candidate [{}] of type [{}] has more then 1 constructor declared.",
          beanName, beanClass);
      throw new BeanDefinitionCreationException(
          NO_DEFAULT_CONSTRUCTOR_MESSAGE.formatted(beanName, beanClass.getName()));
    }

    throw new BeanDefinitionCreationException(
        CLASS_WITHOUT_CONSTRUCTORS_MESSAGE.formatted(beanName, beanClass.getName()));
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

  public static void validateBeanClassName(BeanDefinition beanDefinition,
      BeanDefinitionRegistry registry) {
    Class<?> beanClass = beanDefinition.getBeanClass();
    String beanClassName = beanClass.getName();

    if (isSameBeanNameFromAnotherPackage(registry, beanClass, beanClassName)) {
      log.error("Bean definition with name {} already existed", beanClassName);
      throw new BeanDefinitionDuplicateException(
          "Bean definition %s already exist".formatted(beanClassName));
    }
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
