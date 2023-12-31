package io.github.bobocodebreskul.server.exception;

/**
 * Custom exception for errors related to web method parameters in controller methods.
 */
public class WebMethodParameterException extends RuntimeException {

  public WebMethodParameterException(String message, Throwable cause) {
    super(message, cause);
  }

  public WebMethodParameterException(String message) {
    super(message);
  }
}
