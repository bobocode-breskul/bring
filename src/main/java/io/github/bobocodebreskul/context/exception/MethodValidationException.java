package io.github.bobocodebreskul.context.exception;

/**
 * Exception thrown to indicate that the method does not pass the validation.
 */
public class MethodValidationException extends RuntimeException {

  public MethodValidationException(String message) {
    super(message);
  }
}
