package io.github.bobocodebreskul.server;

import java.lang.reflect.Method;

public record ControllerMethod(Object controller, Method method) {

}
