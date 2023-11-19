package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.context.annotations.Controller;
import io.github.bobocodebreskul.context.annotations.Delete;
import io.github.bobocodebreskul.context.annotations.Get;
import io.github.bobocodebreskul.context.annotations.Head;
import io.github.bobocodebreskul.context.annotations.Post;
import io.github.bobocodebreskul.context.annotations.Put;
import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import java.lang.annotation.Annotation;
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

    if (methodAnnotation instanceof Get) {
      value = ((Get) methodAnnotation).value();
    }

    if (methodAnnotation instanceof Post) {
      value = ((Post) methodAnnotation).value();
    }

    if (methodAnnotation instanceof Put) {
      value = ((Put) methodAnnotation).value();
    }

    if (methodAnnotation instanceof Delete) {
      value = ((Delete) methodAnnotation).value();
    }

    if (methodAnnotation instanceof Head) {
      value = ((Head) methodAnnotation).value();
    }

    return value;
  }

  public void onStartup(Set<Class<?>> c, ServletContext ctx) {
    Map<String, Map<Class<?>, ControllerMethod>> pathToController = getAllPaths();
    // Register your super servlet
    ServletRegistration.Dynamic servlet = ctx.addServlet("dispatcherServlet",
        new DispatcherServlet(this.container, pathToController));
    servlet.addMapping("/*");
  }

  private Map<String, Map<Class<?>, ControllerMethod>> getAllPaths() {
    Map<String, Map<Class<?>, ControllerMethod>> map = new HashMap<>();

    for (Object bean : container.getAllBeans()) {
      if (bean.getClass().isAnnotationPresent(Controller.class)) {
        Method[] declaredMethods = bean.getClass().getDeclaredMethods();

        List<Method> getMethodList = getMethodList(declaredMethods, Get.class);
        List<Method> postMethodList = getMethodList(declaredMethods, Post.class);
        List<Method> putMethodList = getMethodList(declaredMethods, Put.class);
        List<Method> deleteMethodList = getMethodList(declaredMethods, Delete.class);
        List<Method> headMethodList = getMethodList(declaredMethods, Head.class);

        addMethodsToMap(bean, getMethodList, map, Get.class);
        addMethodsToMap(bean, postMethodList, map, Post.class);
        addMethodsToMap(bean, putMethodList, map, Put.class);
        addMethodsToMap(bean, deleteMethodList, map, Delete.class);
        addMethodsToMap(bean, headMethodList, map, Head.class);
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
      } else {
        controllerMethodMap = new HashMap<>();
      }
      controllerMethodMap.put(annotation, new ControllerMethod(controller, method));
      map.put(path, controllerMethodMap);
    }
  }

  private List<Method> getMethodList(Method[] declaredMethods,
      Class<? extends Annotation> annotation) {
    return Arrays.stream(declaredMethods)
        .filter(method -> method.isAnnotationPresent(annotation)).toList();
  }
}
