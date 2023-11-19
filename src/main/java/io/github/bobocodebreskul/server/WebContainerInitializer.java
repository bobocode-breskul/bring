package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.context.annotations.Controller;
import io.github.bobocodebreskul.context.annotations.Get;
import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebContainerInitializer implements ServletContainerInitializer {

  private final BringContainer container;

  public WebContainerInitializer(BringContainer container) {
    this.container = container;
  }

  public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
    Map<String, ControllerMethod> pathToController = getAllPaths();
    // Register your super servlet
    ServletRegistration.Dynamic servlet = ctx.addServlet("dispatcherServlet",
        new DispatcherServlet(this.container, pathToController));
    servlet.addMapping("/*");
  }

  private Map<String, ControllerMethod> getAllPaths() {
    Map<String, ControllerMethod> map = new HashMap<>();

    List<Object> controllers = container.getAllBeans().stream()
        .filter(obj -> obj.getClass().isAnnotationPresent(Controller.class)).toList();

    for (Object controller : controllers) {
      Method[] declaredMethods = controller.getClass().getDeclaredMethods();

      List<Method> list = Arrays.stream(declaredMethods)
          .filter(method -> method.isAnnotationPresent(Get.class)).toList();

      for (Method method : list) {
        String value = method.getAnnotation(Get.class).value();
        String path = controller.getClass().getAnnotation(Controller.class).value().concat(value);
        map.put(path, new ControllerMethod(controller, method));
      }
    }
    return map;
  }
}
