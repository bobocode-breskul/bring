package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.context.exception.WebPathValidationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebPathValidator {

  static final String PATH_SHOULD_START_WITH_SLASH = "Path '%s' validation failed. Path should start with /.";
  static final String PATH_SHOULD_NOT_CONTAIN_WHITESPACES = "Path '%s' validation failed. Path should not contain whitespaces.";
  static final String PATH_SHOULD_NOT_CONTAIN_ASTERISKS = "Path '%s' validation failed. Path should not contain *.";
  static final String PATH_SHOULD_NOT_CONTAIN_MORE_THAN_ONE_SLASH_SEQUENTIALLY = "Path '%s' validation failed. Path should not contain more than one '/' sequentially.";

  /**
   * The method verifies that the provided path is valid.
   *
   * @param path
   * @throws WebPathValidationException when path does not start from /
   * @throws WebPathValidationException when path has white space
   * @throws WebPathValidationException when path has asterisk
   * @throws WebPathValidationException when path contain more than one / sequentially
   */
  public static void validatePath(String path) {
    if (!path.startsWith("/")) {
      log.error(PATH_SHOULD_START_WITH_SLASH.formatted(path));
      throw new WebPathValidationException(PATH_SHOULD_START_WITH_SLASH.formatted(path));
    }

    if (path.contains(" ")) {
      log.error(PATH_SHOULD_NOT_CONTAIN_WHITESPACES.formatted(path));
      throw new WebPathValidationException(PATH_SHOULD_NOT_CONTAIN_WHITESPACES.formatted(path));
    }

    if (path.contains("*")) {
      log.error(PATH_SHOULD_NOT_CONTAIN_ASTERISKS.formatted(path));
      throw new WebPathValidationException(PATH_SHOULD_NOT_CONTAIN_ASTERISKS.formatted(path));
    }

    if (path.matches(".*/{2,}.*")) {
      log.error(PATH_SHOULD_NOT_CONTAIN_MORE_THAN_ONE_SLASH_SEQUENTIALLY.formatted(path));
      throw new WebPathValidationException(PATH_SHOULD_NOT_CONTAIN_MORE_THAN_ONE_SLASH_SEQUENTIALLY.formatted(path));
    }
  }
}
