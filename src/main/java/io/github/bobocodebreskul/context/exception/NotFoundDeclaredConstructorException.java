package io.github.bobocodebreskul.context.exception;

/**
 * Exception thrown to indicate that the declared constructor of a class
 * could not be found during runtime. This typically occurs when attempting
 * to instantiate a class without the presence of a declared constructor.
 */
public class NotFoundDeclaredConstructorException extends RuntimeException {

  public NotFoundDeclaredConstructorException(String message, Throwable cause) {
    super(message, cause);
  }
}
