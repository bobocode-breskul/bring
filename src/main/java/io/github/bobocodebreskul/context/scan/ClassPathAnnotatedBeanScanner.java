package io.github.bobocodebreskul.context.scan;

/**
 * Interface to recursively scan packages and register found bean definition classes
 * in {@link io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry}.
 *
 * @author Vitalii Katkov
 * @author Oleksandr Karpachov
 */
public interface ClassPathAnnotatedBeanScanner {

  /**
   * Recursively scan packages and find all bean definitions located in those packages or in
   * packages provided by found configurations. Then register class as bean definition by
   * using {@link io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry}
   *
   * @param scanPackages packages to scan
   */
  void scan(String... scanPackages);
}
