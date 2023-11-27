package io.github.bobocodebreskul.context.support;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.DependencyNotResolvedException;
import io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Utils class to work with bean and bean definition dependencies.
 */
@Slf4j
public class BeanDependencyUtils {

  /**
   * Find all dependent bean definitions for specified target bean definition. Method will find and resolve bean
   * definitions by simple class name, qualifier, parent type and primary annotation.
   *
   * @param beanDefinition bean definition to find dependencies for
   * @param registry bean registry with all registered bean definitions
   * @return list of all dependent bean definitions
   */
  public List<BeanDefinition> prepareDependencies(BeanDefinition beanDefinition,
      BeanDefinitionRegistry registry) {
    return beanDefinition.getDependencies().stream()
        .map(dependency -> this.getDependency(dependency, registry))
        .toList();
  }

  private BeanDefinition getDependency(BeanDependency dependency, BeanDefinitionRegistry registry) {
    String qualifier = dependency.qualifier();

    if (qualifier != null) {
      if (registry.containsBeanDefinition(qualifier)) {
        return getDependencyFromQualifier(qualifier, dependency, registry);
      }
      throw new DependencyNotResolvedException("No suitable dependency found for qualifier " + qualifier);
    }

    if (registry.containsBeanDefinition(dependency.name())) {
      return registry.getBeanDefinition(dependency.name());
    }

    int modifiers = dependency.type().getModifiers();
    if (Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers)) {
      // TODO: IMPLEMENTATION - when class has sub-class marked as @BringComponent
      return getDependencyForType(dependency.type(), registry);
    }

    log.error("No suitable dependency found for {}", dependency);
    throw new DependencyNotResolvedException("No suitable dependency found for " + dependency);
  }

  private BeanDefinition getDependencyFromQualifier(String qualifier, BeanDependency dependency,
      BeanDefinitionRegistry registry) {
    BeanDefinition beanDefinition = registry.getBeanDefinition(qualifier);

    if (dependency.type().isAssignableFrom(beanDefinition.getBeanClass())) {
      return beanDefinition;
    }

    log.error("Mismatched type for dependency {}. Expected: {}, Actual: {}",
        dependency, dependency.type(), beanDefinition.getBeanClass());
    throw new DependencyNotResolvedException(String.format("Mismatched type for dependency %s. Expected: %s, Actual: %s",
        dependency, dependency.type(), beanDefinition.getBeanClass()));
  }

  private BeanDefinition getDependencyForType(Class<?> type, BeanDefinitionRegistry registry) {
    List<BeanDefinition> beanDefinitionByType = registry.getBeanDefinitionByType(type);

    if (beanDefinitionByType.isEmpty()) {
      log.error("No bean definition found for type {}", type);
      throw new DependencyNotResolvedException("No bean definition found for type " + type);
    }

    if (beanDefinitionByType.size() == 1) {
      return beanDefinitionByType.get(0);
    }

    List<BeanDefinition> primaryBeans = beanDefinitionByType.stream()
        .filter(BeanDefinition::isPrimary)
        .toList();

    if (primaryBeans.size() == 1) {
      return primaryBeans.get(0);
    }

    if (primaryBeans.size() > 1) {
      log.error("Multiple primary qualified beans found for type {}", type);
      throw new DependencyNotResolvedException("Multiple primary qualified beans found for type " + type);
    }

    log.error("Multiple qualifying beans found for type {}", type);
    String foundTypes = beanDefinitionByType.stream()
        .map(BeanDefinition::getBeanClass)
        .map("[%s]"::formatted)
        .collect(Collectors.joining(System.lineSeparator()));
    throw new DependencyNotResolvedException("Multiple qualifying beans found for type '%s'. Found types are: %s%s"
        .formatted(type, System.lineSeparator(), foundTypes));
  }
}
