package io.github.bobocodebreskul.context.exception;

/**
 * Thrown to indicate about not implemented functionality. Designed to be thrown only
 * during development phase.
 */
public class FeatureNotImplementedException extends RuntimeException {
  public FeatureNotImplementedException(String message) {
    super(message);
  }
}
