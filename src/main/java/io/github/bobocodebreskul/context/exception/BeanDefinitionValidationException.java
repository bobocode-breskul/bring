package io.github.bobocodebreskul.context.exception;

/**
 * Thrown to indicate that there are conflicts during bean definition validation
 */
public class BeanDefinitionValidationException extends RuntimeException {

  public BeanDefinitionValidationException(String message) {
    super(message);
  }
}
