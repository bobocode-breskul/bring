package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Initializes the web container by registering a super servlet.
 * <p>
 * This class is responsible for initializing the web container when the application starts. It
 * registers a super servlet named "dispatcherServlet" and maps it to "/*" in the servlet context.
 * <p>
 * The initialization process involves collecting paths and controllers from a
 * {@link BringContainer} using {@link WebPathScanner} and creating an instance of {@link DispatcherServlet} to handle incoming
 * requests.
 */
public class WebContainerInitializer implements ServletContainerInitializer {

  private final WebPathScanner webPathScanner;

  /**
   * Constructs a new instance of {@code WebContainerInitializer} with the specified webPathScanner.
   *
   * @param webPathScanner The webPathScanner is used for retrieving paths.
   */
  public WebContainerInitializer(WebPathScanner webPathScanner) {
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
  //TODO: Revisit current tests
  //TODO: Add test when webPathScanner throws InvocationTargetException
  //TODO: Add test when webPathScanner throws NoSuchMethodException
  //TODO: Add test when webPathScanner throws IllegalAccessException
  public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException{
    // Register your super servlet
    try {
      ctx.addServlet("dispatcherServlet", new DispatcherServlet(webPathScanner.getAllPaths()))
          .addMapping("/*");
    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
      throw new ServletException("Error occurs during servlet registration due to %s".formatted(ex.getMessage()), ex);
    }
  }
}
