package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.context.annotations.ErrorHandlerController;
import io.github.bobocodebreskul.context.annotations.ExceptionHandler;
import io.github.bobocodebreskul.context.exception.DuplicateErrorHandlerException;
import io.github.bobocodebreskul.context.exception.MethodValidationException;
import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for scanning and error handlers. It uses bring container to retrieve
 * all Beans marked as {@link ErrorHandlerController} then retrieve exceptions and methods from
 * public methods marked as {@link ExceptionHandler}
 */
@Slf4j
public class WebErrorHandlerControllerScanner {

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
  //TODO: add test when container does not have any error handler controller beans then should return empty map
  //TODO: add test when container has error handler controller beans but does not have methods annotated with exception handler then should return empty map
  //TODO: add test when container has error handler controller beans but only private methods annotated with exception handler then should return empty map
  //TODO: add test when container has error handler controller beans and have private and public methods annotated with exception handler then should return only public methods in map
  //TODO: add test when container has error handler controller beans but only private methods annotated with exception handler then should return empty map
  //TODO: add test when container has error handler controller beans but method param has no arguments then should throw MethodValidationException
  //TODO: add test when container has error handler controller beans but method param has more then 2 arguments then should throw MethodValidationException
  //TODO: add test when container has error handler controller beans but method param has 2 exception type arguments then should throw MethodValidationException
  //TODO: add test when container has error handler controller beans but method param has 2 HttpServletRequest type arguments then should throw MethodValidationException
  //TODO: add test when container has error handler controller beans but method params does not have HttpServletRequest type arguments then should throw MethodValidationException
  //TODO: add test when container has error handler controller beans and have rigth deifned methods then should return valid controllerMethodMap
  //TODO: add test when container has error handler controller beans and have rigth deifned methods but have duplicate error handlers then should throw DuplicateErrorHandlerException
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
      throw new MethodValidationException("Invalid parameter quantaty");
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
      throw new MethodValidationException(
          "Only 1 HttpServletRequest is allowed for errorhandler method");
    }
  }
}
