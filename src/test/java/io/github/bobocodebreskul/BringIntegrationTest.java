package io.github.bobocodebreskul;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.context.registry.BringContainer;
import io.github.bobocodebreskul.demointegration.DemoApp;
import io.github.bobocodebreskul.demointegration.controller.RequestDto;
import io.github.bobocodebreskul.demointegration.controller.UnsupportedRequestParamTest;
import io.github.bobocodebreskul.server.TomcatServer;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BringIntegrationTest {

  public static final String URL = "http://localhost:7777/url";
  private HttpClient httpClient;
  private ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {
    httpClient = HttpClient.newHttpClient();
    objectMapper = new ObjectMapper();
  }

  @BeforeAll
  static void configure() throws InterruptedException {

    BringContainer.run(DemoApp.class);

    while (!TomcatServer.getStatus().equals("STARTED")) {
      Thread.sleep(100);
    }
  }

  @AfterAll
  static void tearDown() {
    TomcatServer.stop();
  }

  @Test
  @DisplayName("Test application start with controller check 'get' method founded and triggered")
  void given_RanApplication_when_ControllerWithoutPathRegistered_then_returnBodyWithStatus200ForGetMethod()
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(URL))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("\"BaseController Get Method\"" + System.lineSeparator());
  }


  @Test
  @DisplayName("Test application start with controller check 'post' method founded and triggered")
  void given_RanApplication_when_ControllerWithoutPathRegistered_then_returnBodyWithStatus200ForPostMethod()
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .POST(BodyPublishers.noBody())
        .uri(URI.create(URL))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo(
        "\"BaseController Post Method\"" + System.lineSeparator());
  }

  @Test
  @DisplayName("Test application start with controller check 'put' method founded and triggered")
  void given_RanApplication_when_ControllerWithoutPathRegistered_then_returnBodyWithStatus200ForPutMethod()
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .PUT(BodyPublishers.noBody())
        .uri(URI.create(URL))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("\"BaseController Put Method\"" + System.lineSeparator());
  }

  @Test
  @DisplayName("Test application start with controller check 'delete' method founded and triggered")
  void given_RanApplication_when_ControllerWithoutPathRegistered_then_returnBodyWithStatus200ForDeleteMethod()
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .DELETE()
        .uri(URI.create(URL))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo(
        "\"BaseController Delete Method\"" + System.lineSeparator());
  }

  @Test
  @DisplayName("Test application start with controller check 'head' method founded and triggered")
  void given_RanApplication_when_ControllerWithoutPathRegistered_then_returnBodyWithStatus200ForHeadMethod()
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .method("HEAD", HttpRequest.BodyPublishers.noBody())
        .uri(URI.create(URL))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  @DisplayName("Test application start with controller check 'post' method with request body parameter founded and triggered")
  void given_RanApplication_when_ControllerWithoutPathRegisteredAndPostMethodWithRequestBody_then_returnBodyWithStatus200ForPostMethod()
      throws IOException, InterruptedException {
    String url = URL + "/withRequestBody";
    RequestDto requestBody = new RequestDto();
    requestBody.setInteger(10);
    requestBody.setString("String");
    HttpRequest request = HttpRequest.newBuilder()
        .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo(
        "\"%s%s\"%n".formatted(requestBody.getString(), requestBody.getInteger()));
  }

  @Test
  @DisplayName("Test application start with controller check 'post' method with wrong request body parameter founded and triggered and failed")
  void given_RanApplication_when_ControllerWithoutPathRegisteredAndPostMethodWithWrongRequestBody_then_returnBodyWithStatus500ForPostMethod()
      throws IOException, InterruptedException {
    String url = URL + "/withRequestBody";
    HttpRequest request = HttpRequest.newBuilder()
        .POST(BodyPublishers.ofString("dummyBody"))
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
  }

  @Test
  @DisplayName("Test application start with controller check 'get' method with request body parameter founded and triggered and failed")
  void given_RanApplication_when_ControllerWithoutPathRegisteredAndGetMethodWithRequestBody_then_returnBodyWithStatus500ForGetMethod()
      throws IOException, InterruptedException {
    String url = URL + "/withRequestBody";
    RequestDto requestBody = new RequestDto();
    requestBody.setInteger(10);
    requestBody.setString("String");
    HttpRequest request = HttpRequest.newBuilder()
        .method("GET", BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
  }

  @Test
  @DisplayName("Test application start with controller check 'delete' method with request body parameter founded and triggered and failed")
  void given_RanApplication_when_ControllerWithoutPathRegisteredAndDeleteMethodWithRequestBody_then_returnBodyWithStatus500ForGetMethod()
      throws IOException, InterruptedException {
    String url = URL + "/withRequestBody";
    RequestDto requestBody = new RequestDto();
    requestBody.setInteger(10);
    requestBody.setString("String");
    HttpRequest request = HttpRequest.newBuilder()
        .method("DELETE", BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
  }

  @Test
  @DisplayName("Test application start with controller check 'head' method with request body parameter founded and triggered and failed")
  void given_RanApplication_when_ControllerWithoutPathRegisteredAndHeadMethodWithRequestBody_then_returnBodyWithStatus500ForGetMethod()
      throws IOException, InterruptedException {
    String url = URL + "/withRequestBody";
    RequestDto requestBody = new RequestDto();
    requestBody.setInteger(10);
    requestBody.setString("String");
    HttpRequest request = HttpRequest.newBuilder()
        .method("HEAD", BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
  }

  @Test
  @DisplayName("Test application start with controller get method throws runtime exception and with configured exception handler with 1 argument RuntimeException ex should return correct body")
  void given_RanApplication_when_ControllerGetMethodThrowsRuntimeException_then_returnBodyWithStatus500ForGetMethodAndCorrectBody()
      throws IOException, InterruptedException {
    String url = "http://localhost:7777/error/runtime";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
    assertThat(response.body()).contains("RuntimeException");
  }

  @Test
  @DisplayName("Test application start with controller get method throws IllegalArgumentException and with configured exception handler with 2 argument IllegalArgumentException ex, HttpServletRequest req should return correct body")
  void given_RanApplication_when_ControllerGetMethodThrowsIllegalArgumentException_then_returnBodyWithStatus500ForGetMethodAndCorrectBody()
      throws IOException, InterruptedException {
    String url = "http://localhost:7777/error/illegalargument";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
    assertThat(response.body()).contains("IllegalArgumentException and HttpServletRequest");
  }

  @Test
  @DisplayName("Test application start with controller get method throws DuplicatePathException and with configured exception handler with 2 argument DuplicatePathException ex, HttpServletRequest req should not return body")
  void given_RanApplication_when_ControllerGetMethodThrowsDuplicatePathException_then_returnBodyWithStatus500ForGetMethodAndCorrectBody()
      throws IOException, InterruptedException {
    String url = "http://localhost:7777/error/duplicate";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
    assertThat(response.body()).isEmpty();
  }

  @Test
  @DisplayName("Test application start with controller get method throws AmbiguousHttpAnnotationException and with configured exception handler with 2 argument HttpServletRequest req, AmbiguousHttpAnnotationException ex should return correct body")
  void given_RanApplication_when_ControllerGetMethodThrowsAmbiguousHttpAnnotationException_then_returnBodyWithStatus500ForGetMethodAndCorrectBody()
      throws IOException, InterruptedException {
    String url = "http://localhost:7777/error/ambiguos";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
    assertThat(response.body()).contains("HttpServletRequest and AmbiguousHttpAnnotationException");
  }

  @Test
  @DisplayName("Test application start with controller check 'post' method with request body parameter founded and triggered")
  void given_RanApplication_when_ControllerWithoutPathRegisteredAndPostMethodWithRequestEntity_then_returnBodyWithStatus200ForPostMethod()
      throws IOException, InterruptedException {
    String url = URL + "/withBringRequest";
    RequestDto requestBody = new RequestDto();
    requestBody.setInteger(10);
    requestBody.setString("String");
    HttpRequest request = HttpRequest.newBuilder()
        .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("\"%s%s\"%n".formatted(requestBody.getString(), requestBody.getInteger()));
  }

  @Test
  @DisplayName("Test application start with controller get method throws PropertyNotFoundException and with configured exception handler with 1 argument PropertyNotFoundException ex should return correct body and status")
  void given_RanApplication_when_ControllerGetMethodThrowsPropertyNotFoundException_then_returnBodyWithStatus502ForGetMethodAndCorrectBody()
      throws IOException, InterruptedException {
    String url = "http://localhost:7777/error/property";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(502);
    assertThat(response.body()).contains("Hello from BringResponse");
    assertThat(response.headers().map()).isNotEmpty().containsKey("TestHeader");
    assertThat(response.headers().map().get("TestHeader"))
        .hasSize(1)
        .contains("TestValue");
  }

  @Test
  @DisplayName("When endpoint with request parameter of type String then it is processed and returned")
  void given_EndpointWithStringRequestParameter_When_GetEndpoint_Then_ReturnParameterValue()
      throws IOException, InterruptedException {
    String url = "http://localhost:8080/test?test=test";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("\"test\"".concat(System.lineSeparator()));
  }

  @Test
  @DisplayName("When endpoint with request parameter of primitive type then it is processed and returned")
  void given_EndpointWithPrimitiveRequestParameter_When_GetEndpoint_Then_ReturnParameterValue()
      throws IOException, InterruptedException {
    String url = "http://localhost:8080/test/primitive?test=11";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("11".concat(System.lineSeparator()));
  }

  @Test
  @DisplayName("When endpoint with request parameter of wrapper type then it is processed and returned")
  void given_EndpointWithWrapperRequestParameter_When_GetEndpoint_Then_ReturnParameterValue()
      throws IOException, InterruptedException {
    String url = "http://localhost:8080/test/wrapper?test=11";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("11".concat(System.lineSeparator()));
  }

  @Test
  @DisplayName("When endpoint with request parameter and no parameter in request then null returned")
  void given_EndpointWithPrimitiveRequestParameterAndNoParameterInRequest_When_GetEndpoint_Then_ReturnNull()
      throws IOException, InterruptedException {
    String url = "http://localhost:8080/test";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("null".concat(System.lineSeparator()));
  }


  @Test
  @DisplayName("When endpoint with request parameter of non string/primitive/wrapper type then throw exception")
  void given_EndpointWithUnsupportedRequestParameter_When_GetEndpoint_Then_ReturnNull()
      throws IOException, InterruptedException {
    String url = "http://localhost:8080/test/exception?test=test";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
    assertThat(response.body()).isEqualTo(
        "\"Error processing 'testParam' method parameter with type '%s'.\"".formatted(
            UnsupportedRequestParamTest.class).concat(System.lineSeparator()));
  }

  @Test
  @DisplayName("When endpoint with request parameter of primitive type and string value specified in request then throw exception")
  void given_EndpointWithNonConvertibleRequestParameter_When_GetEndpoint_Then_ReturnNull() throws IOException, InterruptedException {
    String url = "http://localhost:8080/test/primitive?test=test";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
    assertThat(response.body()).isEqualTo(
        "\"Error processing 'testParam' method parameter with type 'int'.\"".concat(
            System.lineSeparator()));
  }
}
