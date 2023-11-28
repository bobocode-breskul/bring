package io.github.bobocodebreskul.context.exception;

/**
 * Exception thrown to indicate that an error occurred during the process request in dispatcher servlet.
 */
public class DispatcherServletException extends RuntimeException {
  public DispatcherServletException(Throwable cause) {
    super(cause);
  }
}
