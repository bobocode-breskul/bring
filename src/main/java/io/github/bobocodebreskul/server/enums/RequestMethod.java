package io.github.bobocodebreskul.server.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of HTTP request methods
 */
public enum RequestMethod {

  GET, HEAD, POST, PUT, DELETE;

  private static final Map<String, RequestMethod> methodByName;

  static {
    HashMap<String, RequestMethod> tempMethodByName = new HashMap<>();
    for (RequestMethod requestMethod : RequestMethod.values()) {
      tempMethodByName.put(requestMethod.name().toLowerCase(), requestMethod);
    }
    methodByName = Collections.unmodifiableMap(tempMethodByName);
  }

  public static RequestMethod getByName(String name) {
    String lowerCaseName = name.toLowerCase();
    if (!methodByName.containsKey(lowerCaseName)) {
      throw new IllegalArgumentException("Request method not found for name '%s'".formatted(name));
    }
    return methodByName.get(lowerCaseName);
  }

}