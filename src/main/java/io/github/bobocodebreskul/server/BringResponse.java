package io.github.bobocodebreskul.server;

import static java.util.Objects.isNull;

import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.server.enums.ResponseStatus;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.slf4j.Logger;

/**
 * Extension of {@link BringHttpEntity} that adds HTTP response status. Could be used as return
 * value in {@code @RestController} methods.
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
 *
 * @param <T> body type
 */
public class BringResponse<T> extends BringHttpEntity<T> {

  private static final Logger log = LoggerFactory.getLogger(BringResponse.class);

  private ResponseStatus status;

  public BringResponse(T body) {
    super(body);
    this.status = ResponseStatus.OK;
  }

  public BringResponse(ResponseStatus status) {
    this(null, new HashMap<>(), status);
  }

  public BringResponse(@Nullable T body, @Nullable Map<String, String> headers,
      ResponseStatus status) {
    super(headers, body);
    if (isNull(status)) {
      log.error("HttpStatus must not be null");
      throw new IllegalArgumentException("HttpStatus must not be null");
    }
    this.status = status;
  }

  /**
   * Create a builder with specified status
   *
   * @param status HTTP response status
   * @return builder instance
   */
  public static ResponseBuilder status(ResponseStatus status) {
    return new ResponseBuilder(status);
  }

  /**
   * Create a builder with the status set to {@link ResponseStatus#OK OK}.
   *
   * @return the created builder
   */
  public static ResponseBuilder ok() {
    return new ResponseBuilder(ResponseStatus.OK);
  }

  /**
   * A shortcut for creation of a {@link BringResponse} with {@link ResponseStatus#OK OK}.
   *
   * @param body the response body.
   * @param <T>  body class type.
   * @return the created {@link BringResponse} object.
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

  /**
   * Builder class to simplify {@link BringResponse} object creation.
   */
  public static class ResponseBuilder<T> {

    private final ResponseStatus responseStatus;
    private Map<String, String> headers = new HashMap<>();

    ResponseBuilder(ResponseStatus status) {
      this.responseStatus = status;
    }

    /**
     * Set HTTP headers to builder objects.
     *
     * @param headers HTTP headers
     * @return current Builder object
     */
    public ResponseBuilder<T> headers(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }

    /**
     * Set HTTP entity body to builder and compose {@link BringResponse} object.
     *
     * @param body parameterized body
     * @return {@code BringResponse} object instance
     */
    public BringResponse<T> body(T body) {
      return new BringResponse<>(body, headers, responseStatus);
    }

    /**
     * Compose {@link BringResponse} with empty body.
     *
     * @return {@code BringResponse} object instance
     */
    public BringResponse<T> build() {
      return new BringResponse<>(null, headers, responseStatus);
    }
  }
}
