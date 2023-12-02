package io.github.bobocodebreskul.server.exception;

/**
 * Thrown to indicate that method has more than one http annotation
 */
public class AmbiguousHttpAnnotationException extends RuntimeException {

  public AmbiguousHttpAnnotationException(String message) {
    super(message);
  }
}
