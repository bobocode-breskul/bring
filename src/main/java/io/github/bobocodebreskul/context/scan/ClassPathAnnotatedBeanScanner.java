package io.github.bobocodebreskul.context.scan;

import java.util.Set;

/**
 * Interface to recursively scan packages and search for bean definition classes. Return found bean
 * definitions in all package tree.
 *
 * @author Vitalii Katkov
 */
public interface ClassPathAnnotatedBeanScanner {
  // TODO: convert found classes to bean definitions
  // TODO: change return type from 'String' to bean definition class
  /**
   * Recursively scan packages and find all bean definitions located in those packages or in
   * packages provided by found configurations.
   *
   * @param scanPackages packages to scan
   * @return found bean definitions
   */
  Set<String> scan(String... scanPackages);
}
