package io.github.bobocodebreskul.config;

import ch.qos.logback.classic.Level;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class LoggerFactory {

  public static final String PROPERTY_NAME = "logging";


  /**
   * Return a logger named corresponding to the class passed as parameter, using the statically
   * bound {@link ILoggerFactory} instance.
   *
   * @param clazz the returned logger will be named after clazz
   * @return logger
   */
  public static Logger getLogger(Class<?> clazz) {
    Level level = Level.valueOf(PropertiesConfiguration.getPropertyOrDefault(PROPERTY_NAME, "INFO"));
    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(clazz);
    logger.setLevel(level);
    return logger;
  }
}
