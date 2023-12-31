package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import org.slf4j.Logger;

/**
 * Initializes the web container by registering a super servlet.
 * <p>
 * This class is responsible for initializing the web container when the application starts. It
 * registers a super servlet named "dispatcherServlet" and maps it to "/*" in the servlet context.
 * <p>
 * The initialization process involves collecting paths and controllers from a
 * {@link BringContainer} using {@link WebPathScanner}, collecting error handlers from
 * {@link BringContainer} using {@link WebErrorHandlerControllerScanner} and creating an instance of
 * {@link DispatcherServlet} to handle incoming requests.
 */
public class WebContainerInitializer implements ServletContainerInitializer {

  private final static Logger log = LoggerFactory.getLogger(WebContainerInitializer.class);
  private final WebErrorHandlerControllerScanner webErrorHandlerControllerScanner;
  private final WebPathScanner webPathScanner;

  /**
   * Constructs a new instance of {@code WebContainerInitializer} with the specified
   * webPathScanner.
   *
   * @param webErrorHandlerControllerScanner The webErrorHandlerControllerScanner is used for
   *                                         retrieving error handlers
   * @param webPathScanner                   The webPathScanner is used for retrieving paths.
   */
  public WebContainerInitializer(WebErrorHandlerControllerScanner webErrorHandlerControllerScanner,
      WebPathScanner webPathScanner) {
    this.webErrorHandlerControllerScanner = webErrorHandlerControllerScanner;
    this.webPathScanner = webPathScanner;
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
    // Register your super servlet
    try {
      ctx.addServlet("dispatcherServlet",
              new DispatcherServlet(
                  new HttpRequestMapper(),
                  webErrorHandlerControllerScanner.getAllWebErrorHandlerControllers(),
                  webPathScanner.getAllPaths()))
          .addMapping("/*");
      log.info("DispatcherServlet registered and mapped to '/*'.");
    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
      log.error("Error occurs during servlet registration: {}", ex.getMessage());
      throw new ServletException(
          "Error occurs during servlet registration due to %s".formatted(ex.getMessage()), ex);
    }
    log.info("Web application initialization completed.");
  }

}
