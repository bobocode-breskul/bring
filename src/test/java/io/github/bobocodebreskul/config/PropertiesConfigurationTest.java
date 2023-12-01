package io.github.bobocodebreskul.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.bobocodebreskul.context.exception.InvalidPropertyValueException;
import io.github.bobocodebreskul.context.exception.PropertyNotFoundException;
import java.lang.reflect.Field;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

class PropertiesConfigurationTest {

  @BeforeAll
  public static void init() {
    PropertiesConfiguration.loadProperties(PropertiesConfiguration.APPLICATION_PROPERTIES);
  }

  @Order(1)
  @DisplayName("Verify that properties is loaded successfully from the configuration file.")
  @Test
  void given_PropertiesConfiguration_when_loadProperties_then_verifyPropertiesAreLoaded()
      throws NoSuchFieldException, IllegalAccessException {

    // when
    Field propertiesField = PropertiesConfiguration.class.getDeclaredField("properties");
    propertiesField.setAccessible(true);
    Properties properties = (Properties) propertiesField.get(null);
    String port = properties.getProperty("server.port");
    String server = properties.getProperty("server.url");

    // then
    assertEquals("7777", port);
    assertEquals("https://test.com/", server);
  }

  @Order(2)
  @DisplayName("Verify that getProperty method returns correct string value.")
  @Test
  void given_PropertiesConfiguration_when_getProperty_thenReturnPropertyString() {

    // when
    String port = PropertiesConfiguration.getProperty("server.port");
    String server = PropertiesConfiguration.getProperty("server.url");

    // then
    assertEquals("7777", port);
    assertEquals("https://test.com/", server);
  }

  @Order(3)
  @DisplayName("Verify that getPropertyInt method returns correct integer value.")
  @Test
  void given_PropertiesConfiguration_when_getPropertyAsInt_thenReturnPropertyInteger() {

    // when
    int port = PropertiesConfiguration.getPropertyAsInt("server.port");

    // then
    assertEquals(7777, port);
  }

  @Order(4)
  @DisplayName("Throw an exception if getPropertyAsInt from string value.")
  @Test
  void given_PropertiesConfiguration_when_getPropertyAsIntFromString_thenThrowInvalidPropertyValueException() {
    assertThatThrownBy(
        () -> PropertiesConfiguration.getPropertyAsInt("server.url"))
        .isInstanceOf(InvalidPropertyValueException.class)
        .hasMessage("\"server.url\" property value is not a number!");
  }

  @Order(5)
  @DisplayName("Verify that getPropertyOrDefault method returns correct string value.")
  @Test
  void given_PropertiesConfiguration_when_getPropertyOrDefault_thenReturnPropertyString() {

    // when
    String port = PropertiesConfiguration.getPropertyOrDefault("server.port", "8097");
    String server = PropertiesConfiguration.getPropertyOrDefault("server.url", "test");

    // then
    assertEquals("7777", port);
    assertEquals("https://test.com/", server);
  }

  @Order(6)
  @DisplayName("Verify that getPropertyAsIntOrDefault method returns correct integer value.")
  @Test
  void given_PropertiesConfiguration_when_getPropertyAsIntOrDefault_thenReturnPropertyInteger() {

    // when
    int port = PropertiesConfiguration.getPropertyAsIntOrDefault("server.port", 8097);

    // then
    assertEquals(7777, port);
  }

  @Order(7)
  @DisplayName("Throw an exception if getPropertyAsIntOrDefault from string value.")
  @Test
  void given_PropertiesConfiguration_when_getPropertyAsIntOrDefaultFromString_thenThrowInvalidPropertyValueException() {
    assertThatThrownBy(
        () -> PropertiesConfiguration.getPropertyAsIntOrDefault("server.url", 8097))
        .isInstanceOf(InvalidPropertyValueException.class)
        .hasMessage("\"server.url\" property value is not a number!");
  }

  @Order(8)
  @DisplayName("Verify that getPropertyOrDefault method returns default value if property does not exist.")
  @Test
  void given_PropertiesConfiguration_when_getPropertyOrDefaultAndPropertyDoesNotExist_then_ReturnDefaultPropertyString() {

    // when
    String year = PropertiesConfiguration.getPropertyOrDefault("year", "2024");
    String name = PropertiesConfiguration.getPropertyOrDefault("name", "Maria");

    // then
    assertEquals("2024", year);
    assertEquals("Maria", name);
  }

  @Order(9)
  @DisplayName("Verify that getPropertyAsIntOrDefault method returns default value if property does not exist.")
  @Test
  void given_PropertiesConfiguration_when_getPropertyAsIntOrDefaultAndPropertyDoesNotExist_then_returnDefaultPropertyInteger() {

    // when
    int counter = PropertiesConfiguration.getPropertyAsIntOrDefault("counter", 123);

    // then
    assertEquals(123, counter);
  }

  @Order(10)
  @DisplayName("Do nothing when loadProperties from non existing file.")
  @Test
  void given_PropertiesConfiguration_when_loadPropertiesFromNonExistingPropertyFile_thenDoNothing() {

    // given
    String newConfigFile = "test.properties";

    // when
    assertDoesNotThrow(() -> PropertiesConfiguration.loadProperties(newConfigFile));
  }

  @Order(11)
  @DisplayName("Throw PropertyNotFoundException if property does not exist after getProperty call.")
  @Test
  void given_PropertiesConfigFile_when_getPropertyThatDoesNotExist_then_throwPropertyNotFoundException() {
    // given
    String propertyName = "server.name";

    // when
    assertThatThrownBy(
        () -> PropertiesConfiguration.getProperty(propertyName))
        .isInstanceOf(PropertyNotFoundException.class)
        .hasMessage("The property with name \"%s\" is not found!".formatted(propertyName));
  }

  @Order(12)
  @DisplayName("Throw PropertyNotFoundException if property does not exist after getPropertyAsInt call.")
  @Test
  void given_PropertiesConfigFile_when_getPropertyAsIntThatDoesNotExist_then_throwPropertyNotFoundException() {
    // given
    String propertyName = "server.value";

    // when
    assertThatThrownBy(
        () -> PropertiesConfiguration.getPropertyAsInt(propertyName))
        .isInstanceOf(PropertyNotFoundException.class)
        .hasMessage("The property with name \"%s\" is not found!".formatted(propertyName));
  }
}