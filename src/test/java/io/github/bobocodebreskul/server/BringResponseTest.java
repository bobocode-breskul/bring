package io.github.bobocodebreskul.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.bobocodebreskul.server.BringResponse.ResponseBuilder;
import io.github.bobocodebreskul.server.enums.ResponseBodyEnum;
import io.github.bobocodebreskul.server.enums.ResponseStatus;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BringResponseTest {

  @Order(1)
  @DisplayName("Check that status was set successfully")
  @MethodSource("getStatuses")
  @ParameterizedTest
  void given_BringResponse_When_SetStatus_Then_StatusIsSuccessfullySet(
      ResponseStatus responseStatus)
      throws NoSuchFieldException, IllegalAccessException {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);

    // when
    response.setStatus(responseStatus);

    // then
    Field status = response.getClass().getDeclaredField("status");
    status.setAccessible(true);
    ResponseStatus actual = (ResponseStatus) status.get(response);
    assertThat(actual).isEqualTo(responseStatus);
    status.setAccessible(false);
  }

  @Order(2)
  @DisplayName("Check that status was get successfully")
  @MethodSource("getStatuses")
  @ParameterizedTest
  void given_BringResponse_When_GetStatus_Then_StatusIsSuccessfullyGet(
      ResponseStatus responseStatus) {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(responseStatus);

    // when
    ResponseStatus actual = response.getStatus();

    // then
    assertThat(actual).isEqualTo(responseStatus);
  }

  @Order(4)
  @DisplayName("Throw error if status null")
  @Test
  public void when_CreateBringResponseWithoutStatus_then_ThrowException() {

    assertThatThrownBy(() -> new BringResponse<>(new Object(), null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("HttpStatus must not be null");
  }

  private static Stream<ResponseStatus> getStatuses() {
    return Stream.of(ResponseStatus.ACCEPTED,
        ResponseStatus.CREATED,
        ResponseStatus.BAD_GATEWAY,
        ResponseStatus.BAD_REQUEST,
        ResponseStatus.OK,
        ResponseStatus.FORBIDDEN);
  }

  @Order(3)
  @DisplayName("Check that status was get successfully with builder")
  @MethodSource("getStatuses")
  @ParameterizedTest
  void given_BringResponseBuilder_When_GetStatus_Then_StatusIsSuccessfullyGet(
      ResponseStatus responseStatus) {
    // given
    BringResponse<Void> response = new ResponseBuilder<Void>(responseStatus)
        .headers(new HashMap<>())
        .build();

    // when
    ResponseStatus actual = response.getStatus();

    // then
    assertThat(actual).isEqualTo(responseStatus);
  }
}