package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.server.enums.ResponseBodyEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

class BringResponseTest {

  @Order(1)
  @DisplayName("Check that header is added successfully")
  @Test
  public void given_BringResponse_whenAddHeader_thenHeaderIsSuccessfullyAdded() {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);
    String headerName = "testheader";
    String headerValue = "test-header-value";

    // when
    response.addHeader(headerName, headerValue);

    // then
    //TODO: finish test logic
  }

  @Order(2)
  @DisplayName("Check when ")
  @Test
  public void given_BringResponse_whenGetNotExistingHeader_then_throwException() {
    // given
    BringResponse<Object> response = new BringResponse<>(new Object());

    // when

    // then
    //TODO: finish test logic
  }

}