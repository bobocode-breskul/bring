package io.github.bobocodebreskul.server.exception;

/**
 * Exception thrown when fail to map {@link  io.github.bobocodebreskul.server.BringResponse} into
 * {@link jakarta.servlet.http.HttpServletResponse} or map
 * {@link jakarta.servlet.http.HttpServletRequest} into
 * {@link io.github.bobocodebreskul.server.BringRequest}
 *
 * @see io.github.bobocodebreskul.server.HttpRequestMapper
 */
public class RequestsMappingException extends RuntimeException {

  public RequestsMappingException(String msg, Throwable ex) {
    super(msg, ex);
  }
}
