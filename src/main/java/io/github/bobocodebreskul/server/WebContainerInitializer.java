package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.context.annotations.Controller;
import io.github.bobocodebreskul.context.annotations.Delete;
import io.github.bobocodebreskul.context.annotations.Get;
import io.github.bobocodebreskul.context.annotations.Head;
import io.github.bobocodebreskul.context.annotations.Post;
import io.github.bobocodebreskul.context.annotations.Put;
import io.github.bobocodebreskul.context.exception.DuplicatePathException;
import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
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

  private static String getAnnotationValue(Class<? extends Annotation> annotation, Method method) {
    String value = "";
    Annotation methodAnnotation = method.getAnnotation(annotation);
    if (methodAnnotation != null) {
      try {
        Method valueMethod = annotation.getMethod("value");
        value = (String) valueMethod.invoke(methodAnnotation);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    return value;
  }

  public void onStartup(Set<Class<?>> c, ServletContext ctx) {
    Map<String, Map<Class<?>, ControllerMethod>> pathToController = getAllPaths();
    // Register your super servlet
    ctx.addServlet("dispatcherServlet", new DispatcherServlet(this.container, pathToController))
        .addMapping("/*");
  }

  private Map<String, Map<Class<?>, ControllerMethod>> getAllPaths() {
    Map<String, Map<Class<?>, ControllerMethod>> map = new HashMap<>();

    for (Object bean : container.getAllBeans()) {
      if (bean.getClass().isAnnotationPresent(Controller.class)) {
        Method[] declaredMethods = bean.getClass().getDeclaredMethods();

        List<Class<? extends Annotation>> annotationList = List.of(Get.class, Post.class, Put.class,
            Delete.class, Head.class);

        for (var annotation : annotationList) {
          List<Method> methodList = getMethodList(declaredMethods, annotation);
          addMethodsToMap(bean, methodList, map, annotation);
        }
      }
    }
    return map;
  }

  private void addMethodsToMap(Object controller, List<Method> methodList,
      Map<String, Map<Class<?>, ControllerMethod>> map, Class<? extends Annotation> annotation) {
    Map<Class<?>, ControllerMethod> controllerMethodMap;
    for (Method method : methodList) {
      String value = getAnnotationValue(annotation, method);
      String path = controller.getClass().getAnnotation(Controller.class).value().concat(value);
      if (map.containsKey(path)) {
        controllerMethodMap = map.get(path);
        if (controllerMethodMap.containsKey(annotation)) {
          String controller1Name = controllerMethodMap.get(annotation).controller().getClass()
              .getName();
          String method1Name = controllerMethodMap.get(annotation).method().getName();
          String controller2Name = controller.getClass().getName();
          String method2Name = method.getName();
          throw new DuplicatePathException(
              "%n%n\t\tDuplicate path!%n\t\t%s#%s%n\t\t%s#%s%n".formatted(controller1Name,
                  method1Name, controller2Name, method2Name));
        }
      } else {
        controllerMethodMap = new HashMap<>();
      }
      controllerMethodMap.put(annotation, new ControllerMethod(controller, method));
      map.put(path, controllerMethodMap);
    }
  }

  private List<Method> getMethodList(Method[] declaredMethods,
      Class<? extends Annotation> annotation) {
    return Arrays.stream(declaredMethods).filter(method -> method.isAnnotationPresent(annotation))
        .toList();
  }
}
