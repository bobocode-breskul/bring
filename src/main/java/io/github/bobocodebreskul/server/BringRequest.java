package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.server.enums.RequestMethod;
import java.net.URI;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

// TODO: tests
/**
 * Extension of {@link BringHttpEntity} that adds HTTP method and target URL. Could be used as
 * method parameter in {@code @RestController} classes.
 * <p>Example:</p>
 * <pre class="code">
 *   &#64;Post("/request")
 *   public void handleRequest(BringRequest&lt;String&lt; request) {
 *     RequestMethod method = request.getRequestMethod();
 *     URI url = request.getUrl();
 *     String body = request.getBody();
 *   }
 * </pre>
 * @see BringHttpEntity
 * @param <T> body type
 */
@Slf4j
public class BringRequest<T> extends BringHttpEntity<T>{

  private final RequestMethod requestMethod;
  private final URI url;
  
  public BringRequest(RequestMethod requestMethod, URI url) {
    this(requestMethod, url, null);
  }

  public BringRequest(RequestMethod requestMethod, URI url, T body) {
    super(body);
    this.requestMethod = requestMethod;
    this.url = url;
  }

  /**
   * Create and return {@link BringRequest} builder for specified HTTP {@link RequestMethod}
   * and URL
   * @return builder instance
   */
  public static Builder method(RequestMethod method, URI url) {
    return new Builder(method, url);
  }

  /**
   * Get request HTTP method
   * @return request HTTP method as {@link RequestMethod} enum value
   */
  public RequestMethod getRequestMethod() {
    log.debug("Get request method call");
    return requestMethod;
  }

  /**
   * Get request target URL
   * @return request URL as {@link URI} object
   */
  public URI getUrl() {
    log.debug("Get uri method call");
    return url;
  }

  /**
   * Builder class to simplify {@link BringRequest} object creation.
   */
  public static class Builder {

    private final RequestMethod requestMethod;
    private final URI uri;
    private Map<String, String> headers;

    private Builder(RequestMethod requestMethod, URI uri) {
      this.requestMethod = requestMethod;
      this.uri = uri;
    }

    public Builder headers(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }

    public <T> BringRequest<T> body(T body) {
      return create(body);
    }

    public BringRequest<Void> build() {
      return create(null);
    }

    private <T> BringRequest<T> create(T body) {
      BringRequest<T> tBringRequest = new BringRequest<>(requestMethod, uri, body);
      if (headers != null) {
        headers.forEach(tBringRequest::addHeader);
      }
      return tBringRequest;
    }
  }
}
