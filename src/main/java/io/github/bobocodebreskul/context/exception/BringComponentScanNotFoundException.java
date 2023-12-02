package io.github.bobocodebreskul.context.exception;

/**
 * Custom exception indicating that the {@code BringComponentScan} annotation is not found on a
 * class. This exception extends {@link RuntimeException}.
 */
public class BringComponentScanNotFoundException extends RuntimeException {

  /**
   * Constructs a new {@code BringComponentScanNotFoundException} with the specified detail
   * message.
   *
   * @param message the detail message.
   */
  public BringComponentScanNotFoundException(String message) {
    super(message);
  }
}
