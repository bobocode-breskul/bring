package io.github.bobocodebreskul.context.exception;

/**
 * Throw to indicate issues with mapping {@code HttpServletRequest} or {@code HttpServletResponse}
 * into corresponding Bring HTTP wrappers
 */
public class RequestsMappingException extends RuntimeException {

  public RequestsMappingException(String msg, Throwable ex) {
    super(msg, ex);
  }
}
