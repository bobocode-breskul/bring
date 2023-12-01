package io.github.bobocodebreskul.context.exception;

/**
 * Thrown to indicate that body could not be read
 */
public class BodyReadException extends RuntimeException {

  public BodyReadException(String message) {
    super(message);
  }
}
