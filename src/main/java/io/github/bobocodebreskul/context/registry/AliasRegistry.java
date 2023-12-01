package io.github.bobocodebreskul.context.registry;

import io.github.bobocodebreskul.context.exception.AliasDuplicateException;
import java.util.Set;

/**
 * Interface that defines methods for managing aliases in a registry. An alias is an alternative
 * name for a bean or a component.
 */
public interface AliasRegistry {

  /**
   * Register an alias for the given name.
   *
   * @param name  the canonical name of the bean or component
   * @param alias the alias to be registered
   * @throws IllegalArgumentException if the specified name or alias is null
   * @throws AliasDuplicateException  if the specified alias already exists in registry
   */
  void registerAlias(String name, String alias);

  /**
   * Remove the specified alias from the registry.
   *
   * @param alias the alias to be removed
   * @throws IllegalArgumentException if the specified alias is null
   */
  void removeAlias(String alias);


  /**
   * Check if a given name is an alias.
   *
   * @param alias the name to be checked for alias status
   * @return true if the given name is an alias, false otherwise
   * @throws IllegalArgumentException if the specified alias is null
   */
  boolean isAlias(String alias);

  /**
   * Get all registered aliases for the given name.
   *
   * @param name the canonical name of the bean or component
   * @return a set of aliases for the given name, or an empty set if there are no aliases
   * @throws IllegalArgumentException if the specified name is null
   */
  Set<String> getAliases(String name);
}
