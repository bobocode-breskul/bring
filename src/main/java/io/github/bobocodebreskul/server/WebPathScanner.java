package io.github.bobocodebreskul.server;

import static io.github.bobocodebreskul.context.support.ReflectionUtils.checkIfAnnotationHasAnnotationType;
import static io.github.bobocodebreskul.context.support.ReflectionUtils.invokeAnnotationMethod;
import static io.github.bobocodebreskul.server.WebPathValidator.validatePath;
import static io.github.bobocodebreskul.server.enums.RequestMethod.GET;

import io.github.bobocodebreskul.context.annotations.Delete;
import io.github.bobocodebreskul.context.annotations.Get;
import io.github.bobocodebreskul.context.annotations.Head;
import io.github.bobocodebreskul.context.annotations.Post;
import io.github.bobocodebreskul.context.annotations.Put;
import io.github.bobocodebreskul.context.annotations.RequestMapping;
import io.github.bobocodebreskul.context.annotations.RestController;
import io.github.bobocodebreskul.context.exception.AmbiguousHttpAnnotationException;
import io.github.bobocodebreskul.context.exception.DuplicatePathException;
import io.github.bobocodebreskul.context.registry.BringContainer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for scanning and creating web paths. It uses bring container to
 * retrieve all Beans marked as {@link RestController} then retrieve path and http method from
 * public methods marked as {@link Post}, {@link Get}, {@link Delete}, {@link Put}, {@link Head},
 * {@link RequestMapping}
 */
@Slf4j
public class WebPathScanner {

  private final BringContainer container;

  /**
   * Constructs a new instance of {@code WebPathScanner} with the specified bring container.
   *
   * @param container The container providing information about controllers.
   */
  public WebPathScanner(BringContainer container) {
    this.container = container;
  }

  private static String getPrefixPath(Class<?> controllerClass) {
    return controllerClass.isAnnotationPresent(RequestMapping.class) ?
        controllerClass.getAnnotation(RequestMapping.class).value() : "";
  }

  private static Annotation getHttpAnnotation(Method method) {
    var annotationList = Arrays.stream(method.getAnnotations())
        .filter(
            annotation -> checkIfAnnotationHasAnnotationType(annotation, RequestMapping.class))
        .toList();

    if (annotationList.isEmpty()) {
      return null;
    }

    if (annotationList.size() > 1) {
      throw new AmbiguousHttpAnnotationException(
          "Method %s has more then 1 http annotation".formatted(method.getName()));
    }

    return annotationList.get(0);
  }

  private static void validateIfMethodHasOnlyRequestMappingOrHttpMethodMapping(Method method,
      Annotation annotation) {
    if (method.isAnnotationPresent(RequestMapping.class) && annotation != null) {
      throw new AmbiguousHttpAnnotationException(
          "Method %s has RequestMapping annotation and %s annotation".formatted(method.getName(),
              annotation.annotationType().getName()));
    }
  }

  private static String getHttpMethodName(RequestMapping requestMapping) {
    return requestMapping.method().length == 0 ?
        GET.name() :
        requestMapping.method()[0].name();
  }

  private static String getHttpMethodName(Annotation annotation) {
    RequestMapping requestMapping = annotation.annotationType().getAnnotation(RequestMapping.class);
    return getHttpMethodName(requestMapping);
  }

  private static void addPath(Map<String, Map<String, ControllerMethod>> allPath, String path,
      String httpMethodName, ControllerMethod controllerMethod) {

    if (allPath.containsKey(path)) {
      Map<String, ControllerMethod> httpMethodControllerMethodMap = allPath.get(path);
      if (httpMethodControllerMethodMap.containsKey(httpMethodName)) {
        throw new DuplicatePathException(
            "Duplicate path %s for http method %s detected".formatted(path, httpMethodName));
      } else {
        httpMethodControllerMethodMap.put(httpMethodName, controllerMethod);
      }
    } else {
      allPath.put(path, new HashMap<>(Map.of(httpMethodName, controllerMethod)));
    }
  }

  /**
   * This method is responsible for scanning and creating web paths. It uses bring container to
   * retrieve all Beans marked as {@link RestController} then retrieve path and http method from
   * public methods marked as {@link Post}, {@link Get}, {@link Delete}, {@link Put}, {@link Head},
   * {@link RequestMapping}
   *
   * @return Map of (path, Map of (HTTP method, ControllerMethod))
   * @throws IllegalAccessException    if this {@code Method} object is enforcing Java language
   *                                   access control and the underlying method is inaccessible.
   * @throws IllegalArgumentException  if the method is an instance method and the specified object
   *                                   argument is not an instance of the class or interface
   *                                   declaring the underlying method (or of a subclass or
   *                                   implementor thereof); if the number of actual and formal
   *                                   parameters differ; if an unwrapping conversion for primitive
   *                                   arguments fails; or if, after possible unwrapping, a
   *                                   parameter value cannot be converted to the corresponding
   *                                   formal parameter type by a method invocation conversion.
   * @throws InvocationTargetException if the underlying method throws an exception.
   */
  public Map<String, Map<String, ControllerMethod>> getAllPaths()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    Map<String, Map<String, ControllerMethod>> pathMap = new HashMap<>();

    for (Object controllerBean : getControllerBeans()) {
      var controllerClass = controllerBean.getClass();
      String prefixPath = getPrefixPath(controllerClass);

      for (Method method : controllerClass.getMethods()) {
        Annotation httpMethodAnnotation = getHttpAnnotation(method);

        validateIfMethodHasOnlyRequestMappingOrHttpMethodMapping(method, httpMethodAnnotation);

        if (method.isAnnotationPresent(RequestMapping.class)) {
          RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
          String path = prefixPath.concat(requestMapping.value()).toLowerCase();
          validatePath(path);
          String httpMethodName = getHttpMethodName(requestMapping);
          addPath(pathMap, path, httpMethodName, new ControllerMethod(controllerBean, method));
        } else if (httpMethodAnnotation == null) {
          log.debug("Skip scanning method, because method {} of {} class don't has http mapping",
              method.getName(), controllerBean.getClass().getName());
        } else {
          String path = prefixPath.concat(
              invokeAnnotationMethod(httpMethodAnnotation, "value").toString()).toLowerCase();
          validatePath(path);
          String httpMethodName = getHttpMethodName(httpMethodAnnotation);
          addPath(pathMap, path, httpMethodName, new ControllerMethod(controllerBean, method));
        }
      }
    }

    return pathMap;
  }

  private List<Object> getControllerBeans() {
    return container.getAllBeans()
        .stream()
        .filter(bean -> bean.getClass().isAnnotationPresent(RestController.class))
        .toList();
  }

}
