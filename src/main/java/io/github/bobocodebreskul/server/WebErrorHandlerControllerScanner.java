package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.exception.MethodValidationException;
import io.github.bobocodebreskul.context.registry.BringContainer;
import io.github.bobocodebreskul.server.annotations.ErrorHandlerController;
import io.github.bobocodebreskul.server.annotations.ExceptionHandler;
import io.github.bobocodebreskul.server.exception.DuplicateErrorHandlerException;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

/**
 * This class is responsible for scanning and error handlers. It uses bring container to retrieve
 * all Beans marked as {@link ErrorHandlerController} then retrieve exceptions and methods from
 * public methods marked as {@link ExceptionHandler}
 */
public class WebErrorHandlerControllerScanner {

  private final static Logger log = LoggerFactory.getLogger(WebErrorHandlerControllerScanner.class);
  private final BringContainer container;

  /**
   * Constructs a new instance of {@code WebErrorHandlerControllerScanner} with the specified bring
   * container.
   *
   * @param container The container providing information about error handler controllers.
   */
  public WebErrorHandlerControllerScanner(BringContainer container) {
    this.container = container;
  }

  /**
   * This method is responsible for scanning and error handlers. It uses bring container to retrieve
   * all Beans marked as {@link ErrorHandlerController} then retrieve exceptions and methods from
   * public methods marked as {@link ExceptionHandler}
   *
   * @return Map of (Throwable.class, ControllerMethod)
   */
  public Map<Class<?>, ControllerMethod> getAllWebErrorHandlerControllers() {
    Map<Class<?>, ControllerMethod> controllerMethodMap = new HashMap<>();

    List<Object> controllerBeans = getErrorHandlerControllerBeans();
    log.debug("Retrieved [{}] error handler controller beans", controllerBeans.size());

    for (Object controller : controllerBeans) {
      for (Method method : controller.getClass().getMethods()) {
        if (method.isAnnotationPresent(ExceptionHandler.class)) {
          Class<?>[] methodParameterTypes = method.getParameterTypes();

          log.debug("Validating params for method [{}]", method.getName());
          validateMethodParams(methodParameterTypes);

          Class<?> errorClass = getErrorClass(methodParameterTypes);

          if (controllerMethodMap.put(
              errorClass,
              new ControllerMethod(controller, method)) != null) {
            log.error("Error [{}] is already mapped in method [{}]", errorClass.getName(),
                method.getName());
            throw new DuplicateErrorHandlerException(
                "Error [%s] is already mapped".formatted(errorClass));
          }
        }
      }
    }
    return controllerMethodMap;
  }

  private static Class<?> getErrorClass(Class<?>[] methodParameterTypes) {
    if (methodParameterTypes.length == 1) {
      return methodParameterTypes[0];
    }

    if (methodParameterTypes.length == 2 && Throwable.class.isAssignableFrom(
        methodParameterTypes[0])) {
      return methodParameterTypes[0];
    }

    return methodParameterTypes[1];
  }

  private List<Object> getErrorHandlerControllerBeans() {
    return container.getAllBeans()
        .stream()
        .filter(bean -> bean.getClass().isAnnotationPresent(ErrorHandlerController.class))
        .toList();
  }


  private static void validateMethodParams(Class<?>[] methodParameterTypes) {
    if (methodParameterTypes.length > 2 || methodParameterTypes.length == 0) {
      throw new MethodValidationException("Invalid parameter quantity");
    }

    if (Arrays.stream(methodParameterTypes)
        .filter(Throwable.class::isAssignableFrom)
        .count() != 1) {
      throw new MethodValidationException("Only 1 exceptions is allowed for errorhandler method");
    }

    if (methodParameterTypes.length == 2 &&
        Arrays.stream(methodParameterTypes)
            .filter(HttpServletRequest.class::isAssignableFrom)
            .count() != 1) {
      throw new MethodValidationException("There should be at least 1 HttpServletRequest");
    }
  }
}
