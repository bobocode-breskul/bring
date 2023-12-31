package io.github.bobocodebreskul.context.registry;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.exception.BeanDefinitionDuplicateException;
import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Interface that extends {@link AliasRegistry} and defines methods for managing bean definitions in
 * a registry. A bean definition represents the configuration metadata that defines how to create a
 * specific bean.
 */
public interface BeanDefinitionRegistry extends AliasRegistry {

  /**
   * Register a new bean definition with the specified name and definition.
   *
   * @param beanName       the name of the bean definition
   * @param beanDefinition the definition of the bean
   * @throws IllegalArgumentException         if the specified name or bean definition is null
   * @throws BeanDefinitionDuplicateException if the specified bean definition already exists in
   *                                          registry
   */
  void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

  /**
   * Remove the bean definition with the specified name.
   *
   * @param beanName the name of the bean definition to be removed
   * @throws IllegalArgumentException if the specified bean name is null
   */
  void removeBeanDefinition(String beanName);

  /**
   * Retrieve the bean definition with the specified name.
   *
   * @param beanName the name of the bean definition to be retrieved
   * @return the bean definition associated with the specified name
   * @throws IllegalArgumentException      if the specified bean name is null
   * @throws NoSuchBeanDefinitionException if this registry contains no bean definition for the
   *                                       specified bean class
   */
  BeanDefinition getBeanDefinition(String beanName);

  /**
   * Check if a bean definition with the specified name exists.
   *
   * @param beanName the name of the bean definition to be checked
   * @return true if a bean definition with the specified name exists in registry, false otherwise
   * @throws IllegalArgumentException if the specified bean name is null
   */
  boolean containsBeanDefinition(String beanName);

  /**
   * Retrieve the names of all registered bean definitions.
   *
   * @return a set of bean definition names
   */
  Set<String> getBeanDefinitionNames();

  /**
   * Retrieve the count of registered bean definitions.
   *
   * @return the number of registered bean definitions
   */
  int getBeanDefinitionCount();

  /**
   * Check if a bean name is already in use.
   *
   * @param beanName the name to be checked
   * @return true if the bean name is already in use, false otherwise
   * @throws IllegalArgumentException if the specified bean name is null
   */
  boolean isBeanNameInUse(String beanName);

  /**
   * Retrieves all registered bean definitions from the container.
   *
   * @return a collection containing all the bean definitions registered in the container
   */
  Collection<BeanDefinition> getBeanDefinitions();

  /**
   * Retrieve the bean definition with the specified class.
   *
   * @param beanClass the class of the bean definition to be retrieved
   * @return the bean definition associated with the specified class
   * @throws IllegalArgumentException      if the specified bean class is null
   * @throws NoSuchBeanDefinitionException if this registry contains no bean definition for the
   *                                       specified bean class
   */
  BeanDefinition getBeanDefinitionByClass(Class<?> beanClass);

  /**
   * Retrieves a list of {@link BeanDefinition} instances that match the specified type.
   *
   * <p>This method returns all registered bean definitions that are assignable to the given type.
   * The returned list may include primary and non-primary beans, depending on their availability.
   *
   * <p>If no bean definitions are found for the specified type, an empty list is returned.
   *
   * @param type The target type to match against bean definitions.
   * @return A list of {@link BeanDefinition} instances matching the specified type. An empty list
   * if no matching bean definitions are found.
   * @see BeanDefinition
   */
  List<BeanDefinition> getBeanDefinitionByType(Class<?> type);
}
