package io.github.bobocodebreskul.server;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.bobocodebreskul.server.enums.RequestMethod;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class BringRequestTest {

  private static final String TEST_URL = "127.0.0.1";

  @Order(1)
  @Test
  @DisplayName("Create empty BringRequest by constructor with request method and URL")
  void givenRequestMethodAndInputUri_WhenCreateBringRequest_ThenValidVoidResult() {
    // data
    RequestMethod inputRequestMethod = RequestMethod.GET;
    URI inputUri = URI.create(TEST_URL);
    // given
    // when
    var bringRequest = new BringRequest<>(inputRequestMethod, inputUri);
    // verify
    assertThat(bringRequest.getRequestMethod()).isEqualTo(inputRequestMethod);
    assertThat(bringRequest.getUrl()).isEqualTo(inputUri);
    assertThat(bringRequest.getBody()).isNull();
  }

  @Order(2)
  @Test
  @DisplayName("Create parameterized BringRequest by constructor with request method, URL and body")
  void givenRequestMethodAndInputUriAndBody_WhenCreateBringRequest_ThenValidParameterizedResult() {
    // data
    RequestMethod inputRequestMethod = RequestMethod.GET;
    URI inputUri = URI.create(TEST_URL);
    String inputBody = "TEST_BODY";
    // given
    // when
    var bringRequest = new BringRequest<>(inputRequestMethod, inputUri, inputBody);
    // verify
    assertThat(bringRequest.getRequestMethod()).isEqualTo(inputRequestMethod);
    assertThat(bringRequest.getUrl()).isEqualTo(inputUri);
    assertThat(bringRequest.getBody()).isEqualTo(inputBody);
  }

}
