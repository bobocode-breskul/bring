package io.github.bobocodebreskul.server;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.context.annotations.Get;
import io.github.bobocodebreskul.context.annotations.RequestBody;
import io.github.bobocodebreskul.context.exception.DispatcherServletException;
import io.github.bobocodebreskul.context.exception.MethodInvocationException;
import io.github.bobocodebreskul.context.exception.WebMethodParameterException;
import io.github.bobocodebreskul.context.registry.BringContainer;
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
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Servlet that dispatches incoming HTTP GET requests to the appropriate controller methods.
 * <p>
 * This servlet is responsible for handling HTTP GET requests and dispatching them to the
 * corresponding methods in the controllers provided by the {@link BringContainer}. It uses
 * annotations like {@link Get} to identify the methods that should handle GET requests.
 */
//TODO: add tests
@Slf4j
public class DispatcherServlet extends HttpServlet {

  private final Map<String, Map<String, ControllerMethod>> pathToControllerMethod;
  private final ObjectMapper mapper;

  /**
   * Constructs a new instance of {@code DispatcherServlet} with the specified container and
   * path-to-controller mapping.
   *
   * @param pathToControllerMethod A mapping of paths to controller instances.
   */
  public DispatcherServlet(Map<String, Map<String, ControllerMethod>> pathToControllerMethod) {
    this.pathToControllerMethod = pathToControllerMethod;
    this.mapper = new ObjectMapper();
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
    processRequest(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    processRequest(req, resp);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
    processRequest(req, resp);
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
    processRequest(req, resp);
  }

  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    String pathInfo = req.getPathInfo().toLowerCase();

    // Log debug-level information for request processing details
    log.debug("Processing request for path: {}", pathInfo);

    Map<String, ControllerMethod> controllerMethodMap = pathToControllerMethod.get(pathInfo);

    if (isNull(controllerMethodMap)) {
      log.warn("No controller methods found for path: {}", pathInfo);
      return404(resp, mapper);
      return;
    }

    ControllerMethod controllerMethod = controllerMethodMap.get(req.getMethod());

    if (isNull(controllerMethod)) {
      log.warn("No controller method found for path: {} and HTTP method: {}", pathInfo,
          req.getMethod());
      return404(resp, mapper);
      return;
    }

    Method method = controllerMethod.method();

    if (isNull(method)) {
      log.warn("No method found for controller method: {}", controllerMethod);
      return404(resp, mapper);
      return;
    }

    Object[] args = Arrays.stream(method.getParameters())
        .map(parameter -> prepareMethodParameter(parameter, req, resp))
        .toArray();

    try (PrintWriter writer = resp.getWriter()) {
      Object result = method.invoke(controllerMethod.controller(), args);
      if (!method.getReturnType().equals(Void.class)) {
        writer.println(mapper.writeValueAsString(result));
      }
      writer.flush();
      resp.setStatus(HttpServletResponse.SC_OK);
    } catch (IOException | IllegalAccessException ex) {
      log.error("Error processing request for path: {}", pathInfo, ex);
      throw new DispatcherServletException(ex);
    } catch (InvocationTargetException ex) {
      log.error("Error invoking method for controller method: {}", controllerMethod, ex);
      throw new MethodInvocationException("Method ", ex);
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
    if (RequestMethod.GET.name().equals(req.getMethod())) {
      log.error("GET request not allowed for @RequestBody parameter.");
      throw new WebMethodParameterException("GET http method not support request body");
    }
  }

  /**
   * Retrieves the request body for the given type.
   *
   * @param bodyType The type of the expected request body.
   * @param req      The HttpServletRequest.
   * @return The request body object.
   * @throws RuntimeException If an error occurs while retrieving or parsing the request body.
   */
  private Object getBodyFromRequest(Class<?> bodyType, HttpServletRequest req) {
    try {
      log.debug("Retrieving request body for type: {}", bodyType.getSimpleName());

      String body = req.getReader()
          .lines()
          .collect(Collectors.joining(System.lineSeparator()));

      return mapper.readValue(body, bodyType);

    } catch (IOException e) {
      log.error("Error reading request body", e);
      throw new WebMethodParameterException("Error reading request body", e);
    }
  }

  private void return404(HttpServletResponse resp, ObjectMapper mapper) {
    try (PrintWriter writer = resp.getWriter()) {
      writer.println(mapper.writeValueAsString("Page not found!"));
      writer.flush();
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } catch (IOException e) {
      log.error("Error returning 404 response", e);
      throw new DispatcherServletException(e);
    }
  }
}
