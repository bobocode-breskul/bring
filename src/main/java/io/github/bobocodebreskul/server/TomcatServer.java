package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.context.registry.BringContainer;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

/**
 * Utility class for starting and configuring an embedded Tomcat server.
 * <p>
 * This class provides a convenient way to start an embedded Tomcat server with a specified
 * {@link BringContainer}. It configures the server with default settings, such as host, port,
 * context path, and document base.
 */
@Slf4j
public class TomcatServer {

  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 8080;
  private static final String DEFAULT_CONTEXT_PATH = "/";
  private static final String DOC_BASE = ".";

  /**
   * Starts an embedded Tomcat server with the specified {@link BringContainer}.
   * <p>
   * The server is configured with default settings and a web context is set up using the provided
   * {@link BringContainer}. The server will start and wait for incoming requests until manually
   * terminated.
   *
   * @param container The container providing information about controllers.
   */
  public static void run(BringContainer container) {
    log.info("Tomcat server is starting...");
    Tomcat tomcat = new Tomcat();
    tomcat.setHostname(DEFAULT_HOST);
    tomcat.getHost().setAppBase(DOC_BASE);
    tomcat.setPort(DEFAULT_PORT);
    tomcat.getConnector();
    setContext(tomcat, container);
    try {
      tomcat.start();
      log.info("Tomcat server started successfully.");
    } catch (Exception exception) {
      log.error("Error while starting Tomcat server", exception);
      log.info("Shutting down the application due to Tomcat server failure.");
      System.exit(1);
    }
    tomcat.getServer().await();
  }

  /**
   * Configures the Tomcat server with the specified {@link BringContainer}.
   * <p>
   * It adds a servlet container initializer ({@link WebContainerInitializer}) to initialize the web
   * context.
   *
   * @param tomcat    The Tomcat server instance.
   * @param container The BringContainer containing the configuration for the web application.
   */
  private static void setContext(Tomcat tomcat, BringContainer container) {
    Context context = tomcat.addWebapp(DEFAULT_CONTEXT_PATH, DOC_BASE);
    context.addServletContainerInitializer(
        new WebContainerInitializer(new WebPathScanner(container)), null);
    log.info("Tomcat context set.");
  }
}
