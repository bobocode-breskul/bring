package io.github.bobocodebreskul.context.registry;

import static io.github.bobocodebreskul.context.support.GeneralConstants.BACKSPACE;
import static io.github.bobocodebreskul.context.support.GeneralConstants.FORM_FEED;
import static io.github.bobocodebreskul.context.support.GeneralConstants.HORIZONTAL_TAB;
import static org.apache.commons.lang3.StringUtils.CR;
import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.StringUtils.isBlank;

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

  static final String DISALLOWED_BEAN_NAME_CHARACTERS_EXCEPTION_MESSAGE = "Bean candidate [%s] has invalid bean name: must not be blank or contain disallowed characters";
  private static final CharSequence[] DISALLOWED_BEAN_NAME_CHARACTERS =
      {SPACE, LF, CR, BACKSPACE, FORM_FEED, HORIZONTAL_TAB};

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
  private void validateForCircularDependency(BeanDefinition beanDefinition) {
    if (beanDefinition.getDependencies().isEmpty()) {
      return;
    }

    if (!visitedBeanNames.add(beanDefinition.getName())) {
      removeNonCycleParticipants(beanDefinition);
      throw new BeanDefinitionValidationException(buildErrorMessage(beanDefinitionChain));
    }
    beanDefinitionChain.push(beanDefinition);

    List<BeanDependency> dependencies = beanDefinition.getDependencies();
    for (BeanDependency dependency : dependencies) {
      BeanDefinition dependencyDefinition =
          beanDependencyUtils.getDependency(dependency, definitionRegistry);
      validateForCircularDependency(dependencyDefinition);
    }
    visitedBeanNames.remove(beanDefinition.getName());
    beanDefinitionChain.pop();
  }


  // TODO: 1. when bean definition without illegal characters then nothing thrown
  // TODO: 2. when bean definition with blank name then throw exception
  // TODO: 3. when bean definition contains illegal character then throw exception (parameterized test)
  private void validateBeanName(Class<?> beanClass, String beanName) {
    if (isBlank(beanName) || containsAny(beanName, DISALLOWED_BEAN_NAME_CHARACTERS)) {
      throw new BeanDefinitionValidationException(
          DISALLOWED_BEAN_NAME_CHARACTERS_EXCEPTION_MESSAGE.formatted(beanClass));
    }
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
              getFileLocation(currentBean.getBeanClass())));
    }
    errorMessage.append("└─────┘\n");

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

