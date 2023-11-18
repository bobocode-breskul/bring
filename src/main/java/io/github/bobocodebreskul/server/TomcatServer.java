package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.context.registry.BringContainer;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class TomcatServer {
  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 8080;
  private static final String DEFAULT_CONTEXT_PATH = "/";
  private static final String DOC_BASE = ".";

  public static void run(BringContainer container) {
    Tomcat tomcat = new Tomcat();
    tomcat.setHostname(DEFAULT_HOST);
    tomcat.getHost().setAppBase(DOC_BASE);
    tomcat.setPort(DEFAULT_PORT);
    tomcat.getConnector();
    setContext(tomcat, container);

    try {
      tomcat.start();
    } catch (Exception exception) {
      System.exit(1);
    }

    tomcat.getServer().await();
  }


  private static void setContext(Tomcat tomcat, BringContainer container) {
    Context context = tomcat.addWebapp(DEFAULT_CONTEXT_PATH, DOC_BASE);
    context.addServletContainerInitializer(new WebContainerInitializer(container), null);
  }
}
