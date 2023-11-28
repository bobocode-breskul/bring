package io.github.bobocodebreskul.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

import io.github.bobocodebreskul.server.enums.ResponseBodyEnum;
import io.github.bobocodebreskul.server.enums.ResponseStatus;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.params.provider.ValueSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BringResponseTest {

  private static final String TEST_HEADER_NAME = "test-header-name";
  private static final String TEST_HEADER_VALUE = "test-header-value";

  private static final String TEST_COOKIE_NAME = "test-cookie-name";
  private static final String TEST_COOKIE_VALUE = "test-header-value";

  @SuppressWarnings("unchecked")
  @Order(1)
  @DisplayName("Check that header was added successfully")
  @Test
  void given_BringResponse_When_AddHeader_Then_HeaderIsSuccessfullyAdded()
      throws NoSuchFieldException, IllegalAccessException {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);

    // when
    response.addHeader(TEST_HEADER_NAME, TEST_HEADER_VALUE);

    // then
    Field headers = response.getClass().getDeclaredField("headers");
    headers.setAccessible(true);
    Map<String, String> actual = (Map<String, String>) headers.get(response);
    assertThat(actual).containsEntry(TEST_HEADER_NAME, TEST_HEADER_VALUE);
    headers.setAccessible(false);
  }

  @Order(2)
  @DisplayName("Check adding header when header name is null")
  @Test
  void given_BringResponse_When_AddHeaderWithNullName_Then_ThrowIllegalArgumentException() {
    // given
    BringResponse<Object> response = new BringResponse<>(ResponseBodyEnum.NONE);

    // when
    Exception actualException =
        catchException(() -> response.addHeader(null, TEST_HEADER_VALUE));

    // then
    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(BringResponse.HEADER_NAME_SHOULD_NOT_BE_NULL);
  }

  @Order(3)
  @DisplayName("Adding header with null name should throw IllegalArgumentException")
  @Test
  void given_BringResponse_When_AddHeaderWithNullValue_Then_ThrowIllegalArgumentException() {
    // given
    BringResponse<Object> response = new BringResponse<>(ResponseBodyEnum.NONE);

    // when
    Exception actualException =
        catchException(() -> response.addHeader(TEST_HEADER_NAME, null));

    // then
    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(BringResponse.HEADER_VALUE_SHOULD_NOT_BE_NULL);
  }

  @SuppressWarnings("unchecked")
  @Order(4)
  @DisplayName("Get header successfully")
  @Test
  void given_BringResponse_When_GetHeader_Then_ReturnHeaderValue()
      throws NoSuchFieldException, IllegalAccessException {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);

    // when
    Field headers = response.getClass().getDeclaredField("headers");
    headers.setAccessible(true);
    Map<String, String> headersMap = (Map<String, String>) headers.get(response);
    headersMap.put(TEST_HEADER_NAME, TEST_HEADER_VALUE);
    headers.setAccessible(false);

    // then
    assertThat(response.getHeader(TEST_HEADER_NAME)).isEqualTo(TEST_HEADER_VALUE);
  }

  @Order(5)
  @DisplayName("Get non existing header should return empty string")
  @Test
  void given_BringResponse_When_GetNonExistingHeader_Then_ReturnEmptyString() {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);

    // when

    // then
    assertThat(response.getHeader(TEST_HEADER_NAME)).isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Order(6)
  @DisplayName("Check that header is removed successfully")
  @Test
  void given_BringResponse_When_RemoveHeader_Then_HeaderIsSuccessfullyRemoved()
      throws NoSuchFieldException, IllegalAccessException {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);
    Field headers = response.getClass().getDeclaredField("headers");
    headers.setAccessible(true);
    Map<String, String> headersMap = (Map<String, String>) headers.get(response);
    headersMap.put(TEST_HEADER_NAME, TEST_HEADER_VALUE);

    // when
    response.removeHeader(TEST_HEADER_NAME);

    // then
    assertThat(headersMap).doesNotContainKey(TEST_HEADER_NAME);
    headers.setAccessible(false);
  }

  @SuppressWarnings("unchecked")
  @Order(7)
  @DisplayName("Check that cookies was cleaned when header 'Cookie' was removed")
  @Test
  void given_BringResponse_When_RemoveHeaderCookie_Then_CleanCookies()
      throws NoSuchFieldException, IllegalAccessException {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);
    Field headers = response.getClass().getDeclaredField("headers");
    headers.setAccessible(true);
    Map<String, String> headersMap = (Map<String, String>) headers.get(response);
    headersMap.put(BringResponse.COOKIE, TEST_HEADER_VALUE);
    headers.setAccessible(false);

    // when
    response.removeHeader(TEST_HEADER_NAME);

    // then
    Field cookies = response.getClass().getDeclaredField("cookies");
    cookies.setAccessible(true);
    Map<String, String> cookiesMap = (Map<String, String>) cookies.get(response);
    assertThat(cookiesMap).isEmpty();
    cookies.setAccessible(false);
  }

  @SuppressWarnings("unchecked")
  @Order(8)
  @DisplayName("Check that removing non existing header doesn't removed anything")
  @Test
  void given_BringResponse_When_RemoveNonExistingHeader_Then_NotRemoved()
      throws NoSuchFieldException, IllegalAccessException {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);
    Field headers = response.getClass().getDeclaredField("headers");
    headers.setAccessible(true);
    Map<String, String> headersMap = (Map<String, String>) headers.get(response);
    headersMap.put(TEST_HEADER_NAME, TEST_HEADER_VALUE);

    // when
    response.removeHeader("TEST-NON-EXIST-HEADER_NAME");

    // then
    assertThat(headersMap).containsEntry(TEST_HEADER_NAME, TEST_HEADER_VALUE);
    headers.setAccessible(false);
  }

  @SuppressWarnings("unchecked")
  @Order(9)
  @DisplayName("Check that cookie was added successfully")
  @Test
  void given_BringResponse_When_AddCookie_Then_CookieIsSuccessfullyAdded()
      throws NoSuchFieldException, IllegalAccessException {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);

    // when
    response.addCookie(TEST_COOKIE_NAME, TEST_COOKIE_VALUE);

    // then
    Field cookies = response.getClass().getDeclaredField("cookies");
    cookies.setAccessible(true);
    Map<String, String> actual = (Map<String, String>) cookies.get(response);
    assertThat(actual).containsEntry(TEST_COOKIE_NAME, TEST_COOKIE_VALUE);
    cookies.setAccessible(false);
  }

  @SuppressWarnings("unchecked")
  @Order(10)
  @DisplayName("Check that header cookie was added successfully")
  @Test
  void given_BringResponse_When_AddCookie_Then_CookieIsSuccessfullyAddedToHeaders()
      throws NoSuchFieldException, IllegalAccessException {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);

    // when
    response.addCookie(TEST_COOKIE_NAME, TEST_COOKIE_VALUE);

    // then
    Field headers = response.getClass().getDeclaredField("headers");
    headers.setAccessible(true);
    Map<String, String> actual = (Map<String, String>) headers.get(response);
    assertThat(actual)
        .containsEntry(BringResponse.COOKIE, TEST_COOKIE_NAME + "=" + TEST_COOKIE_VALUE);
    headers.setAccessible(false);
  }

  @Order(11)
  @DisplayName("Adding cookie with null name should throw IllegalArgumentException")
  @Test
  void given_BringResponse_When_AddCookieWithNullName_Then_ThrowIllegalArgumentException() {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);

    // when
    Exception actualException = catchException(() -> response.addCookie(null, TEST_COOKIE_VALUE));

    // then
    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(BringResponse.COOKIE_NAME_SHOULD_NOT_BE_NULL);
  }

  @Order(12)
  @DisplayName("Adding cookie with null value should throw IllegalArgumentException")
  @Test
  void given_BringResponse_When_AddCookieWithNullValue_Then_ThrowIllegalArgumentException() {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);

    // when
    Exception actualException = catchException(() -> response.addCookie(TEST_COOKIE_NAME, null));

    // then
    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(BringResponse.COOKIE_VALUE_SHOULD_NOT_BE_NULL);
  }

  @Order(13)
  @DisplayName("Get cookie when exists then return correct value")
  @Test
  void given_ExistedValue_When_AddCookieWithNullValue_Then_ReturnValue() {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(null, null, ResponseStatus.OK);
    response.addCookie(TEST_COOKIE_NAME, TEST_COOKIE_VALUE);

    // when
    String actual = response.getCookie(TEST_COOKIE_NAME);

    // then
    assertThat(actual).isEqualTo(TEST_COOKIE_VALUE);
  }

  @Order(14)
  @DisplayName("Get cookie when non-exists then return correct value")
  @Test
  void given_NonExistedValue_When_AddCookieWithNullValue_Then_ReturnEmpty() {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseStatus.OK);

    // when
    String actualException = response.getCookie(TEST_COOKIE_NAME);

    // then
    assertThat(actualException).isEmpty();
  }

  @Order(15)
  @DisplayName("Get cookie when exists then return correct value")
  @ValueSource(strings = {"Test-Cookie-Name", TEST_COOKIE_NAME, "TEST-COOKIE-NAME"})
  @ParameterizedTest
  void given_DifferentCaseKeys_When_AddCookieWithNullValue_Then_ReturnTheSameValue() {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseStatus.OK);
    response.addCookie(TEST_COOKIE_NAME, TEST_COOKIE_VALUE);

    // when
    String actualException = response.getCookie(TEST_COOKIE_NAME);

    // then
    assertThat(actualException).isEqualTo(TEST_COOKIE_VALUE);
  }

  @SuppressWarnings("unchecked")
  @Order(16)
  @DisplayName("Check that cookie is removed successfully")
  @Test
  void given_BringResponse_When_RemoveCookie_Then_CookieIsSuccessfullyRemoved()
      throws NoSuchFieldException, IllegalAccessException {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);
    Field cookies = response.getClass().getDeclaredField("cookies");
    cookies.setAccessible(true);
    Map<String, String> cookiesMap = (Map<String, String>) cookies.get(response);
    cookiesMap.put(TEST_COOKIE_NAME, TEST_COOKIE_VALUE);

    // when
    response.removeCookie(TEST_COOKIE_NAME);

    // then
    assertThat(cookiesMap).doesNotContainKey(TEST_COOKIE_NAME);
    cookies.setAccessible(false);
  }

  @SuppressWarnings("unchecked")
  @Order(17)
  @DisplayName("Check that removing non existing header doesn't removed anything")
  @Test
  void given_BringResponse_When_RemoveNonExistingCookie_Then_NotRemoved()
      throws NoSuchFieldException, IllegalAccessException {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);
    Field cookies = response.getClass().getDeclaredField("cookies");
    cookies.setAccessible(true);
    Map<String, String> cookiesMap = (Map<String, String>) cookies.get(response);
    cookiesMap.put(TEST_COOKIE_NAME, TEST_COOKIE_VALUE);

    // when
    response.removeCookie("TEST-NON-EXIST-HEADER_NAME");

    // then
    assertThat(cookiesMap).containsEntry(TEST_COOKIE_NAME, TEST_COOKIE_VALUE);
    cookies.setAccessible(false);
  }

  @Order(18)
  @DisplayName("Adding cookie with null value should throw IllegalArgumentException")
  @Test
  void given_BringResponse_When_RemoveCookieWithNullName_Then_ThrowIllegalArgumentException() {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);

    // when
    Exception actualException = catchException(() -> response.removeCookie(null));

    // then
    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(BringResponse.COOKIE_NAME_SHOULD_NOT_BE_NULL);
  }

  @SuppressWarnings("unchecked")
  @Order(19)
  @DisplayName("Check that removing cookie should remove it from headers")
  @Test
  void given_BringResponse_When_RemoveCookie_Then_RemovedCookieFromHeaders()
      throws NoSuchFieldException, IllegalAccessException {
    // given
    BringResponse<ResponseBodyEnum> response = new BringResponse<>(ResponseBodyEnum.NONE);
    response.addCookie(TEST_COOKIE_NAME, TEST_COOKIE_VALUE);
    response.addCookie("test-cookie-name-2", "test-cookie-value-2");

    // when
    response.removeCookie("test-cookie-name-2");

    // then
    Field headers = response.getClass().getDeclaredField("headers");
    headers.setAccessible(true);
    Map<String, String> actual = (Map<String, String>) headers.get(response);
    assertThat(actual)
        .containsEntry(BringResponse.COOKIE, TEST_COOKIE_NAME + "=" + TEST_COOKIE_VALUE);
    headers.setAccessible(false);
  }

  // TODO: add tests for get/set status and get/set body
}