package io.github.bobocodebreskul.server;

import static io.github.bobocodebreskul.context.support.ReflectionUtils.castValue;

import static io.github.bobocodebreskul.server.enums.ResponseStatus.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.exception.DispatcherServletException;
import io.github.bobocodebreskul.context.exception.ResourceNotFoundException;
import io.github.bobocodebreskul.context.exception.WebMethodParameterException;
import io.github.bobocodebreskul.context.registry.BringContainer;
import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.RequestBody;
import io.github.bobocodebreskul.server.annotations.RequestParam;
import io.github.bobocodebreskul.server.enums.RequestMethod;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ClassUtils;

/**
 * Servlet that dispatches incoming HTTP GET requests to the appropriate controller methods.
 * <p>
 * This servlet is responsible for handling HTTP GET requests and dispatching them to the
 * corresponding methods in the controllers provided by the {@link BringContainer}. It uses
 * annotations like {@link Get} to identify the methods that should handle GET requests.
 */
public class DispatcherServlet extends HttpServlet {
  private final static Logger log = LoggerFactory.getLogger(DispatcherServlet.class);
  private final static List<String> METHODS_WITHOUT_BODY = List.of(
      RequestMethod.GET.name(),
      RequestMethod.HEAD.name(),
      RequestMethod.DELETE.name()
  );
  private final Map<Class<?>, ControllerMethod> exceptionToErrorHandlerControllerMethod;
  private final Map<String, Map<String, ControllerMethod>> pathToControllerMethod;
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Constructs a new instance of {@code DispatcherServlet} with the specified container,
   * exception-to-errorController mapping and path-to-controller mapping.
   *
   * @param exceptionToErrorHandlerControllerMethod A mapping of errors to error handler controller
   *                                                instances.
   * @param pathToControllerMethod                  A mapping of paths to controller instances.
   */
  public DispatcherServlet(Map<Class<?>, ControllerMethod> exceptionToErrorHandlerControllerMethod,
      Map<String, Map<String, ControllerMethod>> pathToControllerMethod) {
    this.exceptionToErrorHandlerControllerMethod = exceptionToErrorHandlerControllerMethod;
    this.pathToControllerMethod = pathToControllerMethod;
  }

  /**
   * Custom service method that logs information before and after the request processing.
   *
   * @param request  The HTTP servlet request.
   * @param response The HTTP servlet response.
   */
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String pathInfo = request.getPathInfo().toLowerCase();

    // Log general information about the servlet
    log.debug("DispatcherServlet is processing request for path: {}", pathInfo);

    // Log the start of the request
    log.info("Start processing request for path: {}", pathInfo);

