package io.github.bobocodebreskul.context.exception;

/**
 * Thrown to indicate that an attempt to register an alias resulted in a duplicate alias. This
 * exception is typically thrown when trying to register an alias that already exists in the alias
 * registry.
 */
public class AliasDuplicateException extends RuntimeException {

  public AliasDuplicateException(String message) {
    super(message);
  }
}
