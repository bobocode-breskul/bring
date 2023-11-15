package io.github.bobocodebreskul.context.exception;

public class BeanDefinitionDuplicateException extends RuntimeException {

  /**
   * Thrown to indicate that an attempt to register a bean definition resulted in a duplicate bean
   * definition. This exception is typically thrown when trying to register a bean definition with a
   * name that already exists in the bean definition registry.
   */
  public BeanDefinitionDuplicateException(String message) {
    super(message);
  }
}
