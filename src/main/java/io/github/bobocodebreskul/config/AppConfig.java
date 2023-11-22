package io.github.bobocodebreskul.config;

import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppConfig {

  private static final String CONFIG_FILE = "application.properties";

  private static Properties properties;

  public static void loadProperties() {
    Properties properties = new Properties();
    try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
      if (input == null) {
        log.info("Sorry, unable to find " + CONFIG_FILE);
      } else {
        properties.load(input);
        log.info("Properties from %s file was loaded.".formatted(CONFIG_FILE));
      }
    } catch (Exception e) {
      log.error("Loading properties from %s file failed.".formatted(CONFIG_FILE), e);
    }
    AppConfig.properties = properties;
  }

  public static int getPort() {
    String port = properties.getProperty("port");
    return port == null ? 8080 : Integer.parseInt(port);
  }

}
