package io.github.bobocodebreskul.server;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.exception.BodyReadException;
import io.github.bobocodebreskul.context.exception.RequestsMappingException;
import io.github.bobocodebreskul.server.enums.RequestMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;

/**
 * Utility mapper class to convert {@code HttpServletRequest} into {@link BringRequest} and
 * {@code HttpServletResponse} into {@link BringResponse}.
 *
 * @see BringRequest
 * @see BringResponse
 */
public class HttpRequestMapper {

  private static final Logger log = LoggerFactory.getLogger(HttpRequestMapper.class);

  public static final String CONTENT_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";
  public static final String CONTENT_TYPE_TEXT_HTML = "text/html";
  public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
  public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
  public static final String CONTENT_TYPE_KEY = "Content-Type";

  private static final Set<Class<?>> PRIMITIVE_SET = Set.of(Boolean.class, Character.class,
      Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, String.class);
  private static final Set<String> ALLOWED_INPUT_CONTENT_TYPES = Set.of(CONTENT_TYPE_TEXT_HTML,
      CONTENT_TYPE_TEXT_PLAIN, CONTENT_TYPE_APPLICATION_JSON);

  private final ObjectMapper objectMapper = new ObjectMapper();

  // todo test BringResponse with all fields then we set correct values to HttpServletResponse
  // todo test HttpServletResponse throw IOException during body writing then throw RequestsMappingException
  // todo test when ObjectMapper#writeValueAsString(body) throw JsonProcessingException then throw RequestsMappingException

  /**
   * Convert {@code HttpServletResponse} into parameterized {@link BringResponse}
   *
   * @param httpServletResponse servlet response to write into
   * @param bringResponseEntity parameterized response entity to write out
   */
  public void writeBringResponseIntoHttpServletResponse(HttpServletResponse httpServletResponse,
      BringResponse<?> bringResponseEntity) {

    httpServletResponse.setStatus(bringResponseEntity.getStatus().getValue());

    bringResponseEntity.getHeadersNames()
        .forEach(headerName -> httpServletResponse.setHeader(headerName,
            bringResponseEntity.getHeader(headerName)));

    if (bringResponseEntity.getBody() != null) {
      writeResponseBody(bringResponseEntity, httpServletResponse);
    }
  }

  private void writeResponseBody(BringResponse<?> bringResponseEntity,
      HttpServletResponse httpServletResponse) {
    try {
      Object body = bringResponseEntity.getBody();
      String contentType;
      if (body instanceof byte[] byteBody) {
        contentType = CONTENT_TYPE_APPLICATION_OCTET_STREAM;
        httpServletResponse.getOutputStream().write(byteBody);
      } else if (PRIMITIVE_SET.contains(body.getClass())) {
        contentType = CONTENT_TYPE_TEXT_PLAIN;
        httpServletResponse.getOutputStream().print(writeBodyAsJson(body));
      } else {
        contentType = CONTENT_TYPE_APPLICATION_JSON;
        httpServletResponse.getOutputStream().print(writeBodyAsJson(body));
      }
      if (!bringResponseEntity.getHeadersNames().contains(CONTENT_TYPE_KEY)) {
        httpServletResponse.setHeader(CONTENT_TYPE_KEY, contentType);
      }
    } catch (IOException e) {
      log.error("Failed to write response entity to httpServletResponse", e);
      throw new RequestsMappingException("Failed to write response entity to httpServletResponse",
          e);
    }
  }

  // todo test when we fill all BringRequest fields from HttpServletRequest when body exists
  // todo test when we fill all BringRequest fields from HttpServletRequest when body not exists
  // todo test when objectMapper.readValue(stringBody, bodyType) then throws IOException

  /**
   * Convert {@code HttpServletRequest} into parameterized {@link BringRequest}
   *
   * @param httpServletRequest servlet request to convert
   * @param bodyType           expected body type class
   * @param <T>                expected body type
   * @return created bring request with specified body
   */
  public <T> BringRequest<T> mapHttpServletRequestOnBringRequestEntity(
      HttpServletRequest httpServletRequest, Class<T> bodyType) {

    RequestMethod method = RequestMethod.getByName(httpServletRequest.getMethod());
    Map<String, String> requestHeaders = extractHeaders(httpServletRequest);
    String contentType = requestHeaders.getOrDefault(CONTENT_TYPE_KEY.toLowerCase(), EMPTY);

    return BringRequest.method(method, URI.create(httpServletRequest.getRequestURI()))
        .headers(requestHeaders)
        .body(extractBody(httpServletRequest, contentType, bodyType));
  }

  private String writeBodyAsJson(Object body) {
    try {
      return objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      log.error("Failed to write body as String. ", e);
      throw new RequestsMappingException("Failed to write body as String. ", e);
    }
  }

  private <T> T extractBody(HttpServletRequest request, String contentType, Class<T> bodyType) {
    try {
      BufferedReader bodyReader = new BufferedReader(
        new InputStreamReader(request.getInputStream()));
      String stringBody = bodyReader.lines().toString();
      T body = null;
      if (!stringBody.isBlank()) {
        body = objectMapper.readValue(stringBody, bodyType);
      }
      return body;
    } catch (IOException e) {
      log.error("Failed to map HttpServletRequest body into object.", e);
      throw new RequestsMappingException("Failed to map HttpServletRequest body into object.", e);
    }
  }

  private Map<String, String> extractHeaders(HttpServletRequest request) {
    Map<String, String> headers = new HashMap<>();
    for (Iterator<String> it = request.getHeaderNames().asIterator(); it.hasNext(); ) {
      String headerName = it.next();
      String headerValue = request.getHeader(headerName);
      headers.put(headerName.toLowerCase(), headerValue);
    }
    return headers;
  }
}
