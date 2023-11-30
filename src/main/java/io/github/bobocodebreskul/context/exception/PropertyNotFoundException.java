package io.github.bobocodebreskul.context.exception;

import io.github.bobocodebreskul.config.PropertiesConfiguration;

/**
 * Exception thrown when property is not found in property file.
 *
 * @see PropertiesConfiguration
 */
public class PropertyNotFoundException extends RuntimeException {

  public PropertyNotFoundException(String message) {
    super(message);
  }
}
