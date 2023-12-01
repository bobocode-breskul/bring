package io.github.bobocodebreskul.server;

import static io.github.bobocodebreskul.config.PropertiesConfiguration.getPropertyAsIntOrDefault;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.registry.BringContainer;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;

/**
 * Utility class for starting and configuring an embedded Tomcat server.
 * <p>
 * This class provides a convenient way to start an embedded Tomcat server with a specified
 * {@link BringContainer}. It configures the server with default settings, such as host, port,
 * context path, and document base.
 */
public class TomcatServer {

  private final static Logger log = LoggerFactory.getLogger(TomcatServer.class);
  private static final String DEFAULT_HOST = "localhost";

  /**
   * The PORT field stores the web server port. If the "server.port" property exists in the
   * application.properties file, a custom port will be set. Otherwise, the default port 8080 will
   * be used.
   */
  private static final int PORT = getPropertyAsIntOrDefault("server.port", 8080);
  private static final String DEFAULT_CONTEXT_PATH = "/";
  private static final String DOC_BASE = ".";
  private static final ExecutorService executor = Executors.newFixedThreadPool(1);
  private static Tomcat tomcat;

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
//    disableTomcatLogs();
    tomcat = new Tomcat();
    tomcat.setHostname(DEFAULT_HOST);
    tomcat.getHost().setAppBase(DOC_BASE);
    tomcat.setPort(PORT);
    tomcat.getConnector();
    setContext(tomcat, container);

    executor.submit(() -> {
      try {
        tomcat.start();
        log.info("Tomcat server started successfully at port %s.".formatted(PORT));
      } catch (Exception exception) {
        log.error("Error while starting Tomcat server", exception);
        log.info("Shutting down the application due to Tomcat server failure.");
        System.exit(1);
      }
      tomcat.getServer().await();
    });

  }

  /**
   * Stops the embedded Tomcat server and releases associated resources.
   */
  public static void stop() {
    try {
      tomcat.stop();
      tomcat.destroy();
    } catch (LifecycleException e) {
      throw new RuntimeException("Error stopping Tomcat server", e);
    }
  }

  /**
   * Retrieves the current status of the embedded Tomcat server.
   * <p>
   * This method returns a string representing the current state of the Tomcat server. The possible
   * states include:
   * <ul>
   * <li>{@code NEW}</li>
   * <li>{@code INITIALIZING}</li>
   * <li>{@code INITIALIZED}</li>
   * <li>{@code STARTING_PREP}</li>
   * <li>{@code STARTING}</li>
   * <li>{@code STARTED}</li>
   * <li>{@code STOPPING_PREP}</li>
   * <li>{@code STOPPING}</li>
   * <li>{@code STOPPED}</li>
   * <li>{@code DESTROYING}</li>
   * <li>{@code DESTROYED}</li>
   * </ul>
   *
   * @return A string representing the current state of the Tomcat server.
   */
  public static String getStatus() {
    return tomcat.getServer().getStateName();
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
        new WebContainerInitializer(
            new WebErrorHandlerControllerScanner(container),
            new WebPathScanner(container)),
        null);
    log.info("Tomcat context set.");
  }

  private static void disableTomcatLogs() {
    System.setProperty("java.util.logging.config.file", getResourcePath("logging.properties"));
  }

  private static String getResourcePath(String fileName) {
    return Optional.ofNullable(TomcatServer.class.getClassLoader().getResource(fileName))
        .map(URL::getPath)
        .orElse(EMPTY);
  }
}
