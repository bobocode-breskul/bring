package io.github.bobocodebreskul.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import io.github.bobocodebreskul.server.enums.RequestMethod;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
class BringRequestTest {

  private static final String TEST_URL = "127.0.0.1";
  private static final String TEST_STRING_BODY = "TEST_BODY";
  private static final String TEST_HEADER_KEY_1 = "TEST_HEADER_KEY_1";
  private static final String TEST_HEADER_KEY_2 = "TEST_HEADER_KEY_2";
  private static final String TEST_HEADER_KEY_3 = "TEST_HEADER_KEY_3";
  private static final String TEST_HEADER_VALUE_1 = "TEST_HEADER_VALUE_1";
  private static final String TEST_HEADER_VALUE_2 = "TEST_HEADER_VALUE_2";
  private static final String TEST_HEADER_VALUE_3 = "TEST_HEADER_VALUE_3";

  @Order(1)
  @Test
  @DisplayName("Create empty BringRequest by constructor with request method and URL")
  void givenRequestMethodAndInputUri_WhenCreateBringRequest_ThenValidVoidResult() {
    // data
    RequestMethod inputRequestMethod = RequestMethod.GET;
    URI inputUri = URI.create(TEST_URL);
    // given
    // when
    var actualRequest = new BringRequest<>(inputRequestMethod, inputUri);
    // then
    assertThat(actualRequest.getRequestMethod()).isEqualTo(inputRequestMethod);
    assertThat(actualRequest.getUrl()).isEqualTo(inputUri);
    assertThat(actualRequest.getBody()).isNull();
  }

  @Order(2)
  @Test
  @DisplayName("Create parameterized BringRequest by constructor with request method, URL and body")
  void givenRequestMethodAndInputUriAndBody_WhenCreateBringRequest_ThenValidParameterizedResult() {
    // data
    RequestMethod inputRequestMethod = RequestMethod.GET;
    URI inputUri = URI.create(TEST_URL);
    String inputBody = TEST_STRING_BODY;
    // given
    // when
    var actualRequest = new BringRequest<>(inputRequestMethod, inputUri, inputBody);
    // then
    assertThat(actualRequest.getRequestMethod()).isEqualTo(inputRequestMethod);
    assertThat(actualRequest.getUrl()).isEqualTo(inputUri);
    assertThat(actualRequest.getBody()).isEqualTo(inputBody);
  }

  @Order(3)
  @Test
  @DisplayName("Create empty BringRequest using builder with request method and URL")
  void givenRequestMethodAndInputUri_WhenCreateBringRequestWithBuilder_ThenValidVoidResult() {
    // data
    RequestMethod inputRequestMethod = RequestMethod.GET;
    URI inputUri = URI.create(TEST_URL);
    // given
    // when
    var actualRequest = BringRequest.method(inputRequestMethod, inputUri)
        .build();
    // then
    assertThat(actualRequest.getRequestMethod()).isEqualTo(inputRequestMethod);
    assertThat(actualRequest.getUrl()).isEqualTo(inputUri);
    assertThat(actualRequest.getBody()).isNull();
  }

  @Order(4)
  @Test
  @DisplayName("Create parameterized BringRequest using builder with request method, URL and body")
  void givenRequestMethodAndInputUriAndBody_WhenCreateBringRequestWithBuilder_ThenValidParameterizedResult() {
    // data
    RequestMethod inputRequestMethod = RequestMethod.GET;
    URI inputUri = URI.create(TEST_URL);
    String inputBody = TEST_STRING_BODY;
    // given
    // when
    var actualRequest = BringRequest.method(inputRequestMethod, inputUri)
        .body(inputBody);
    // then
    assertThat(actualRequest.getRequestMethod()).isEqualTo(inputRequestMethod);
    assertThat(actualRequest.getUrl()).isEqualTo(inputUri);
    assertThat(actualRequest.getBody()).isEqualTo(inputBody);
  }

  @Test
  @DisplayName("Add headers to BringRequest when it is created with builder without exceptions")
  void givenRequestMethodAndUriAndHeaders_WhenCreateBringRequestWithBuilder_ThenHeadersCouldBeModified() {
    // data
    RequestMethod inputRequestMethod = RequestMethod.GET;
    URI inputUri = URI.create(TEST_URL);
    Map<String, String> headers = Map.of(
        TEST_HEADER_KEY_1, TEST_HEADER_VALUE_1,
        TEST_HEADER_KEY_2, TEST_HEADER_VALUE_2
    );
    // given
    // when
    BringRequest<Void> actualRequest = BringRequest.method(inputRequestMethod, inputUri)
        .headers(headers)
        .build();
    // then
    assertThatNoException().isThrownBy(
        () -> actualRequest.addHeader(TEST_HEADER_KEY_3, TEST_HEADER_VALUE_3));
    assertThat(actualRequest.getHeader(TEST_HEADER_KEY_1))
        .isEqualTo(TEST_HEADER_VALUE_1);
    assertThat(actualRequest.getHeader(TEST_HEADER_KEY_3))
        .isEqualTo(TEST_HEADER_VALUE_3);
    assertThat(actualRequest.getHeader(TEST_HEADER_KEY_2))
        .isEqualTo(TEST_HEADER_VALUE_2);
  }
}
