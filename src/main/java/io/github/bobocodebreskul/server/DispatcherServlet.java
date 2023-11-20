package io.github.bobocodebreskul.server;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.context.annotations.Delete;
import io.github.bobocodebreskul.context.annotations.Get;
import io.github.bobocodebreskul.context.annotations.Head;
import io.github.bobocodebreskul.context.annotations.Post;
import io.github.bobocodebreskul.context.annotations.Put;
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

@Slf4j
public class DispatcherServlet extends HttpServlet {

  private final BringContainer container;

  private final Map<String, Map<Class<?>, ControllerMethod>> pathToControllerMethod;

  public DispatcherServlet(BringContainer container,
      Map<String, Map<Class<?>, ControllerMethod>> pathToControllerMethod) {
    this.container = container;
    this.pathToControllerMethod = pathToControllerMethod;
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp,
      Class<?> methodType) {
    PrintWriter writer;
    try {
      ObjectMapper mapper = new ObjectMapper();
      String pathInfo = req.getPathInfo();
      Map<Class<?>, ControllerMethod> controllerMethodMap = pathToControllerMethod.get(pathInfo);
      if (isNull(controllerMethodMap)) {
        return404(resp, mapper);
        return;
      }
      ControllerMethod controllerMethod = controllerMethodMap.get(methodType);
      if (isNull(controllerMethod)) {
        return404(resp, mapper);
        return;
      }
      Method method = controllerMethod.method();
      if (isNull(method)) {
        return404(resp, mapper);
        return;
      }
      writer = resp.getWriter();
      Object result = method.invoke(controllerMethod.controller());
      writer.println(mapper.writeValueAsString(result));
      writer.flush();
      resp.setStatus(HttpServletResponse.SC_OK);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    } catch (InvocationTargetException ex) {
      throw new MethodInvocationException("Method ", ex);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void return404(HttpServletResponse resp, ObjectMapper mapper) {
    try {
      PrintWriter writer = resp.getWriter();
      writer.println(mapper.writeValueAsString("Page not found!"));
      writer.flush();
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    this.processRequest(req, resp, Get.class);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    this.processRequest(req, resp, Post.class);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
    this.processRequest(req, resp, Put.class);
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
    this.processRequest(req, resp, Delete.class);
  }

  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
    this.processRequest(req, resp, Head.class);
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Your interception logic before the request is processed
    log.info("Start request for %s".formatted(request.getPathInfo()));
    // Continue the request processing
    super.service(request, response);

    // Your interception logic after the request is processed
    log.info("Finish request for %s".formatted(request.getPathInfo()));
  }
}