    try {
      // Continue the request processing
      super.service(request, response);
    } finally {
      // Log the completion of the request
      log.info("Finish processing request for path: {}", pathInfo);
    }
  }

  /**
   * Handles HTTP GET requests by dispatching them to the appropriate controller method.
   *
   * @param req  The HTTP servlet request.
   * @param resp The HTTP servlet response.
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    processRequest(req, resp, true);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    processRequest(req, resp, true);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
    processRequest(req, resp, true);
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
    processRequest(req, resp, true);
  }

  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
    processRequest(req, resp, true);
  }

  protected void handleError(HttpServletRequest req, HttpServletResponse resp, Throwable ex) {
    try {
      ControllerMethod controllerMethod =
          exceptionToErrorHandlerControllerMethod.get(ex.getClass());
      if (controllerMethod == null) {
        processResponse(resp, ex);
      } else {
        Object controller = controllerMethod.controller();
        Method method = controllerMethod.method();

        Object result = doMethodInvoke(method, controller, req, ex);
        processResponse(resp, result);
      }

      processRequest(req, resp, false);
    } catch (IOException | InvocationTargetException | IllegalAccessException e) {
      log.error("Error happened during processing handling exception: {}", ex.getMessage(), ex);
      handleError(req, resp, new DispatcherServletException(ex));
    }
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp,
      boolean isNormalFlow) {
    try {
      if (!isNormalFlow) {
        return;
      }

      String pathInfo = req.getPathInfo().toLowerCase();

      // Log debug-level information for request processing details
      log.debug("Processing request for path: {}", pathInfo);

      Map<String, ControllerMethod> controllerMethodMap = getPathControllerMethodMap(pathInfo);
      ControllerMethod controllerMethod = getControllerMethod(req, controllerMethodMap, pathInfo);
      Method method = getMethod(controllerMethod);

      Object[] args = Arrays.stream(method.getParameters())
          .map(parameter -> prepareMethodParameter(parameter, req, resp))
          .toArray();

      Object result = method.invoke(controllerMethod.controller(), args);
      if (result instanceof BringResponse<?>) {

      }
      try (PrintWriter writer = resp.getWriter()) {
        if (!method.getReturnType().equals(Void.class)) {
          writer.println(mapper.writeValueAsString(result));
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        writer.flush();
      }
    } catch (Exception ex) {
      log.error("Failed to process request due to error: [{}]", ex.getMessage(), ex);
      if (ex instanceof InvocationTargetException) {
        handleError(req, resp, ((InvocationTargetException) ex).getTargetException());
      } else {
        handleError(req, resp, ex);
      }
    }
  }

  private Map<String, ControllerMethod> getPathControllerMethodMap(String pathInfo) {
    Map<String, ControllerMethod> controllerMethodMap = pathToControllerMethod.get(pathInfo);

    if (controllerMethodMap == null) {
      log.warn("No controller methods found for path: {}", pathInfo);
      throw new ResourceNotFoundException("Page not found!");
    }
    return controllerMethodMap;
  }

  private static ControllerMethod getControllerMethod(HttpServletRequest req,
      Map<String, ControllerMethod> controllerMethodMap, String pathInfo) {
    ControllerMethod controllerMethod = controllerMethodMap.get(req.getMethod());

    if (controllerMethod == null) {
      log.warn("No controller method found for path: {} and HTTP method: {}",
          pathInfo,
          req.getMethod());
      throw new ResourceNotFoundException("Page not found!");
    }
    return controllerMethod;
  }

  private static Method getMethod(ControllerMethod controllerMethod) {
    Method method = controllerMethod.method();

    if (method == null) {
      log.warn("No method found for controller method: {}", controllerMethod);
      throw new ResourceNotFoundException("Page not found!");
    }
    return method;
  }

  private void processResponse(HttpServletResponse resp, Throwable ex) throws IOException {
    try (PrintWriter writer = resp.getWriter()) {
      if (ex instanceof ResourceNotFoundException) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      writer.println(mapper.writeValueAsString(ex.getMessage()));
      writer.flush();
    }
  }

  private static Object doMethodInvoke(Method method, Object controller, HttpServletRequest req,
      Throwable ex)
      throws IllegalAccessException, InvocationTargetException {
    if (method.getParameterCount() == 1) {
      return method.invoke(controller, ex);
    }
    return getMethodInvokeResult(method, controller, req, ex);
  }

  private void processResponse(HttpServletResponse resp, Object result) throws IOException {
    BringResponse bringResponse = toBringResponse(result);

    String outputResult = mapper.writeValueAsString(bringResponse.getBody());
    int statusCode = bringResponse.getStatus().getStatusCode();
    Map<String, String> allHeaders = bringResponse.getAllHeaders();

    try (PrintWriter writer = resp.getWriter()) {
      resp.setStatus(statusCode);

      for (Map.Entry<String, String> entry : allHeaders.entrySet()) {
        resp.addHeader(entry.getKey(), entry.getValue());
      }

      if (Objects.nonNull(result)) {
        writer.println(outputResult);
        writer.flush();
      }
    }
  }

  private static BringResponse toBringResponse(Object result) {
    if (result instanceof BringResponse response) {
      return response;
    } else {
      return new BringResponse<>(result, null, INTERNAL_SERVER_ERROR);
    }
  }

  private static Object getMethodInvokeResult(Method method, Object controller,
      HttpServletRequest req, Throwable ex)
      throws IllegalAccessException, InvocationTargetException {
    if (Throwable.class.isAssignableFrom(method.getParameterTypes()[0])) {
      return method.invoke(controller, ex, req);
    }
    return method.invoke(controller, req, ex);
  }

  /**
   * Prepare the method parameter. It is get parameter and try to find object which we can inject
   * here mainly it our request and response, but also possible add request body
   *
   * @param parameter The method parameter to be processed.
   * @param req       The HttpServletRequest.
   * @param resp      The HttpServletResponse.
   * @return The prepared parameter instance.
   * @throws WebMethodParameterException If an error occurs during processing.
   */
  private Object prepareMethodParameter(Parameter parameter, HttpServletRequest req,
      HttpServletResponse resp) {
    try {
      log.debug("Processing method parameter: {}", parameter.getName());

      if (isHttpRequest(parameter)) {
        return req;
      }

      if (isHttpResponse(parameter)) {
        return resp;
      }

      if (parameter.isAnnotationPresent(RequestParam.class)) {
        validateRequestParameterType(parameter.getType());
        return getRequestParam(parameter, req);
      }

      if (parameter.isAnnotationPresent(RequestBody.class)) {
        validateRequestMethod(req);
        return getBodyFromRequest(parameter.getType(), req);
      }

      throw new WebMethodParameterException("Unsupported parameter type: " + parameter.getType());

    } catch (Exception e) {
      log.error(
          "Error processing '%s' method parameter with type '%s'.".formatted(parameter.getName(),
              parameter.getType()), e);
      throw new WebMethodParameterException(
          "Error processing '%s' method parameter with type '%s'.".formatted(parameter.getName(),
              parameter.getType()), e);
    }
  }

  private boolean isHttpRequest(Parameter parameter) {
    return HttpServletRequest.class.isAssignableFrom(parameter.getType());
  }

  private boolean isHttpResponse(Parameter parameter) {
    return HttpServletResponse.class.isAssignableFrom(parameter.getType());
  }

  private void validateRequestMethod(HttpServletRequest req) {
    if (METHODS_WITHOUT_BODY.contains(req.getMethod())) {
      log.error("{} request not allowed for @RequestBody parameter.", req.getMethod());
      throw new WebMethodParameterException(
          "%s http method not support request body".formatted(req.getMethod()));
    }
  }

  /**
   * Retrieves the request body for the given type.
   *
   * @param bodyType The type of the expected request body.
   * @param req      The HttpServletRequest.
   * @return The request body object.
   * @throws WebMethodParameterException If an error occurs while retrieving or parsing the request
   *                                     body.
   */
  private Object getBodyFromRequest(Class<?> bodyType, HttpServletRequest req) {
    String body = null;
    try {
      log.debug("Retrieving request body for type: {}", bodyType.getSimpleName());

      body = req.getReader()
          .lines()
          .collect(Collectors.joining(System.lineSeparator()));

      return mapper.readValue(body, bodyType);

    } catch (DatabindException e) {
      log.error(
          "Cannot map body to object due too incorrect data inside expected json but was %n%s".formatted(
              body), e);
      throw new WebMethodParameterException(
          "Cannot map body to object due too incorrect data inside expected json but was %n%s".formatted(
              body), e);
    } catch (IOException e) {
      log.error("Error reading request body from request", e);
      throw new WebMethodParameterException("Error reading request body from request", e);
    }
  }

  private Object getRequestParam(Parameter parameter, HttpServletRequest req) {
    RequestParam annotation = parameter.getAnnotation(RequestParam.class);
    String requestParamName = annotation.value();
    log.debug("Retrieving request parameter with name: {}", requestParamName);
    return Optional.ofNullable(req.getParameter(requestParamName))
        .map(value -> castValue(value, parameter.getType()))
        .orElseGet(() -> {
          log.warn("Cannot find request parameter [{}] in request", requestParamName);
          return null;
        });
  }

  private void validateRequestParameterType(Class<?> type) {
    if (!ClassUtils.isPrimitiveOrWrapper(type) && !String.class.isAssignableFrom(type)) {
      log.error("Request not allowed with request parameter of type [{}]", type);
      throw new WebMethodParameterException(
          "Error reading request parameter of type [%s]. String and primitive/wrappers allowed only"
              .formatted(type));
    }
  }

}
