package io.github.bobocodebreskul.context.exception;

/**
 * Thrown to indicate that bean name is not unique
 */
public class DuplicateBeanDefinitionException extends RuntimeException {

  public DuplicateBeanDefinitionException(String message) {
    super(message);
  }
}
