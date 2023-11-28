package io.github.bobocodebreskul.context.exception;

/**
 * Exception thrown to indicate that an attempt to add a duplicate path has been detected.
 * This exception typically occurs when a path is being added to controller and method
 * already exists.
 */
public class DuplicatePathException extends RuntimeException{

  public DuplicatePathException(String message) {
    super(message);
  }
}
