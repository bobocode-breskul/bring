package io.github.bobocodebreskul.server;

import static java.util.Objects.isNull;

import io.github.bobocodebreskul.server.enums.ResponseStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * todo write java doc
 * @param <T>
 */
@Slf4j
public class BringResponse<T> {

  static final String COOKIE = "Cookie";
  static final String HEADER_NAME_SHOULD_NOT_BE_NULL = "Header name should not be null.";
  static final String HEADER_VALUE_SHOULD_NOT_BE_NULL = "Header value should not be null.";
  static final String COOKIE_NAME_SHOULD_NOT_BE_NULL = "Cookie name should not be null.";
  static final String COOKIE_VALUE_SHOULD_NOT_BE_NULL = "Cookie value should not be null.";

  private Map<String, String> headers = new HashMap<>();

  private final Map<String, String> cookies = new HashMap<>();
  private T body;
  private ResponseStatus status;

  public BringResponse(T body) {
    this.body = body;
    this.status = ResponseStatus.OK;
  }

  public BringResponse(ResponseStatus status) {
    this(null, new HashMap<>(), status);
  }

  public BringResponse(@Nullable T body, @Nullable Map<String, String> headers, ResponseStatus status) {
    this.headers = isNull(headers) ? new HashMap<>() : headers;
    this.body = body;
    if (isNull(status)) {
      log.error("HttpStatus must not be null");
      throw new IllegalArgumentException("HttpStatus must not be null");
    }
    this.status = status;
  }

  /**
   * Method adds header to {@link BringResponse}
   * @param headerName header name
   * @param headerValue header value
   */
  public void addHeader(String headerName, String headerValue) {
    log.info("Add header with name='{}' and value='{}'.", headerName, headerValue);
    if (isNull(headerName)) {
      log.error("Adding header failed. Header name should not be null.");
      throw new IllegalArgumentException(HEADER_NAME_SHOULD_NOT_BE_NULL);
    }
    if (isNull(headerValue)) {
      log.error("Adding header failed. Header value should not be null.");
      throw new IllegalArgumentException(HEADER_VALUE_SHOULD_NOT_BE_NULL);
    }
    this.headers.put(headerName.toLowerCase(), headerValue);
    log.debug("Header with name='{}' and value='{}' was added.", headerName, headerValue);
  }

  /**
   * Returns header value by name.
   * @param headerName header name.
   * @return header value.
   */
  public String getHeader(String headerName) {
    log.info("Get header for headerName='{}'.", headerName);
    if (this.headers.containsKey(headerName.toLowerCase())) {
      String headerValue = this.headers.get(headerName);
      log.info("Header value for header name '{}' is '{}'.", headerName, headerValue);
      return headerValue;
    }
    log.error("Header for name='{}' not found.", headerName);
    return "";
  }

  /**
   * Removes header from {@link BringResponse}
   * @param headerName header name.
   */
  public void removeHeader(String headerName) {
    log.info("Remove header for headerName='{}'.", headerName);
    if (isNull(headerName)) {
      log.error("Adding header failed. Header name should not be null.");
      throw new IllegalArgumentException(HEADER_NAME_SHOULD_NOT_BE_NULL);
    }
    if (this.headers.containsKey(headerName.toLowerCase())) {
      if (headerName.equalsIgnoreCase(COOKIE)) {
        cookies.clear();
      }
      String headerValue = headers.get(headerName.toLowerCase());
      this.headers.remove(headerName.toLowerCase());
      log.info("Header '{}: {}' was removed.", headerName, headerValue);
    }
    log.warn("Removing failed. Header for name='{}' not found.", headerName);
  }

  /**
   * Method for adding cookies to {@link BringResponse}
   * @param cookieName cookie name.
   * @param cookieValue cookie value.
   */
  public void addCookie(String cookieName, String cookieValue) {
    log.info("Add cookie with name='{}' and value='{}'.", cookieName, cookieValue);
    if (isNull(cookieName)) {
      log.error("Adding cookie failed. Cookie name should not be null.");
      throw new IllegalArgumentException(COOKIE_NAME_SHOULD_NOT_BE_NULL);
    }
    if (isNull(cookieValue)) {
      log.error("Adding cookie failed. Cookie value should not be null.");
      throw new IllegalArgumentException(COOKIE_VALUE_SHOULD_NOT_BE_NULL);
    }
    this.cookies.put(cookieName, cookieValue.toLowerCase());
    log.debug("Cookie with name='{}' and value='{}' was added.", cookieName, cookieValue);
    String cookiesAsString = getCookiesAsString();
    this.headers.put(COOKIE, cookiesAsString);
    log.debug("Cookies '{}' was added to headers.", cookiesAsString);
  }

  /**
   * Returns cookie value from {@link BringResponse}.
   * @param cookieName cookie name.
   * @return cookie value.
   */
  public String getCookie(String cookieName) {
    log.info("Get cookie for cookieName='{}'.", cookieName);
    if (this.cookies.containsKey(cookieName.toLowerCase())) {
      String cookieValue = this.cookies.get(cookieName.toLowerCase());
      log.info("Cookie value for cookie name '{}' is '{}'.", cookieName, cookieValue);
      return cookieValue;
    }
    log.warn("Cookie for name='{}' not found.", cookieName);
    return "";
  }

  /**
   * Removes cookie from {@link BringResponse} by name.
   * @param cookieName removed cookie name.
   */
  public void removeCookie(String cookieName) {
    log.info("Remove cookie for cookieName='{}'.", cookieName);
    if (isNull(cookieName)) {
      log.error("Adding cookie failed. Cookie name should not be null.");
      throw new IllegalArgumentException(COOKIE_NAME_SHOULD_NOT_BE_NULL);
    }
    if (this.cookies.containsKey(cookieName)) {
      String cookieValue = cookies.get(cookieName);
      this.cookies.remove(cookieName);
      String cookiesAsString = getCookiesAsString();
      this.headers.put(COOKIE, cookiesAsString);
      log.info("Cookie '{}={}' was removed.", cookieName, cookieValue);
    }
    log.warn("Removing failed. Cookie for name='{}' not found.", cookieName);
  }

  private String getCookiesAsString() {
    List<String> pairs = this.cookies.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .toList();
    return String.join("; ", pairs);
  }

  /**
   * Return Response body.
   *
   * @return response body as object.
   */
  public T getBody() {
    log.debug("Get body call");
    return body;
  }

  /**
   * Set parametrized object as body into BringResponse.
   *
   * @param body parametrized object of response.
   */
  public void setBody(T body) {
    log.debug("Set body call");
    this.body = body;
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

  static class ResponseBuilder<T> {
    Map<String, String> headers;
    T body;

    public ResponseBuilder<T> headers(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }

    public ResponseBuilder<T> body(T body) {
      this.body = body;
      return this;
    }

    public BringResponse<T> status(ResponseStatus status) {
      return new BringResponse<>(body, headers, status);
    }

    public <T> BringResponse<T> body(T body) {
      return new BringResponse<>(body, headers, responseStatus);
    }

    public BringResponse<Void> build() {
      return new BringResponse<>(null, headers, responseStatus);
    }
  }
}
