package io.github.bobocodebreskul.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.exception.RequestsMappingException;
import io.github.bobocodebreskul.server.enums.RequestMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpRequestMapper {
  ObjectMapper objectMapper = new ObjectMapper();

  public <T> void writeBringResponseIntoHttpServletResponse(
    BringResponse<T> bringResponseEntity, HttpServletResponse httpServletResponse) {

    httpServletResponse.setStatus(bringResponseEntity.getStatus().getValue());

    bringResponseEntity.getHeadersNames()
      .forEach(headerName -> httpServletResponse.setHeader(headerName,
        bringResponseEntity.getHeader(headerName)));

    if (bringResponseEntity.getBody() != null) {
      try {
        httpServletResponse.getOutputStream().print(writeBodyAsJson(bringResponseEntity.getBody()));
      } catch (IOException e) {
        log.error("Failed to write response entity to httpServletResponse", e);
        throw new RequestsMappingException("Failed to write response entity to httpServletResponse", e);
      }
    }
  }

  private String writeBodyAsJson(Object body) {
    try {
      return objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      log.error("Failed to write body as String. ", e);
      throw new RequestsMappingException("Failed to write body as String. ", e);
    }
  }


  public <T> BringRequest<T> mapHttpServletRequestOnBringRequestEntity(
      HttpServletRequest httpServletRequest, Class<T> bodyType) {

      RequestMethod method = RequestMethod.getByName(httpServletRequest.getMethod());
      // TODO: handle void type, empty input (for example GET)
      return BringRequest.method(method)
          .uri(extractURI(httpServletRequest))
          .headers(extractHeaders(httpServletRequest))
          .body(extractBody(httpServletRequest, bodyType));

  }

  private <T> T extractBody(HttpServletRequest request, Class<T> bodyType) {
    try {
      BufferedReader bodyReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
      String stringBody = bodyReader.lines().toString();
      T body = null;
      if (!stringBody.isBlank()) {
        body = objectMapper.readValue(stringBody, bodyType);
      }
      return body;
    } catch (IOException e) {
      // todo update exception
      log.error("Failed to map HttpServletRequest body into object.", e);
      throw new RequestsMappingException("Failed to map HttpServletRequest body into object.", e);
    }
  }

  private URI extractURI(HttpServletRequest request) {
    try {
      String stringUri = request.getRequestURI();
      return new URI(stringUri);
    } catch (URISyntaxException e) {
      log.error("Failed to map HttpServletRequest body into object.", e);
      throw new RequestsMappingException("Failed to map HttpServletRequest body into object.", e);
    }
  }

  private Map<String, String> extractHeaders(HttpServletRequest request) {
    Map<String, String> headers = new HashMap<>();
    for (Iterator<String> it = request.getHeaderNames().asIterator(); it.hasNext(); ) {
      String headerName = it.next();
      String headerValue = request.getHeader(headerName);
      headers.put(headerName, headerValue);
    }
    return headers;
  }

}
