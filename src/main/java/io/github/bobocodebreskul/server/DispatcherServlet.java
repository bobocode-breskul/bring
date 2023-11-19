package io.github.bobocodebreskul.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.context.annotations.Delete;
import io.github.bobocodebreskul.context.annotations.Get;
import io.github.bobocodebreskul.context.annotations.Head;
import io.github.bobocodebreskul.context.annotations.Post;
import io.github.bobocodebreskul.context.annotations.Put;
import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;
import lombok.SneakyThrows;
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

  @SneakyThrows
  private void processRequest(HttpServletRequest req, HttpServletResponse resp,
      Class<?> methodType) {
    ObjectMapper mapper = new ObjectMapper();
    String pathInfo = req.getPathInfo();
    Map<Class<?>, ControllerMethod> controllerMethodMap = pathToControllerMethod.get(pathInfo);
    ControllerMethod controllerMethod = controllerMethodMap.get(methodType);
    Method method = controllerMethod.method();
    Object result;
    if (method != null) {
      result = method.invoke(controllerMethod.controller());
      resp.setStatus(200);
      PrintWriter writer = resp.getWriter();
      writer.println(mapper.writeValueAsString(result));
      writer.flush();
    } else {
      PrintWriter writer = resp.getWriter();
      writer.println(mapper.writeValueAsString("Page not found!"));
      writer.flush();
      resp.setStatus(404);
    }
  }

  @SneakyThrows
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    this.processRequest(req, resp, Get.class);
  }

  @SneakyThrows
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    this.processRequest(req, resp, Post.class);
  }

  @SneakyThrows
  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
    this.processRequest(req, resp, Put.class);
  }

  @SneakyThrows
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
    this.processRequest(req, resp, Delete.class);
  }

  @SneakyThrows
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
