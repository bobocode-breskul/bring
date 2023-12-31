package io.github.bobocodebreskul.server;

import static io.github.bobocodebreskul.context.support.ReflectionUtils.castValue;
import static io.github.bobocodebreskul.server.enums.ResponseStatus.INTERNAL_SERVER_ERROR;
import static io.github.bobocodebreskul.server.utils.DispatcherValidationUtils.validateRequestMethod;
import static io.github.bobocodebreskul.server.utils.DispatcherValidationUtils.validateRequestParameterType;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.exception.ResourceNotFoundException;
import io.github.bobocodebreskul.context.registry.BringContainer;
import io.github.bobocodebreskul.server.annotations.RequestBody;
import io.github.bobocodebreskul.server.annotations.RequestMapping;
import io.github.bobocodebreskul.server.annotations.RequestParam;
import io.github.bobocodebreskul.server.exception.DuplicateErrorHandlerException;
import io.github.bobocodebreskul.server.exception.WebMethodParameterException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;

/**
 * Servlet that dispatches incoming HTTP GET requests to the appropriate controller methods.
 * <p>
 * This servlet is responsible for handling HTTP GET requests and dispatching them to the
 * corresponding methods in the controllers provided by the {@link BringContainer}. It uses
 * annotations like {@link RequestMapping} to identify the methods and its path.
 */
public class DispatcherServlet extends HttpServlet {

  private static final Logger log = LoggerFactory.getLogger(DispatcherServlet.class);
  private final HttpRequestMapper httpRequestMapper;
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
  public DispatcherServlet(HttpRequestMapper httpRequestMapper,
      Map<Class<?>, ControllerMethod> exceptionToErrorHandlerControllerMethod,
      Map<String, Map<String, ControllerMethod>> pathToControllerMethod) {
    this.httpRequestMapper = httpRequestMapper;
    this.exceptionToErrorHandlerControllerMethod = exceptionToErrorHandlerControllerMethod;
    this.pathToControllerMethod = pathToControllerMethod;
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

  private static Object doMethodInvoke(Method method, Object controller, HttpServletRequest req,
      Throwable ex)
      throws IllegalAccessException, InvocationTargetException {
    if (method.getParameterCount() == 1) {
      return method.invoke(controller, ex);
    }
    return getMethodInvokeResult(method, controller, req, ex);
  }

  private static BringResponse<Object> toBringResponse(Object result) {
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
      handleError(req, resp, new DuplicateErrorHandlerException(ex));
    }
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp,
      boolean isNormalFlow) {
    try {
      if (!isNormalFlow) {
        return;
      }

      String pathInfo = verifyPath(req.getPathInfo().toLowerCase());

      // Log debug-level information for request processing details
      log.debug("Processing request for path: {}", pathInfo);

      Map<String, ControllerMethod> controllerMethodMap = getPathControllerMethodMap(pathInfo);
      ControllerMethod controllerMethod = getControllerMethod(req, controllerMethodMap, pathInfo);
      Method method = getMethod(controllerMethod);

      Object[] args = Arrays.stream(method.getParameters())
          .map(parameter -> prepareMethodParameter(parameter, req, resp))
          .toArray();

      Object result = method.invoke(controllerMethod.controller(), args);
      if (result instanceof BringResponse<?> bringResponse) {
        httpRequestMapper.writeBringResponseIntoHttpServletResponse(resp, bringResponse);
      } else {
        writeRawResult(resp, method, result);
      }
    } catch (Exception ex) {
      if (ex instanceof InvocationTargetException itex) {
        log.error("Error during request handling", itex.getTargetException());
        handleError(req, resp, itex.getTargetException());
      } else {
        log.error("Error during request handling", ex);
        handleError(req, resp, ex);
      }
    }
  }

  private void writeRawResult(HttpServletResponse resp, Method method, Object result)
      throws IOException {
    try (PrintWriter writer = resp.getWriter()) {
      if (!method.getReturnType().equals(Void.class)) {
        writer.println(mapper.writeValueAsString(result));
      }
      resp.setStatus(HttpServletResponse.SC_OK);
      writer.flush();
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

  private void processResponse(HttpServletResponse resp, Object result) throws IOException {
    BringResponse<Object> bringResponse = toBringResponse(result);

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

      if (isBringRequest(parameter)) {
        return composeBringRequest(parameter, req);
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
          "Error processing '%s' method parameter with type '%s', due to %s".formatted(
              parameter.getName(),
              parameter.getType(), e.getMessage()), e);
    }
  }

  private boolean isHttpRequest(Parameter parameter) {
    return HttpServletRequest.class.isAssignableFrom(parameter.getType());
  }

  private boolean isHttpResponse(Parameter parameter) {
    return HttpServletResponse.class.isAssignableFrom(parameter.getType());
  }

  private boolean isBringRequest(Parameter parameter) {
    return BringRequest.class.isAssignableFrom(parameter.getType());
  }

  private BringRequest<?> composeBringRequest(Parameter parameter, HttpServletRequest req) {
    Type parameterType = parameter.getParameterizedType();
    // Check if it's a parameterized type
    if (parameterType instanceof ParameterizedType parameterizedType) {
      // Get the actual type arguments
      Type[] typeArguments = parameterizedType.getActualTypeArguments();

      // Assuming there's only one type argument
      if (typeArguments.length == 1) {
        // Get the class of the type argument
        Class<?> genericClass = (Class<?>) typeArguments[0];
        return httpRequestMapper.mapHttpServletRequestOnBringRequestEntity(req, genericClass);
      } else {
        log.error("Invalid number of parameterized types found for BringRequest. Expected 1, "
            + "found {}", typeArguments.length);
        throw new WebMethodParameterException(("BringRequest parameter should have only 1 "
            + "parameterized type, found %d").formatted(typeArguments.length));
      }
    }
    log.error("BringRequest type could not be casted to parameterized type, type {}",
        parameterType);
    throw new WebMethodParameterException(("Could not extract parameterized type from BringRequest "
        + "object, type - '%s'").formatted(parameterType));
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

  private String verifyPath(String path) {
    while (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    return path;
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


}
