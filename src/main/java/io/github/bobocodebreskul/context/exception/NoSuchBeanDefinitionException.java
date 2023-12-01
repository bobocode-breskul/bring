package io.github.bobocodebreskul.context.exception;

/**
 * Bean definition not founded inside registry
 */
public class NoSuchBeanDefinitionException extends RuntimeException {

  public NoSuchBeanDefinitionException(String message) {
    super(message);
  }
}
