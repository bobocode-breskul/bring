package io.github.bobocodebreskul.server;

import static java.util.Objects.isNull;

import io.github.bobocodebreskul.server.enums.ResponseStatus;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents an HTTP response entity. Gives access to response headers, cookies, body and status.
 * Could be used as return value in {@code @RestController} methods.
 * <p>Example:</p>
 * <pre class="code">
 *   &#64;Get("/request")
 *   public BringResponse&lt;String&gt; handleRequest() {
 *     Map&lt;String, String&gt; headers = new HashMap&lt;&gt;();
 *     headers.put("Content-Type", "text/html");
 *     return BringResponse.status(ResponseStatus.CREATED)
 *       .headers(headers)
 *       .body("RESULT DATA");
 *   }
 * </pre>
 * @param <T> body type
 */
@Slf4j
public class BringResponse<T> extends BringHttpEntity<T> {

  private ResponseStatus status;

  public BringResponse(T body) {
    super(body);
    this.status = ResponseStatus.OK;
  }

  public BringResponse(ResponseStatus status) {
    this(null, new HashMap<>(), status);
  }

  public BringResponse(@Nullable T body, @Nullable Map<String, String> headers, ResponseStatus status) {
    super(headers, body);
    if (isNull(status)) {
      log.error("HttpStatus must not be null");
      throw new IllegalArgumentException("HttpStatus must not be null");
    }
    this.status = status;
  }

  /**
   * Create a builder with specified status
   * @param status HTTP response status
   * @return builder instance
   */
  public static ResponseBuilder status(ResponseStatus status) {
    return new ResponseBuilder(status);
  }

  /**
   * Create a builder with the status set to {@link ResponseStatus#OK OK}.
   * @return the created builder
   */
  public static ResponseBuilder ok() {
    return new ResponseBuilder(ResponseStatus.OK);
  }

  /**
   * A shortcut for creation of a {@link BringResponse} with {@link ResponseStatus#OK OK}.
   * @param body the response body.
   * @return the created {@link BringResponse} object.
   * @param <T> body class type.
   */
  public static <T> BringResponse<T> ok(T body) {
    return ok().body(body);
  }

  /**
   * Returns http response status.
   *
   * @return response status as enum object which contain http status code.
   */
  public ResponseStatus getStatus() {
    log.debug("Get status call");
    return status;
  }

  /**
   * Set http response status into response entity
   *
   * @param status enum object which contain http status code.
   */
  public void setStatus(ResponseStatus status) {
    log.debug("Set status call");
    this.status = status;
  }

  public static class ResponseBuilder {

    private final ResponseStatus responseStatus;
    private Map<String, String> headers;

    private ResponseBuilder(ResponseStatus status) {
      this.responseStatus = status;
    }

    public ResponseBuilder headers(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }

    public <T> BringResponse<T> body(T body) {
      return new BringResponse<>(body, headers, responseStatus);
    }

    public BringResponse<Void> build() {
      return new BringResponse<>(null, headers, responseStatus);
    }
  }
}
