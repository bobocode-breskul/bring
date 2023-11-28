package io.github.bobocodebreskul.server;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.context.annotations.Get;
import io.github.bobocodebreskul.context.exception.DispatcherServletException;
import io.github.bobocodebreskul.context.exception.MethodInvocationException;
import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
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

  /**
   * Constructs a new instance of {@code DispatcherServlet} with the specified container and
   * path-to-controller mapping.
   *
   * @param pathToControllerMethod A mapping of paths to controller instances.
   */
  public DispatcherServlet(Map<String, Map<String, ControllerMethod>> pathToControllerMethod) {
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
    ObjectMapper mapper = new ObjectMapper();
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
      log.warn("No controller method found for path: {} and HTTP method: {}", pathInfo, req.getMethod());
      return404(resp, mapper);
      return;
    }

    Method method = controllerMethod.method();

    if (isNull(method)) {
      log.warn("No method found for controller method: {}", controllerMethod);
      return404(resp, mapper);
      return;
    }

    try (PrintWriter writer = resp.getWriter()) {
      Object result = method.invoke(controllerMethod.controller());
      writer.println(mapper.writeValueAsString(result));
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
