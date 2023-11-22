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
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Servlet that dispatches incoming HTTP GET requests to the appropriate controller methods.
 * <p>
 * This servlet is responsible for handling HTTP GET requests and dispatching them to the
 * corresponding methods in the controllers provided by the {@link BringContainer}. It uses
 * annotations like {@link Get} to identify the methods that should handle GET requests.
 */
@Slf4j
public class DispatcherServlet extends HttpServlet {


  private final ObjectMapper mapper;
  private final BringContainer container;

  private final Map<String, Object> pathToController;

  /**
   * Constructs a new instance of {@code DispatcherServlet} with the specified container and
   * path-to-controller mapping.
   *
   * @param container        The container providing information about controllers.
   * @param pathToController A mapping of paths to controller instances.
   */
  public DispatcherServlet(BringContainer container, Map<String, Object> pathToController) {
    this.mapper = new ObjectMapper();
    this.container = container;
    this.pathToController = pathToController;
  }

  /**
   * Finds a method annotated with {@link Get} in the provided controller bean.
   *
   * @param bean The controller bean to search for the annotated method.
   * @return An optional containing the annotated method if found, or an empty optional otherwise.
   */
  private static Optional<Method> findGetMethod(Object bean) {
    return Arrays.stream(bean.getClass().getMethods())
        .filter(m -> m.getAnnotation(Get.class) != null).findFirst();
  }

  /**
   * Handles HTTP GET requests by dispatching them to the appropriate controller method.
   *
   * @param req  The HTTP servlet request.
   * @param resp The HTTP servlet response.
   */
  @SneakyThrows
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Object bean = pathToController.get(req.getPathInfo());
    Optional<Method> method = findGetMethod(bean);
    Object result;
    if (method.isPresent()) {
      result = method.get().invoke(bean);
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


  /**
   * Custom service method that logs information before and after the request processing.
   *
   * @param request  The HTTP servlet request.
   * @param response The HTTP servlet response.
   */
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
