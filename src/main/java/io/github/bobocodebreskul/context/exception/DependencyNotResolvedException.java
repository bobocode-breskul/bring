package io.github.bobocodebreskul.context.exception;

/**
 * Thrown to indicate that dependency could not be resolved, for example no bean definition by name, qualifier or
 * parent class type.
 */
public class DependencyNotResolvedException extends RuntimeException {

  public DependencyNotResolvedException(String message) {
    super(message);
  }

}
