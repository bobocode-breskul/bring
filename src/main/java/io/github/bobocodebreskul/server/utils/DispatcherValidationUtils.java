package io.github.bobocodebreskul.server.utils;

import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.exception.WebMethodParameterException;
import io.github.bobocodebreskul.server.DispatcherServlet;
import io.github.bobocodebreskul.server.enums.RequestMethod;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;

/**
 * Utility class providing methods for validating request methods and parameter types in a {@link DispatcherServlet}.
 */
public class DispatcherValidationUtils {

  private static final Logger log = LoggerFactory.getLogger(DispatcherValidationUtils.class);
  private static final List<String> METHODS_WITHOUT_BODY = List.of(
      RequestMethod.GET.name(),
      RequestMethod.HEAD.name(),
      RequestMethod.DELETE.name()
  );

  /**
   * Validates the request method for the presence of a request body. Throws a {@link WebMethodParameterException}
   * if the request method is not allowed to have a request body.
   *
   * @param req the {@link HttpServletRequest} to validate
   * @throws WebMethodParameterException if the request method does not support a request body
   */
  public static void validateRequestMethod(HttpServletRequest req) {
    if (METHODS_WITHOUT_BODY.contains(req.getMethod())) {
      log.error("{} request not allowed for @RequestBody parameter.", req.getMethod());
      throw new WebMethodParameterException(
          "%s http method not support request body".formatted(req.getMethod()));
    }
  }

  /**
   * Validates the type of a request parameter. Throws a {@link WebMethodParameterException} if the parameter
   * type is not allowed (only String and primitive/wrapper types are allowed).
   *
   * @param type the type of the request parameter to validate
   * @throws WebMethodParameterException if the request parameter type is not allowed
   */
  public static void validateRequestParameterType(Class<?> type) {
    if (!ClassUtils.isPrimitiveOrWrapper(type) && !String.class.isAssignableFrom(type)) {
      log.error("Request not allowed with request parameter of type [{}]", type);
      throw new WebMethodParameterException(
          "Error reading request parameter of type [%s]. String and primitive/wrappers allowed only"
              .formatted(type));
    }
  }
}
