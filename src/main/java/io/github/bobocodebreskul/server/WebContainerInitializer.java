package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.context.annotations.RestController;
import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Initializes the web container by registering a super servlet.
 * <p>
 * This class is responsible for initializing the web container when the application starts. It
 * registers a super servlet named "dispatcherServlet" and maps it to "/*" in the servlet context.
 * <p>
 * The initialization process involves collecting paths and controllers from a
 * {@link BringContainer} and creating an instance of {@link DispatcherServlet} to handle incoming
 * requests.
 */
public class WebContainerInitializer implements ServletContainerInitializer {

  private final BringContainer container;

  /**
   * Constructs a new instance of {@code WebContainerInitializer} with the specified container.
   *
   * @param container The container providing information about controllers.
   */
  public WebContainerInitializer(BringContainer container) {
    this.container = container;
  }


  /**
   * Called when the web application starts.
   * <p>
   * Initializes the web container by registering the super servlet "dispatcherServlet" and mapping
   * it to "/*".
   *
   * @param c   The set of application classes found by the container.
   * @param ctx The servlet context of the web application.
   * @throws ServletException If an error occurs during servlet registration.
   */
  public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
    Map<String, Object> pathToController = getAllPaths();
    // Register your super servlet
    ServletRegistration.Dynamic servlet = ctx.addServlet("dispatcherServlet",
        new DispatcherServlet(this.container, pathToController));
    servlet.addMapping("/*");
  }

  private Map<String, Object> getAllPaths() {
    return container.getAllBeans().stream()
        .filter(obj -> obj.getClass().isAnnotationPresent(RestController.class))
        .collect(Collectors.toMap(obj -> obj.getClass().getAnnotation(RestController.class).value(),
            Function.identity()));
  }
}
