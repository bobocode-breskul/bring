package io.github.bobocodebreskul.server;

import static java.util.Objects.isNull;

import io.github.bobocodebreskul.server.enums.ResponseStatus;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/*
 * (done) body (get, set)
 * (done) status (get, set)
 * (in progress) headers()
 * cookies (done) builder
 * parameterized ResponseEntity з типом Body.
 * response.builder().setBody(body).setStatus(200).build();
 */

/**
 * todo write java doc
 * @param <T>
 */
@Slf4j
public class BringResponse<T> extends BringHttpEntity<T> {

  ResponseStatus status;

  public BringResponse(T body) {
    super(body);
    this.status = ResponseStatus.OK;
  }

  public BringResponse(ResponseStatus status) {
    this(null, new HashMap<>(), status);
  }

  public BringResponse(@Nullable T body, @Nullable Map<String, String> headers, ResponseStatus status) {
    super(isNull(headers) ? new HashMap<>() : headers, body);
    if (isNull(status)) {
      log.error("HttpStatus must not be null");
      throw new IllegalArgumentException("HttpStatus must not be null");
    }
    this.status = status;
  }

  public ResponseStatus getStatus() {
    log.debug("Get status call");
    return status;
  }

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

    // todo think about termination methods with other response statuses
    BringResponse<T> ok() {
      return new BringResponse<>(body, headers, ResponseStatus.OK);
    }
  }
}
