package io.github.bobocodebreskul.context.exception;

/**
 * Exception thrown to indicate that the web resource could not be found.
 */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
