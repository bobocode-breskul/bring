package io.github.bobocodebreskul.context.registry;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.BeanDefinitionValidationException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: tests

/**
 * The {@code BeanDefinitionValidator} class is responsible for validating bean definitions within a
 * {@link BeanDefinitionRegistry}, checking for circular dependencies and ensuring proper bean
 * naming conventions.
 */
public class BeanDefinitionValidator {

  private final BeanDefinitionRegistry definitionRegistry;

  /**
   * Set to keep track of visited bean names during circular dependency validation.
   */
  private final Set<String> visitedBeanNames = new HashSet<>();

  /**
   * Stack to maintain the chain of bean definitions for circular dependency tracking.
   */
  private final Deque<BeanDefinition> beanDefinitionChain = new ArrayDeque<>();

  public BeanDefinitionValidator(BeanDefinitionRegistry definitionRegistry) {
    this.definitionRegistry = definitionRegistry;
  }

  /**
   * Validates all bean definitions in the associated {@link BeanDefinitionRegistry}. This includes
   * checking for circular dependencies and ensuring valid bean names.
   */
  public void validateBeanDefinitions() {
    for (BeanDefinition beanDefinition : definitionRegistry.getBeanDefinitions()) {
      validateForCircularDependency(beanDefinition);
      validateBeanName(beanDefinition.getName());
    }
  }

  // TODO: revisit for better improved logic
  private void validateForCircularDependency(BeanDefinition beanDefinition) {
    if (beanDefinition.getDependencies().isEmpty()) {
      return;
    }

    if (!visitedBeanNames.add(beanDefinition.getName())) {
      throw new BeanDefinitionValidationException(buildErrorMessage(beanDefinitionChain));
    }
    beanDefinitionChain.push(beanDefinition);

    List<BeanDependency> dependencies = beanDefinition.getDependencies();
    for (BeanDependency dependency : dependencies) {
      String dependencyName = dependency.name();
      BeanDefinition dependencyDefinition = definitionRegistry.getBeanDefinition(dependencyName);
      validateForCircularDependency(dependencyDefinition);
    }
    visitedBeanNames.remove(beanDefinition.getName());
    beanDefinitionChain.pop();
  }

  private void validateBeanName(String beanName) {
    //TODO: write validation for not allowed characters in the beanName
  }

  private String buildErrorMessage(Deque<BeanDefinition> circularDependencies) {
    StringBuilder errorMessage =
        new StringBuilder("The dependencies of some of the beans form a cycle:\n");

    BeanDefinition firstBean = circularDependencies.pollLast();
    errorMessage.append(String.format("┌─────┐\n|  %s defined in file [%s]\n", firstBean.getName(),
        getFileLocation(firstBean.getBeanClass())));
    while (!circularDependencies.isEmpty()) {
      BeanDefinition currentBean = circularDependencies.pollLast();
      errorMessage.append("↑     ↓\n");
      errorMessage.append(
          String.format("|  %s defined in file [%s]\n", currentBean.getName(),
              getFileLocation(firstBean.getBeanClass())));
    }
    errorMessage.append("└─────┘\n");

    return errorMessage.toString();
  }

  private String getFileLocation(Class<?> beanClass) {
    return Path.of(beanClass.getName()).toAbsolutePath().toString()
        .replace(".", "/")
        .concat(".java");
  }
}

