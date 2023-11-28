package io.github.bobocodebreskul.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * (done) body (get, set)
 * (done) status (get, set)
 * (in progress) headers()
 * cookies (done) builder
 * parameterized ResponseEntity з типом Body.
 * response.builder().setBody(body).setStatus(200).build();
 */

@Slf4j
public class BringResponse<T> {


  private final Map<String, String> headers = new HashMap<>();

  private final Map<String, String> cookies = new HashMap<>();
  T body;
  Integer status;

  public BringResponse(T body) {
    this.body = body;
  }

  public void addHeader(String headerName, String headerValue) {
    log.info("Add header with name='{}' and value='{}'.", headerName, headerValue);
    if (Objects.isNull(headerName)) {
      log.error("Adding header failed. Header name should not be null.");
      throw new IllegalArgumentException("Header key should not be null.");
    }
    if (Objects.isNull(headerValue)) {
      log.error("Adding header failed. Header value should not be null.");
      throw new IllegalArgumentException("Header value should not be null.");
    }
    this.headers.put(headerName, headerValue);
    log.debug("Header with name='{}' and value='{}' was added.", headerName, headerValue);
  }

  public String getHeader(String headerName) {
    log.info("Get header for headerName='{}'.", headerName);
    if (this.headers.containsKey(headerName)) {
      String headerValue = this.headers.get(headerName);
      log.info("Header value for header name '{}' is '{}'.", headerName, headerValue);
      return headerValue;
    }
    log.error("Header for name='{}' not found.", headerName);
    return "";
  }

  public T getBody() {
    log.debug("Get body call");
    return body;
  }

  public void setBody(T body) {
    log.debug("Set body call");
    this.body = body;
  }

  public Integer getStatus() {
    log.debug("Get status call");
    return status;
  }

  public void setStatus(Integer status) {
    log.debug("Set status call");
    this.status = status;
  }
}
