package io.github.bobocodebreskul.context.exception;

/**
 * Thrown to indicate bean could not be instantiated
 */
public class BeanDefinitionCreationException extends RuntimeException {

  public BeanDefinitionCreationException(String message) {
    super(message);
  }
}
