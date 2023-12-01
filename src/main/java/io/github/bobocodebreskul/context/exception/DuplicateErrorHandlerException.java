package io.github.bobocodebreskul.context.exception;

/**
 * Exception thrown to indicate that an attempt to add a duplicate exception handler has been
 * detected. This exception typically occurs when an exception handler is being added to controller
 * and method already exists.
 */
public class DuplicateErrorHandlerException extends RuntimeException {

  public DuplicateErrorHandlerException(String message) {
    super(message);
  }
}
