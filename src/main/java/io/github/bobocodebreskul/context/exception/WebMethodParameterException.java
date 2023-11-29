package io.github.bobocodebreskul.context.exception;

public class WebMethodParameterException extends RuntimeException {

  public WebMethodParameterException(String message, Throwable cause) {
    super(message, cause);
  }

  public WebMethodParameterException(String message) {
    super(message);
  }
}
