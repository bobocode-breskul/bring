package io.github.bobocodebreskul.context.exception;

/**
 * Exception thrown to indicate that an error occurred during the invocation of a method.
 */
public class MethodInvocationException extends RuntimeException{

  public MethodInvocationException(String message, Throwable cause) {
    super(message, cause);
  }
}
