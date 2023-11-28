package io.github.bobocodebreskul.context.exception;

/**
 * Exception thrown to indicate that an error occurred during web path validation.
 */
public class WebPathValidationException extends RuntimeException {
  public WebPathValidationException(String message) {
    super(message);
  }
}
