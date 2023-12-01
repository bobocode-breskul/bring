package io.github.bobocodebreskul.server;

import static io.github.bobocodebreskul.context.support.ReflectionUtils.checkIfAnnotationHasAnnotationType;
import static io.github.bobocodebreskul.context.support.ReflectionUtils.invokeAnnotationMethod;
import static io.github.bobocodebreskul.server.WebPathValidator.validatePath;
import static io.github.bobocodebreskul.server.enums.RequestMethod.GET;

import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.exception.AmbiguousHttpAnnotationException;
import io.github.bobocodebreskul.context.exception.DuplicatePathException;
import io.github.bobocodebreskul.context.registry.BringContainer;
import io.github.bobocodebreskul.server.annotations.Delete;
import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.Head;
import io.github.bobocodebreskul.server.annotations.Post;
import io.github.bobocodebreskul.server.annotations.Put;
import io.github.bobocodebreskul.server.annotations.RequestMapping;
import io.github.bobocodebreskul.server.annotations.RestController;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

/**
 * This class is responsible for scanning and creating web paths. It uses bring container to
 * retrieve all Beans marked as {@link RestController} then retrieve path and http method from
 * public methods marked as {@link Post}, {@link Get}, {@link Delete}, {@link Put}, {@link Head},
 * {@link RequestMapping}
 */
public class WebPathScanner {

  private final static Logger log = LoggerFactory.getLogger(WebPathScanner.class);
  private final BringContainer container;

  /**
   * Constructs a new instance of {@code WebPathScanner} with the specified bring container.
   *
   * @param container The container providing information about controllers.
   */
  public WebPathScanner(BringContainer container) {
    this.container = container;
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
      validatePath(prefixPath);
      log.info("Processing controller class: [{}]", controllerClass.getSimpleName());
      for (Method method : controllerClass.getMethods()) {
        Annotation httpMethodAnnotation = getHttpAnnotation(method);

        if (method.isAnnotationPresent(RequestMapping.class)) {
          RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
          validatePath(requestMapping.value());
          String path = prefixPath.concat(requestMapping.value()).toLowerCase();
          String httpMethodName = getHttpMethodName(requestMapping);
          addPath(pathMap, path, httpMethodName, new ControllerMethod(controllerBean, method));
          log.debug("Added path: [{}] for HTTP method: [{}] for controller method: [{}]",
              path, httpMethodName, method.getName());
        } else if (httpMethodAnnotation == null) {
          log.debug("Skip scanning method, because method [{}] of class [{}]  has no http mapping",
              method.getName(), controllerBean.getClass().getName());
        } else {
          String httpMethodAnnotationValue = invokeAnnotationMethod(httpMethodAnnotation, "value").toString();
          validatePath(httpMethodAnnotationValue);
          String path = prefixPath.concat(httpMethodAnnotationValue).toLowerCase();
          String httpMethodName = getHttpMethodName(httpMethodAnnotation);
          addPath(pathMap, path, httpMethodName, new ControllerMethod(controllerBean, method));
          log.debug("Added path: [{}] for HTTP method: [{}] for controller method: [{}]",
              path, httpMethodName, method.getName());
        }
      }
    }

    return pathMap;
  }

  private static String getPrefixPath(Class<?> controllerClass) {
    if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
      String value = controllerClass.getAnnotation(RequestMapping.class).value();
      log.info("Controller class [{}] has RequestMapping annotation with value: [{}]",
          controllerClass.getSimpleName(), value);
      log.debug("Prefix path for controller class [{}]: [{}]", controllerClass.getSimpleName(),
          value);
      return value;
    } else {
      log.info("Controller class [{}] does not have RequestMapping annotation",
          controllerClass.getSimpleName());
      log.debug("No prefix path for controller class [{}]", controllerClass.getSimpleName());
      return "";
    }
  }

  private static Annotation getHttpAnnotation(Method method) {
    var annotationList = Arrays.stream(method.getAnnotations())
        .filter(
            annotation -> checkIfAnnotationHasAnnotationType(annotation, RequestMapping.class))
        .toList();

    if (annotationList.isEmpty()) {
      log.debug("Method [{}] does not have any HTTP annotations.", method.getName());
      return null;
    }

    if (annotationList.size() > 1) {
      log.error("Method [{}] has more than 1 HTTP annotation", method.getName());
      throw new AmbiguousHttpAnnotationException(
          "Method %s has more then 1 http annotation".formatted(method.getName()));
    }

    Annotation httpAnnotation = annotationList.get(0);
    log.debug("Method [{}] has HTTP annotation: [{}]", method.getName(),
        httpAnnotation.annotationType().getSimpleName());

    validateIfMethodHasOnlyRequestMappingOrHttpMethodMapping(method, httpAnnotation);
    return httpAnnotation;
  }

  private static void validateIfMethodHasOnlyRequestMappingOrHttpMethodMapping(Method method,
      Annotation annotation) {
    if (method.isAnnotationPresent(RequestMapping.class) && annotation != null) {
      log.error("Method [{}] has both RequestMapping and [{}] annotations",
          method.getName(), annotation.annotationType().getSimpleName());
      throw new AmbiguousHttpAnnotationException(
          "Method %s has RequestMapping annotation and %s annotation".formatted(method.getName(),
              annotation.annotationType().getName()));
    }
  }

  private static String getHttpMethodName(RequestMapping requestMapping) {
    String httpMethodName = requestMapping.method().length == 0 ?
        GET.name() : requestMapping.method()[0].name();
    log.debug("HTTP method name for RequestMapping: [{}]", httpMethodName);
    return httpMethodName;
  }

  private static String getHttpMethodName(Annotation annotation) {
    RequestMapping requestMapping = annotation.annotationType().getAnnotation(RequestMapping.class);
    String httpMethodName = getHttpMethodName(requestMapping);
    log.debug("HTTP method name for annotation [{}]: [{}]",
        annotation.annotationType().getSimpleName(), httpMethodName);
    return httpMethodName;
  }

  private static void addPath(Map<String, Map<String, ControllerMethod>> allPath, String path,
      String httpMethodName, ControllerMethod controllerMethod) {
    if (allPath.containsKey(path)) {
      Map<String, ControllerMethod> httpMethodControllerMethodMap = allPath.get(path);
      if (httpMethodControllerMethodMap.containsKey(httpMethodName)) {
        log.error("Duplicate path [{}] for http method [{}] detected", path, httpMethodName);
        throw new DuplicatePathException(
            "Duplicate path %s for http method %s detected".formatted(path, httpMethodName));
      } else {
        httpMethodControllerMethodMap.put(httpMethodName, controllerMethod);
        log.debug("Added path: [{}] for HTTP method: [{}] for controller method: [{}]",
            path, httpMethodName, controllerMethod.method().getName());
      }
    } else {
      allPath.put(path, new HashMap<>(Map.of(httpMethodName, controllerMethod)));
      log.debug("Added new path: [{}] for HTTP method: [{}] for controller method: [{}]",
          path, httpMethodName, controllerMethod.method().getName());
    }
  }

  private List<Object> getControllerBeans() {
    List<Object> controllerBeans = container.getAllBeans()
        .stream()
        .filter(bean -> bean.getClass().isAnnotationPresent(RestController.class))
        .toList();
    log.debug("Retrieved [{}] controller beans", controllerBeans.size());
    return controllerBeans;
  }

}
