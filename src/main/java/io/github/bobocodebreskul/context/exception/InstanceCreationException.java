package io.github.bobocodebreskul.context.exception;

/**
 * Exception thrown to indicate that an instance of a class could not be created during runtime.
 * This may happen due to issues such as the absence of a public no-argument constructor or other
 * instantiation problems.
 */
public class InstanceCreationException extends RuntimeException {

  public InstanceCreationException(String message) {
    super(message);
  }

  public InstanceCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
