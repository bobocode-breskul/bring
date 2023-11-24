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
   * Scans the specified configuration class, identifying classes annotated with
   * specific annotations.
   *
   * @param configClass the configuration class to scan
   */
  void scan(Class<?> configClass);
}
