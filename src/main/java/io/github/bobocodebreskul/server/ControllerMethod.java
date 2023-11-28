package io.github.bobocodebreskul.server;

import java.lang.reflect.Method;

/**
 * Represents a combination of a controller object and a method, typically used in web application
 * routing. Instances of this record are employed by {@link WebPathScanner} to identify and map
 * controller methods.
 *
 * @param controller The object instance serving as the controller for a specific route.
 * @param method     The method within the controller object that handles the corresponding route.
 * @see WebPathScanner
 */
public record ControllerMethod(Object controller, Method method) {

}
