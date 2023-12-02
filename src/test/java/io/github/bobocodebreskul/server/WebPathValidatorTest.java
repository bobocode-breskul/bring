package io.github.bobocodebreskul.server;

import static io.github.bobocodebreskul.server.WebPathValidator.PATH_SHOULD_NOT_CONTAIN_ASTERISKS;
import static io.github.bobocodebreskul.server.WebPathValidator.PATH_SHOULD_NOT_CONTAIN_MORE_THAN_ONE_SLASH_SEQUENTIALLY;
import static io.github.bobocodebreskul.server.WebPathValidator.PATH_SHOULD_NOT_CONTAIN_WHITESPACES;
import static io.github.bobocodebreskul.server.WebPathValidator.PATH_SHOULD_NOT_ENDS_WITH_SLASH;
import static io.github.bobocodebreskul.server.WebPathValidator.PATH_SHOULD_START_WITH_SLASH;
import static io.github.bobocodebreskul.server.WebPathValidator.validatePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchException;

import io.github.bobocodebreskul.server.exception.WebPathValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class WebPathValidatorTest {

  @Order(1)
  @DisplayName("Do nothing when path is valid")
  @ParameterizedTest
  @ValueSource(strings = {"/test", "/test/test", "/test/test/test"})
  public void whenPathIsValid_thenDoNothing(String path) {
    assertThatCode(() -> validatePath(path))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @DisplayName("Throw WebPathValidationException when the path does not start with a slash")
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
  @DisplayName("Throw WebPathValidationException when the path ends with slash")
  @Order(3)
  @ValueSource(strings = {"/test/", "/test/test/", "/test/test/test/"})
  public void whenPathEndsWithSlash_thenThrowWebPathValidationException(String path) {
    Exception actualException = catchException(
        () -> validatePath(path));

    assertThat(actualException)
        .isInstanceOf(WebPathValidationException.class)
        .hasMessage(PATH_SHOULD_NOT_ENDS_WITH_SLASH.formatted(path));
  }

  @ParameterizedTest
  @DisplayName("Throw WebPathValidationException when the path contains white spaces")
  @Order(4)
  @ValueSource(strings = {"//te st", "/ test/test", "/test/test ", "/test/test\n", "/test/test\r"})
  public void whenPathContainsWhitespaces_thenThrowWebPathValidationException(String path) {
    Exception actualException = catchException(
        () -> validatePath(path));

    assertThat(actualException)
        .isInstanceOf(WebPathValidationException.class)
        .hasMessage(PATH_SHOULD_NOT_CONTAIN_WHITESPACES.formatted(path));
  }

  @ParameterizedTest
  @DisplayName("Throw WebPathValidationException when the path contains more than one consecutive slash")
  @Order(5)
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
  @DisplayName("Throw WebPathValidationException when the path contains the asterisk symbol")
  @Order(6)
  @ValueSource(strings = {"/test*", "/test/*", "/*"})
  public void whenPathContainsAsterisk_thenThrowWebPathValidationException(String path) {
    Exception actualException = catchException(
        () -> validatePath(path));

    assertThat(actualException)
        .isInstanceOf(WebPathValidationException.class)
        .hasMessage(PATH_SHOULD_NOT_CONTAIN_ASTERISKS.formatted(path));
  }
}
