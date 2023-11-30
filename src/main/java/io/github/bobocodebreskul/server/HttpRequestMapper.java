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

  // todo test BringResponse with all fields then we set correct values to HttpServletResponse
  // todo test HttpServletResponse throw IOException during body writing then throw RequestsMappingException
  // todo test when ObjectMapper#writeValueAsString(body) throw JsonProcessingException then throw RequestsMappingException
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
        throw new RequestsMappingException("Failed to write response entity to httpServletResponse",
          e);
      }
    }
  }

  // todo test when we fill all BringRequest fields from HttpServletRequest when body exists
  // todo test when we fill all BringRequest fields from HttpServletRequest when body not exists
  // todo test when objectMapper.readValue(stringBody, bodyType) then throws IOException
  public <T> BringRequest<T> mapHttpServletRequestOnBringRequestEntity(
    HttpServletRequest httpServletRequest, Class<T> bodyType) {

    RequestMethod method = RequestMethod.getByName(httpServletRequest.getMethod());
    return BringRequest.method(method, URI.create(httpServletRequest.getRequestURI()))
      .headers(extractHeaders(httpServletRequest))
      .body(extractBody(httpServletRequest, bodyType));
  }

  private String writeBodyAsJson(Object body) {
    try {
      return objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      log.error("Failed to write body as String. ", e);
      throw new RequestsMappingException("Failed to write body as String. ", e);
    }
  }

  private <T> T extractBody(HttpServletRequest request, Class<T> bodyType) {
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
      headers.put(headerName, headerValue);
    }
    return headers;
  }
}
