package io.github.bobocodebreskul.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.context.annotations.Get;
import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DispatcherServlet extends HttpServlet {

  private final BringContainer container;

  private final Map<String, ControllerMethod> pathToControllerMethod;

  public DispatcherServlet(BringContainer container,
      Map<String, ControllerMethod> pathToControllerMethod) {
    this.container = container;
    this.pathToControllerMethod = pathToControllerMethod;
  }

  private static List<Method> getGetMethods(Object bean) {
    return Arrays.stream(bean.getClass().getMethods())
        .filter(m -> m.getAnnotation(Get.class) != null).toList();
  }

  @SneakyThrows
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    String pathInfo = req.getPathInfo();
    ControllerMethod controllerMethod = pathToControllerMethod.get(pathInfo);
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
