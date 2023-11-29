package io.github.bobocodebreskul.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.context.annotations.BringComponent;
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

public class HttpRequestMapper {

//    public BringResponse<T> mapHttpServletResponseOnBringResponseEntity(
//        HttpServletResponse httpServletResponse) {
//
//      return null;
//    }
  ObjectMapper objectMapper = new ObjectMapper();

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
      throw new RuntimeException(e);
    }
  }

  private URI extractURI(HttpServletRequest request) {
    try {
      String stringUri = request.getRequestURI();
      return new URI(stringUri);
    } catch (URISyntaxException e) {
      // todo update exception
      throw new RuntimeException(e);
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
