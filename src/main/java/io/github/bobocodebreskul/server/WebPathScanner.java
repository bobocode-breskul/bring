package io.github.bobocodebreskul.server;

import static io.github.bobocodebreskul.context.support.ReflectionUtils.checkIfAnnotationHasAnnotationType;
import static io.github.bobocodebreskul.context.support.ReflectionUtils.invokeAnnotationMethod;
import static io.github.bobocodebreskul.server.enums.RequestMethod.GET;

import io.github.bobocodebreskul.context.annotations.Controller;
import io.github.bobocodebreskul.context.annotations.RequestMapping;
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

//TODO: Add java docs
@Slf4j
public class WebPathScanner {

  private final BringContainer container;

  /**
   * Constructs a new instance of {@code WebPathScanner} with the specified container.
   *
   * @param container The container providing information about controllers.
   */
  public WebPathScanner(BringContainer container) {
    this.container = container;
  }

  //TODO: Implement path validator. It should validate if RequestMapping or HTTP Method has valid path (Create correct regex)
  //TODO: Add test when container does not have controller beans then WebPathScanner should not return paths
  //TODO: Add test when container has controller beans but controller's methods do not have HTTP mapping annotations then WebPathScanner than should not return paths
  //TODO: Add test when container has controller beans but controller's method has several HTTP mapping annotations than should throw AmbiguousHttpAnnotationException
  //TODO: Add test when container has controller beans but controller's method has HTTP mapping annotation and RequestMapping annotation than should throw AmbiguousHttpAnnotationException
  //TODO: Add test when container has controller beans but controller has methods with HTTP mapping and without them then should skip methods without HTTP mapping annotation
  //TODO: Add test for happy flow
  //TODO: Add java docs
  public Map<String, Map<String, ControllerMethod>> getAllPaths()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    Map<String, Map<String, ControllerMethod>> allPath = new HashMap<>();

    for (Object controllerBean : getControllerBeans()) {
      var controllerClass = controllerBean.getClass();
      String prefixPath = getPrefixPath(controllerClass);

      for (Method method : controllerClass.getMethods()) {
        Annotation httpMethodAnnotation = getHttpAnnotation(method);

        validateIfMethodHasOnlyRequestMappingOrHttpMethodMapping(method, httpMethodAnnotation);

        if (method.isAnnotationPresent(RequestMapping.class)) {
          RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
          String path = prefixPath.concat(requestMapping.value());
          String httpMethodName = getHttpMethodName(requestMapping);
          addPath(allPath, path, httpMethodName, new ControllerMethod(controllerBean, method));
        } else if (httpMethodAnnotation == null) {
          log.debug("Skip scanning method, because method {} of {} class don't has http mapping",
              method.getName(), controllerBean.getClass().getName());
        } else {
          String path = prefixPath.concat(invokeAnnotationMethod(httpMethodAnnotation, "value").toString());
          String httpMethodName = getHttpMethodName(httpMethodAnnotation);

          addPath(allPath, path, httpMethodName, new ControllerMethod(controllerBean, method));
        }
      }
    }

    return allPath;
  }

  private List<Object> getControllerBeans() {
    return container.getAllBeans()
        .stream()
        .filter(bean -> bean.getClass().isAnnotationPresent(Controller.class))
        .toList();
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
      throw new AmbiguousHttpAnnotationException("Method %s has more then 1 http annotation".formatted(method.getName()));
    }

    return annotationList.get(0);
  }

  private static void validateIfMethodHasOnlyRequestMappingOrHttpMethodMapping(Method method, Annotation annotation) {
    if (method.isAnnotationPresent(RequestMapping.class) && annotation != null) {
      throw new AmbiguousHttpAnnotationException("Method %s has RequestMapping annotation and %s annotation".formatted(method.getName(), annotation.annotationType().getName()));
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

}
