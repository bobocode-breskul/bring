package io.github.bobocodebreskul;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.context.registry.BringContainer;
import io.github.bobocodebreskul.demointegration.DemoApp;
import io.github.bobocodebreskul.demointegration.controller.BaseController;
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

  public static final String BASE_URL = "http://localhost:7777";
  public static final String CONTROLLER_PATH = "/url";
  private HttpClient httpClient;
  private ObjectMapper objectMapper;
  private static BringContainer container;

  @BeforeEach
  public void setUp() {
    httpClient = HttpClient.newHttpClient();
    objectMapper = new ObjectMapper();
  }

  @BeforeAll
  static void configure() throws InterruptedException {

    container = BringContainer.run(DemoApp.class);

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
        .uri(URI.create(BASE_URL + CONTROLLER_PATH))
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
        .uri(URI.create(BASE_URL + CONTROLLER_PATH))
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
        .uri(URI.create(BASE_URL + CONTROLLER_PATH))
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
        .uri(URI.create(BASE_URL + CONTROLLER_PATH))
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
        .uri(URI.create(BASE_URL + CONTROLLER_PATH))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  @DisplayName("Test application start with controller check 'post' method with request body parameter founded and triggered")
  void given_RanApplication_when_ControllerWithoutPathRegisteredAndPostMethodWithRequestBody_then_returnBodyWithStatus200ForPostMethod()
      throws IOException, InterruptedException {
    String url = BASE_URL + CONTROLLER_PATH + "/withRequestBody";
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
  @DisplayName("Test application start with controller check 'post' method with request body parameter founded and triggered")
  void given_RanApplication_when_RequestWithWrongContentType_then_returnBodyWithStatus500ForPostMethod()
      throws IOException, InterruptedException {
    String url = BASE_URL + CONTROLLER_PATH + "/withBringRequest";
    RequestDto requestBody = new RequestDto();
    requestBody.setInteger(10);
    requestBody.setString("String");
    HttpRequest request = HttpRequest.newBuilder()
        .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
        .header("content-type", "application/xml")
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
  }

  @Test
  @DisplayName("Test application start with controller check 'post' method with wrong request body parameter founded and triggered and failed")
  void given_RanApplication_when_ControllerWithoutPathRegisteredAndPostMethodWithWrongRequestBody_then_returnBodyWithStatus500ForPostMethod()
      throws IOException, InterruptedException {
    String url = BASE_URL + CONTROLLER_PATH + "/withRequestBody";
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
    String url = BASE_URL + CONTROLLER_PATH + "/withRequestBody";
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
    String url = BASE_URL + CONTROLLER_PATH + "/withRequestBody";
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
    String url = BASE_URL + CONTROLLER_PATH + "/withRequestBody";
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
    String url = BASE_URL +"/error/runtime";
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
    String url = BASE_URL +"/error/illegalargument";
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
    String url = BASE_URL +"/error/duplicate";
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
    String url = BASE_URL +"/error/ambiguos";
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
    String url = BASE_URL + CONTROLLER_PATH + "/withBringRequest";
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
    String url = BASE_URL +"/error/property";
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
    String url = BASE_URL +"/test?test=test";
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
    String url = BASE_URL +"/test/primitive?test=11";
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
    String url = BASE_URL +"/test/wrapper?test=11";
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
    String url = BASE_URL +"/test";
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
    String url = BASE_URL +"/test/exception?test=test";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
    assertThat(response.body()).isEqualTo(
        "\"Error processing 'testParam' method parameter with type '%s', due to Error reading request parameter of type [%s]. String and primitive/wrappers allowed only\"".formatted(
            UnsupportedRequestParamTest.class, UnsupportedRequestParamTest.class).concat(System.lineSeparator()));
  }

  @Test
  @DisplayName("When endpoint with request parameter of primitive type and string value specified in request then throw exception")
  void given_EndpointWithNonConvertibleRequestParameter_When_GetEndpoint_Then_ReturnNull() throws IOException, InterruptedException {
    String url = BASE_URL +"/test/primitive?test=test";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(500);
    assertThat(response.body()).isEqualTo(
        "\"Error processing 'testParam' method parameter with type 'int', due to Failed to convert value of type 'java.lang.String' to required type 'int' for input string: [\\\"test\\\"]\"".concat(
            System.lineSeparator()));
  }

  @Test
  @DisplayName("When bean injected to controller method return bean value")
  void given_EndpointSimpleGetMethod_When_BeanInjectedToController_Then_ReturnInjectedBeanValue()
      throws IOException, InterruptedException {
    String url = BASE_URL + CONTROLLER_PATH +"/getConfigBean";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("\"I config bean\"" + System.lineSeparator());
  }

  @Test
  @DisplayName("When do request on not registered path throw exception")
  void given_NotRegisterdEndpoint_When_wrongCall_Then_Return404()
      throws IOException, InterruptedException {
    String url = BASE_URL + CONTROLLER_PATH +"/imposblePath";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(404);
    assertThat(response.body()).isEqualTo("\"Page not found!\"" + System.lineSeparator());
  }
  @Test
  @DisplayName("When bean injected to controller method which return bring response return bean value")
  void given_EndpointWithBringResponse_When_MethodRegistered_Then_ReturnResult()
      throws IOException, InterruptedException {
    String url = BASE_URL + CONTROLLER_PATH +"/getBringResponse";
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("\"I config bean\"");
  }

  @Test
  @DisplayName("Check get bean by name return dame value as get bean class")
  void given_registeredBean_When_GetByNameAndGetByClass_Then_ReturnSameInstance() {
    Object beanByName = container.getBean("yourController");
    BaseController beanByClass = container.getBean(BaseController.class);

    assertThat(beanByName).isEqualTo(beanByClass);
  }
}
