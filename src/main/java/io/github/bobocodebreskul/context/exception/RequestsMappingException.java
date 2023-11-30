package io.github.bobocodebreskul.context.exception;

public class RequestsMappingException extends RuntimeException {

  public RequestsMappingException(String msg, Throwable ex) {
    super(msg, ex);
  }
}
