package io.github.bobocodebreskul.context.registry;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CycleDependencyResolver {

  private final BeanDefinitionRegistry registry;
  private final Set<String> visitedBeanNames = new HashSet<>();
  private final Deque<BeanDefinition> beanDefinitionChain = new ArrayDeque<>();

  public CycleDependencyResolver(BeanDefinitionRegistry registry) {
    this.registry = registry;
  }

  public static void validateCycleDependency(BeanDefinitionRegistry registry) {
    for (BeanDefinition beanDefinition : registry.getBeanDefinitions()) {
      CycleDependencyResolver cycleDependencyResolver = new CycleDependencyResolver(registry);
      cycleDependencyResolver.validate(beanDefinition);
    }
  }

  public void validate(BeanDefinition beanDefinition) {
    beanDefinitionChain.push(beanDefinition);
    if (!visitedBeanNames.add(beanDefinition.getName())) {
      String fullPath = beanDefinitionChain.stream()
          .map(BeanDefinition::getName)
          .collect(Collectors.joining(" -> "));
      throw new RuntimeException("CYCLE FOUND, full path = " + fullPath);
    }
    List<BeanDependency> dependencies = beanDefinition.getDependencies();
    for (BeanDependency dependency : dependencies) {
      String dependencyName = dependency.name();
      BeanDefinition dependencyDefinition = registry.getBeanDefinition(dependencyName);
      validate(dependencyDefinition);
    }
    visitedBeanNames.remove(beanDefinition.getName());
    beanDefinitionChain.pop();
  }

}
