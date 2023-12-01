package io.github.bobocodebreskul.config;

import io.github.bobocodebreskul.context.exception.ConfigurationFileNotFoundException;
import io.github.bobocodebreskul.context.exception.InvalidPropertyValueException;
import io.github.bobocodebreskul.context.exception.LoadingPropertiesFailedException;
import io.github.bobocodebreskul.context.exception.PropertyNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * PropertiesConfiguration class stores properties from configuration file and provides an API to
 * get property as string or integer value.
 */
@Slf4j
public class PropertiesConfiguration {
  public static final String APPLICATION_PROPERTIES = "application.properties";

  private static final Properties properties = new Properties();

  static {
    PropertiesConfiguration.loadProperties(PropertiesConfiguration.APPLICATION_PROPERTIES);
  }

  /**
   * Method loads properties from configuration file and stores then in properties variable.
   */
  public static void loadProperties(String configFileName) {
    try (InputStream input = PropertiesConfiguration.class.getClassLoader()
        .getResourceAsStream(configFileName)) {
      if (input == null) {
        log.info("Sorry, unable to find %s".formatted(configFileName));
        throw new ConfigurationFileNotFoundException(
            "Sorry, unable to find %s".formatted(configFileName));
      }
      properties.load(input);
      log.info("Properties from %s file was loaded.".formatted(configFileName));
    } catch (Exception e) {
      log.error("Loading properties from %s file failed.".formatted(configFileName), e);
      throw new LoadingPropertiesFailedException(
          "Loading properties from %s file failed.".formatted(configFileName), e);
    }
  }

  /**
   * Method returns property string value loaded from configuration file. If property does not exist
   * throws PropertyNotFoundException.
   *
   * @param propertyName property key name
   * @return string property value
   */
  public static String getProperty(String propertyName) {
    log.debug("Get string property \"%s\"".formatted(propertyName));
    String property = properties.getProperty(propertyName);
    if (property == null) {
      throw new PropertyNotFoundException(
          "The property with name \"%s\" is not found!".formatted(propertyName));
    }
    return property;
  }

  /**
   * Method returns property string value loaded from configuration file or returns default value if
   * property doesn't exist.
   *
   * @param propertyName property key name
   * @param defaultValue property default value
   * @return string property value
   */
  public static String getPropertyOrDefault(String propertyName, String defaultValue) {
    log.debug("Get string property \"%s\"".formatted(propertyName));
    String property = properties.getProperty(propertyName);
    return property == null ? defaultValue : property;
  }

  /**
   * Method returns property integer value loaded from configuration file. If property does not
   * exist throws PropertyNotFoundException.
   *
   * @param propertyName property key name
   * @return integer property value
   */
  public static int getPropertyAsInt(String propertyName) {
    log.debug("Get integer property \"%s\"".formatted(propertyName));
    try {
      String property = properties.getProperty(propertyName);
      if (property == null) {
        throw new PropertyNotFoundException(
            "The property with name \"%s\" is not found!".formatted(propertyName));
      }
      return Integer.parseInt(property);
    } catch (NumberFormatException ex) {
      throw new InvalidPropertyValueException(
          "\"%s\" property value is not a number!".formatted(propertyName), ex);
    }
  }

  /**
   * Method returns property integer value loaded from configuration file or returns default value
   * if property doesn't exist.
   *
   * @param propertyName property key name
   * @param defaultValue property default value
   * @return string property value
   */
  public static int getPropertyAsIntOrDefault(String propertyName, int defaultValue) {
    log.debug("Get integer property \"%s\"".formatted(propertyName));
    try {
      String property = properties.getProperty(propertyName);
      return property == null ? defaultValue : Integer.parseInt(property);
    } catch (NumberFormatException ex) {
      throw new InvalidPropertyValueException(
          "\"%s\" property value is not a number!".formatted(propertyName), ex);
    }
  }
}
