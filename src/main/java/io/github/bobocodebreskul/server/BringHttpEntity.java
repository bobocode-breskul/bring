package io.github.bobocodebreskul.server;

import static java.util.Objects.isNull;

import io.github.bobocodebreskul.config.LoggerFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;

/**
 * Representation of HTTP request or response, containing headers, cookies and body. Use its
 * extension classes in your implementation.
 *
 * @param <T> body type
 * @see BringRequest
 * @see BringResponse
 */
class BringHttpEntity<T> {

  private static final Logger log = LoggerFactory.getLogger(BringHttpEntity.class);

  static final String COOKIE = "Cookie";
  static final String HEADER_NAME_SHOULD_NOT_BE_NULL = "Header name should not be null.";
  static final String HEADER_VALUE_SHOULD_NOT_BE_NULL = "Header value should not be null.";
  static final String COOKIE_NAME_SHOULD_NOT_BE_NULL = "Cookie name should not be null.";
  static final String COOKIE_VALUE_SHOULD_NOT_BE_NULL = "Cookie value should not be null.";

  private final Map<String, String> headers = new HashMap<>();

  private final Map<String, String> cookies = new HashMap<>();

  private T body;

  public BringHttpEntity(T body) {
    this.body = body;
  }

  public BringHttpEntity(Map<String, String> headers, T body) {
    if (!isNull(headers)) {
      headers.forEach((key, value) -> this.headers.put(key.toLowerCase(), value));
    }
    this.body = body;
  }

  /**
   * Method adds header to {@link BringResponse}
   *
   * @param headerName  header name
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
   *
   * @param headerName header name.
   * @return header value.
   */
  public String getHeader(String headerName) {
    String lowerCaseHeaderName = headerName.toLowerCase();
    log.info("Get header for headerName='{}'.", headerName);
    if (this.headers.containsKey(lowerCaseHeaderName)) {
      String headerValue = this.headers.get(lowerCaseHeaderName);
      log.info("Header value for header name '{}' is '{}'.", headerName, headerValue);
      return headerValue;
    }
    log.warn("Header for name='{}' not found.", headerName);
    return "";
  }

  /**
   * Get all set header names
   *
   * @return header names as Set collection
   */
  public Set<String> getHeadersNames() {
    return new HashSet<>(this.headers.keySet());
  }

  /**
   * Removes header from {@link BringResponse}
   *
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
   *
   * @param cookieName  cookie name.
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
   *
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
   *
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

  /**
   * Returns all headers.
   *
   * @return Map of header and value.
   */
  public Map<String, String> getAllHeaders() {
    return new HashMap<>(this.headers);
  }

  private String getCookiesAsString() {
    List<String> pairs = this.cookies.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .toList();
    return String.join("; ", pairs);
  }

  /**
   * Get HTTP entity as parameterized body
   *
   * @return parameterized body
   */
  public T getBody() {
    log.debug("Get body call");
    return body;
  }

  /**
   * Set HTTP entity parameterized body
   *
   * @param body parameterized body
   */
  public void setBody(T body) {
    log.debug("Set body call");
    this.body = body;
  }
}
