package io.github.bobocodebreskul.context.registry;

import static org.apache.commons.lang3.StringUtils.isBlank;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.exception.BeanDefinitionValidationException;
import io.github.bobocodebreskul.context.support.BeanDependencyUtils;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

// TODO: tests

/**
 * The {@code BeanDefinitionValidator} class is responsible for validating bean definitions within a
 * {@link BeanDefinitionRegistry}, checking for circular dependencies and ensuring proper bean
 * naming conventions.
 */
public class BeanDefinitionValidator {

  static final String DISALLOWED_BEAN_NAME_CHARACTERS_EXCEPTION_MESSAGE = "Bean candidate [%s] "
      + "has invalid bean name: must not be blank or contain disallowed characters";
  private static final Pattern DISALLOWED_BEAN_NAME_CHARS_PATTERN = Pattern.compile("[\\s\b]");

  private final BeanDefinitionRegistry definitionRegistry;
  private final BeanDependencyUtils beanDependencyUtils;

  /**
   * Set to keep track of visited bean names during circular dependency validation.
   */
  private final Set<String> visitedBeanNames = new HashSet<>();

  /**
   * Stack to maintain the chain of bean definitions for circular dependency tracking.
   */
  private final Deque<BeanDefinition> beanDefinitionChain = new ArrayDeque<>();

  public BeanDefinitionValidator(BeanDefinitionRegistry definitionRegistry,
      BeanDependencyUtils beanDependencyUtils) {
    this.definitionRegistry = definitionRegistry;
    this.beanDependencyUtils = beanDependencyUtils;
  }

  /**
   * Validates all bean definitions in the associated {@link BeanDefinitionRegistry}. This includes
   * checking for circular dependencies and ensuring valid bean names.
   */
  public void validateBeanDefinitions() {
    for (BeanDefinition beanDefinition : definitionRegistry.getBeanDefinitions()) {
      validateForCircularDependency(beanDefinition);
      validateBeanName(beanDefinition.getBeanClass(), beanDefinition.getName());
    }
  }

  // TODO: 1. When bean definition with empty dependencies then nothing thrown
  // TODO: 2. When bean definition with dependencies without circular dependency then nothing thrown
  // TODO: 3. When bean definition with circular dependency of format: a -> b -> c -> a then exception thrown and check error message
  // TODO: 4. When bean definition with circular dependency of format a -> b (interface, b1 impl) -> c -> a
  // TODO: 5. When bean definition with circular dependency of format a -> b (interface with b1, qualified b2 impl) -> c -> a  then exception thrown
  // TODO: 7. When bean definition with circular dependency of format a -> b (interface with b1, primary b2 impl) -> c -> a  then exception thrown
  // TODO: 8. When bean definition with circular dependency of format a -> b (interface with qualified b1, b2 impl) then nothing thrown
  // TODO: 9. When bean definition with circular dependency of format a -> b (interface with primary b1, b2 impl) then nothing thrown
  // TODO: 10. When bean definition with circular dependency of format a -> b -> c -> d -> b then exception thrown and only cycle participants present in the error message
  // TODO: 11. When bean definition with circular dependency of format a -> a then exception thrown
  private void validateForCircularDependency(BeanDefinition beanDefinition) {
    if (beanDefinition.getDependencies().isEmpty()) {
      return;
    }

    if (!visitedBeanNames.add(beanDefinition.getName())) {
      removeNonCycleParticipants(beanDefinition);
      throw new BeanDefinitionValidationException(buildErrorMessage());
    }
    beanDefinitionChain.push(beanDefinition);

    beanDependencyUtils.prepareDependencies(beanDefinition, definitionRegistry)
        .forEach(this::validateForCircularDependency);

    visitedBeanNames.remove(beanDefinition.getName());
    beanDefinitionChain.pop();
  }

  // TODO: IMPLEMENTATION - move to it's own validator class and use in
  //  BeanDefinitionReaderUtils.getBeanName and for Qualifier logic too
  // TODO: 1. when bean definition without illegal characters then nothing thrown
  // TODO: 2. when bean definition with blank name then throw exception
  // TODO: 3. when bean definition contains illegal character then throw exception (parameterized test)
  private void validateBeanName(Class<?> beanClass, String beanName) {
    if (isBlank(beanName) || DISALLOWED_BEAN_NAME_CHARS_PATTERN.matcher(beanName).find()) {
      throw new BeanDefinitionValidationException(
          DISALLOWED_BEAN_NAME_CHARACTERS_EXCEPTION_MESSAGE.formatted(beanClass));
    }
  }


  private String buildErrorMessage() {
    StringBuilder errorMessage =
        new StringBuilder("The dependencies of some of the beans form a cycle:")
            .append(System.lineSeparator());

    BeanDefinition firstBean = Objects.requireNonNull(beanDefinitionChain.pollLast());
    errorMessage.append("┌─────┐").append(System.lineSeparator());
    errorMessage.append("|  %s defined in file [%s]%n".formatted(firstBean.getName(),
        getFileLocation(firstBean.getBeanClass())));
    while (!beanDefinitionChain.isEmpty()) {
      BeanDefinition currentBean = beanDefinitionChain.pollLast();
      errorMessage.append("↑     ↓").append(System.lineSeparator());
      errorMessage.append("|  %s defined in file [%s]%n".formatted(currentBean.getName(),
          getFileLocation(currentBean.getBeanClass())));
    }
    errorMessage.append("└─────┘").append(System.lineSeparator());

    return errorMessage.toString();
  }

  private void removeNonCycleParticipants(BeanDefinition beanDefinition) {
    while (!beanDefinition.getName().equals(beanDefinitionChain.getLast().getName())) {
      beanDefinitionChain.pollLast();
    }
  }

  private String getFileLocation(Class<?> beanClass) {
    return Path.of(beanClass.getName()).toAbsolutePath().toString()
        .replace(".", "/")
        .concat(".java");
  }
}

