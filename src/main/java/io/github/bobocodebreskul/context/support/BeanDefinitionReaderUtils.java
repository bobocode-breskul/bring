package io.github.bobocodebreskul.context.support;

import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
    Objects.requireNonNull(beanDefinition.getBeanClass(), () -> {
      log.error("Failed to generate bean name for nullable bean class");
      return "Bean class has not been specified";
    });

    Class<?> beanClass = beanDefinition.getBeanClass();
    String beanName = StringUtils.uncapitalize(beanClass.getSimpleName());
    if (isSameBeanNameFromAnotherPackage(registry, beanDefinition, beanName)) {
      beanName = beanClass.getName();
    }

    log.trace("Generated bean name: {} for class {}", beanName, beanClass.getName());
    return beanName;
  }

  /**
   * Returns a list of bean types that this bean depends on, including constructor argument types,
   * fields, and methods annotated with the {@link Autowired} annotation.
   *
   * @param beanClass the class of the bean to scan for dependencies
   * @return a list of bean dependency types
   */
  public static List<Class<?>> getBeanDependencies(Class<?> beanClass) {
    log.trace("Scanning class {} for @Autowire candidates", beanClass.getName());

    Constructor<?>[] declaredConstructors = beanClass.getDeclaredConstructors();
    // TODO: possible move to separate general validation class
    if (declaredConstructors.length != 1) {
      log.error("Bean candidate [{}] has more then 1 candidate: [{}]", beanClass.getName(), declaredConstructors.length);
      throw new BeanDefinitionCreationException("Bean candidate should have only one constructor declared");
    }
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
        .flatMap(beanDefinition -> beanDefinition.getDependsOn().stream())
        .anyMatch(beanDefinitionClass -> beanDefinitionClass.equals(beanClass));
    if (isAutowiredCandidate) {
      log.trace("Bean class {} is registered as autowired candidate", beanClass.getName());
    }
    return isAutowiredCandidate;
  }

  private static boolean isSameBeanNameFromAnotherPackage(BeanDefinitionRegistry registry,
      BeanDefinition beanDefinition,
      String beanName) {
    return registry.isBeanNameInUse(beanName)
        && !Objects.equals(beanDefinition.getBeanClass().getPackageName(),
        registry.getBeanDefinition(beanName).getBeanClass().getPackageName());

  }
}
