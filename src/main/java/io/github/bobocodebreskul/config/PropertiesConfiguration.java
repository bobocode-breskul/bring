package io.github.bobocodebreskul.config;

import static java.util.Objects.isNull;

import io.github.bobocodebreskul.context.exception.ConfigurationFileNotFoundException;
import io.github.bobocodebreskul.context.exception.InvalidPropertyValueException;
import io.github.bobocodebreskul.context.exception.LoadingPropertiesFailedException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * PropertiesConfiguration class stores properties from configuration file and provides an API to
 * get property as string or integer value.
 */
@Slf4j
public class PropertiesConfiguration {

  private static String CONFIG_FILE = "application.properties";

  private static Properties properties;

  /**
   * Method loads properties from configuration file and stores then in properties variable.
   */
  public static void loadProperties() {
    Properties properties = new Properties();
    try (InputStream input = PropertiesConfiguration.class.getClassLoader()
        .getResourceAsStream(CONFIG_FILE)) {
      if (input == null) {
        log.info("Sorry, unable to find %s".formatted(CONFIG_FILE));
        throw new ConfigurationFileNotFoundException(
            "Sorry, unable to find %s".formatted(CONFIG_FILE));
      }

      properties.load(input);
      log.info("Properties from %s file was loaded.".formatted(CONFIG_FILE));
    } catch (Exception e) {
      log.error("Loading properties from %s file failed.".formatted(CONFIG_FILE), e);
      throw new LoadingPropertiesFailedException(
          "Loading properties from %s file failed.".formatted(CONFIG_FILE), e);
    }
    PropertiesConfiguration.properties = properties;
  }

  /**
   * Method returns property string value loaded from configuration file.
   *
   * @param propertyName property key name
   * @return string property value
   */
  public static String getProperty(String propertyName) {
    log.debug("Get string property \"%s\"".formatted(propertyName));
    return properties.getProperty(propertyName);
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
    return isNull(property) ? defaultValue : property;
  }

  /**
   * Method returns property integer value loaded from configuration file.
   *
   * @param propertyName property key name
   * @return integer property value
   */
  public static Integer getPropertyAsInt(String propertyName) {
    log.debug("Get integer property \"%s\"".formatted(propertyName));
    try {
      return Integer.parseInt(properties.getProperty(propertyName));
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
  public static Integer getPropertyAsIntOrDefault(String propertyName, int defaultValue) {
    log.debug("Get integer property \"%s\"".formatted(propertyName));
    try {
      String property = properties.getProperty(propertyName);
      return isNull(property) ? defaultValue : Integer.parseInt(property);
    } catch (NumberFormatException ex) {
      throw new InvalidPropertyValueException(
          "\"%s\" property value is not a number!".formatted(propertyName), ex);
    }
  }
}
