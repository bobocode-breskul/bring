package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.exception.WebPathValidationException;
import java.util.regex.Pattern;
import org.slf4j.Logger;

public class WebPathValidator {

  private final static Logger log = LoggerFactory.getLogger(WebPathValidator.class);
  static final String PATH_SHOULD_START_WITH_SLASH = "Request mapping with path '%s' validation failed. Path should start with /.";
  static final String PATH_SHOULD_NOT_ENDS_WITH_SLASH = "Request mapping with path path '%s' validation failed. Path should not end with /.";
  static final String PATH_SHOULD_NOT_CONTAIN_WHITESPACES = "Request mapping with path '%s' validation failed. Path should not contain whitespaces.";
  static final String PATH_SHOULD_NOT_CONTAIN_ASTERISKS = "Request mapping with path '%s' validation failed. Path should not contain *.";
  static final String PATH_SHOULD_NOT_CONTAIN_MORE_THAN_ONE_SLASH_SEQUENTIALLY = "Request mapping with path '%s' validation failed. Path should not contain more than one '/' sequentially.";

  private static final Pattern WHITESPACE_SEARCH_PATTERN = Pattern.compile("\\s");

  /**
   * The method verifies that the provided path is valid.
   *
   * @param path web path for validation
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

    if (path.endsWith("/")) {
      log.error(PATH_SHOULD_NOT_ENDS_WITH_SLASH.formatted(path));
      throw new WebPathValidationException(PATH_SHOULD_NOT_ENDS_WITH_SLASH.formatted(path));
    }

    if (WHITESPACE_SEARCH_PATTERN.matcher(path).find()) {
      log.error(PATH_SHOULD_NOT_CONTAIN_WHITESPACES.formatted(path));
      throw new WebPathValidationException(PATH_SHOULD_NOT_CONTAIN_WHITESPACES.formatted(path));
    }

    if (path.contains("*")) {
      log.error(PATH_SHOULD_NOT_CONTAIN_ASTERISKS.formatted(path));
      throw new WebPathValidationException(PATH_SHOULD_NOT_CONTAIN_ASTERISKS.formatted(path));
    }

    if (path.matches(".*/{2,}.*")) {
      log.error(PATH_SHOULD_NOT_CONTAIN_MORE_THAN_ONE_SLASH_SEQUENTIALLY.formatted(path));
      throw new WebPathValidationException(
          PATH_SHOULD_NOT_CONTAIN_MORE_THAN_ONE_SLASH_SEQUENTIALLY.formatted(path));
    }
  }
}
