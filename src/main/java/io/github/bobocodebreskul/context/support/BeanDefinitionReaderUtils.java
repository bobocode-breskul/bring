package io.github.bobocodebreskul.context.support;

import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.BeanDefinitionCreationException;
import io.github.bobocodebreskul.context.exception.BeanDefinitionDuplicateException;
import io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods that are useful for bean definition reader implementations.
 */
@Slf4j
@UtilityClass
public class BeanDefinitionReaderUtils {


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

    log.trace("Generated bean name: {} for class {}", beanName, beanDefinition.getBeanClass().getName());

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
   * Returns a list of bean dependencies (as {@link BeanDependency}) this bean depends on,
   * including constructor argument types, fields, and methods annotated with the
   * {@link Autowired} annotation.
   *
   * @param beanClass the class of the bean to scan for dependencies
   * @return a list of bean dependency types
   */
  public static List<BeanDependency> getBeanDependencies(Class<?> beanClass) {
    log.trace("Scanning class {} for @Autowire candidates", beanClass.getName());

    Constructor<?>[] declaredConstructors = beanClass.getDeclaredConstructors();

    validateBeanClassConstructorSize(beanClass.getName(), declaredConstructors);

    Stream<Class<?>> constructorDependenciesStream = Arrays.stream(declaredConstructors)
        .flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()));

    Stream<Class<?>> fieldDependenciesStream = Arrays.stream(beanClass.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(Autowired.class))
        .map(Field::getType);

    Stream<Class<?>> methodDependenciesStream = Arrays.stream(beanClass.getDeclaredMethods())
        .filter(method -> method.isAnnotationPresent(Autowired.class))
        .flatMap(method -> Arrays.stream(method.getParameterTypes()));

    return Stream.concat(Stream.concat(constructorDependenciesStream, fieldDependenciesStream),
            methodDependenciesStream)
        .map(dependency -> new BeanDependency(generateClassBeanName(dependency), dependency))
        .toList();
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
  
  public static void validateBeanClassName(BeanDefinition beanDefinition, BeanDefinitionRegistry registry) {
    Class<?> beanClass = beanDefinition.getBeanClass();
    String beanClassName = beanClass.getName();

    if (isSameBeanNameFromAnotherPackage(registry, beanClass, beanClassName)) {
      log.error("Bean definition with name {} already existed", beanClassName);
      throw new BeanDefinitionDuplicateException("Bean definition %s already exist".formatted(beanClassName));
    }
  }

  private static void validateBeanClassConstructorSize(String beanName, Constructor<?>[] declaredConstructors) {
    if (declaredConstructors.length != 1) {
      log.error("Bean candidate [{}] has more then 1 candidate: [{}]", beanName, declaredConstructors.length);
      throw new BeanDefinitionCreationException("Bean candidate should have only one constructor declared");
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
