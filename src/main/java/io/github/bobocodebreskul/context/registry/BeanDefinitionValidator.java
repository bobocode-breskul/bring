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

/**
 * The {@code BeanDefinitionValidator} class is responsible for validating bean definitions within a
 * {@link BeanDefinitionRegistry}, checking for circular dependencies and ensuring proper bean
 * naming conventions.
 */
// TODO logs
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

