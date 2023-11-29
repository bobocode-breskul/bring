package io.github.bobocodebreskul.context.exception;

import io.github.bobocodebreskul.config.PropertiesConfiguration;

/**
 * Exception thrown to indicate that loading properties in loadProperties() method was failed.
 *
 * @see PropertiesConfiguration
 */
public class LoadingPropertiesFailedException extends RuntimeException {

  public LoadingPropertiesFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
