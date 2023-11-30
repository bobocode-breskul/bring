package io.github.bobocodebreskul.context.exception;

import io.github.bobocodebreskul.config.PropertiesConfiguration;

/**
 * Exception thrown to indicate that configuration file was not found during loadProperties()
 * loading from PropertiesConfiguration.
 *
 * @see PropertiesConfiguration
 */
public class ConfigurationFileNotFoundException extends RuntimeException {

  public ConfigurationFileNotFoundException(String message) {
    super(message);
  }
}
