package io.github.bobocodebreskul.server.exception;

/**
 * Thrown to indicate that body could not be read
 */
public class BodyReadException extends RuntimeException {

  public BodyReadException(String message) {
    super(message);
  }
}
