package io.github.bobocodebreskul.server;

import static io.github.bobocodebreskul.server.WebPathValidator.PATH_SHOULD_NOT_CONTAIN_ASTERISKS;
import static io.github.bobocodebreskul.server.WebPathValidator.PATH_SHOULD_NOT_CONTAIN_MORE_THAN_ONE_SLASH_SEQUENTIALLY;
import static io.github.bobocodebreskul.server.WebPathValidator.PATH_SHOULD_NOT_CONTAIN_WHITESPACES;
import static io.github.bobocodebreskul.server.WebPathValidator.PATH_SHOULD_START_WITH_SLASH;
import static io.github.bobocodebreskul.server.WebPathValidator.validatePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchException;

import io.github.bobocodebreskul.context.exception.WebPathValidationException;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class WebPathValidatorTest {

  @Order(1)
  @ParameterizedTest
  @ValueSource(strings = {"/", "/test", "/test/test", "/test/test/test"})
  public void whenPathIsValid_thenDoNothing(String path) {
    assertThatCode(() -> validatePath(path))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @Order(2)
  @ValueSource(strings = {"test", "test/test", "test/test/test"})
  public void whenPathNotStartsWithSlash_thenThrowWebPathValidationException(String path) {
    Exception actualException = catchException(
        () -> validatePath(path));

    assertThat(actualException)
        .isInstanceOf(WebPathValidationException.class)
        .hasMessage(PATH_SHOULD_START_WITH_SLASH.formatted(path));
  }

  @ParameterizedTest
  @Order(3)
  @ValueSource(strings = {"//te st", "/ test/test", "/test/test "})
  public void whenPathContainsWhitespaces_thenThrowWebPathValidationException(String path) {
    Exception actualException = catchException(
        () -> validatePath(path));

    assertThat(actualException)
        .isInstanceOf(WebPathValidationException.class)
        .hasMessage(PATH_SHOULD_NOT_CONTAIN_WHITESPACES.formatted(path));
  }

  @ParameterizedTest
  @Order(4)
  @ValueSource(strings = {"//test", "/test//test", "/test///test"})
  public void whenPathContainsMoreThanOneSlashSequentially_thenThrowWebPathValidationException(
      String path) {
    Exception actualException = catchException(
        () -> validatePath(path));

    assertThat(actualException)
        .isInstanceOf(WebPathValidationException.class)
        .hasMessage(PATH_SHOULD_NOT_CONTAIN_MORE_THAN_ONE_SLASH_SEQUENTIALLY.formatted(path));
  }

  @ParameterizedTest
  @Order(5)
  @ValueSource(strings = {"/test*", "/test/*", "/*"})
  public void whenPathContainsAsterisk_thenThrowWebPathValidationException(String path) {
    Exception actualException = catchException(
        () -> validatePath(path));

    assertThat(actualException)
        .isInstanceOf(WebPathValidationException.class)
        .hasMessage(PATH_SHOULD_NOT_CONTAIN_ASTERISKS.formatted(path));
  }
}
