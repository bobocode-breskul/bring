package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.context.annotations.Controller;
import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WebContainerInitializer implements ServletContainerInitializer {

  private final BringContainer container;

  public WebContainerInitializer(BringContainer container) {
    this.container = container;
  }

  public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
    Map<String, Object> pathToController = getAllPaths();
    // Register your super servlet
    ServletRegistration.Dynamic servlet = ctx.addServlet("dispatcherServlet", new DispatcherServlet(this.container, pathToController));
    servlet.addMapping("/*");
  }

  private Map<String, Object> getAllPaths() {
    return container.getAllBeans().stream()
            .filter(obj -> obj.getClass().isAnnotationPresent(Controller.class))
            .collect(Collectors.toMap(obj -> obj.getClass().getAnnotation(Controller.class).value(), Function.identity()));
  }
}
