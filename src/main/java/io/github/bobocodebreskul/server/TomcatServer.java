package io.github.bobocodebreskul.server;

import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

public class TomcatServer {
  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 8080;
  private static final String DEFAULT_CONTEXT_PATH = "/";
  private static final String DOC_BASE = ".";
  private static final String ADDITION_WEB_INF_CLASSES = "target/classes";
  private static final String WEB_APP_MOUNT = "/WEB-INF/classes";
  private static final String INTERNAL_PATH = "/";

  public static void run() {
    Tomcat tomcat = new Tomcat();
    tomcat.setHostname(DEFAULT_HOST);
    tomcat.getHost().setAppBase(DOC_BASE);
    tomcat.setPort(DEFAULT_PORT);
    tomcat.getConnector();
    setContext(tomcat);

    try {
      tomcat.start();
    } catch (Exception exception) {
      System.exit(1);
    }

    tomcat.getServer().await();
  }


  private static void setContext(Tomcat tomcat) {
    Context context = tomcat.addWebapp(DEFAULT_CONTEXT_PATH, DOC_BASE);
    File classes = new File(ADDITION_WEB_INF_CLASSES);
    String base = classes.getAbsolutePath();
    WebResourceRoot resources = new StandardRoot(context);
    resources.addPreResources(new DirResourceSet(resources, WEB_APP_MOUNT, base, INTERNAL_PATH));
    context.setResources(resources);
  }
}
