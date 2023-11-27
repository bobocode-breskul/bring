package io.github.bobocodebreskul.server;

import static io.github.bobocodebreskul.server.enums.RequestMethod.DELETE;
import static io.github.bobocodebreskul.server.enums.RequestMethod.GET;
import static io.github.bobocodebreskul.server.enums.RequestMethod.HEAD;
import static io.github.bobocodebreskul.server.enums.RequestMethod.POST;
import static io.github.bobocodebreskul.server.enums.RequestMethod.PUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.BDDMockito.given;

import io.github.bobocodebreskul.context.exception.AmbiguousHttpAnnotationException;
import io.github.bobocodebreskul.context.exception.DuplicatePathException;
import io.github.bobocodebreskul.context.exception.WebPathValidationException;
import io.github.bobocodebreskul.context.registry.BringContainer;
import io.github.bobocodebreskul.context.utils.TestUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WebPathScannerTest {

  @InjectMocks
  private WebPathScanner webPathScanner;
  @Mock
  private BringContainer bringContainer;

  @Test
  @Order(1)
  public void given_BringContainerDoesNotHaveControllerBeans_When_GetAllPaths_Then_PathMapShouldBeEmpty()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    given(bringContainer.getAllBeans()).willReturn(List.of("Bean"));

    Map<String, Map<String, ControllerMethod>> allPaths = webPathScanner.getAllPaths();

    assertThat(allPaths).isEmpty();
  }

  @Test
  @Order(2)
  public void given_BringContainerHasControllerBeanButWithOutHTTPAnnotatedMethods_When_GetAllPaths_Then_PathMapShouldBeEmpty()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    given(bringContainer.getAllBeans()).willReturn(
        List.of(TestUtil.retrieveControllerWithoutHttpAnnotationsMethod()));

    Map<String, Map<String, ControllerMethod>> allPaths = webPathScanner.getAllPaths();

    assertThat(allPaths).isEmpty();
  }

  @Test
  @Order(3)
  public void given_BringContainerHasControllerBeanWithSeveralHTTPAnnotationAnnotatedMethod_When_GetAllPaths_Then_PathMapShouldBeEmpty()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    given(bringContainer.getAllBeans()).willReturn(
        List.of(TestUtil.retrieveControllerWithSeveralHttpAnnotationsMethod()));

    Exception actualException = catchException(() -> webPathScanner.getAllPaths());

    assertThat(actualException)
        .isInstanceOf(AmbiguousHttpAnnotationException.class)
        .hasMessage("Method hello has more then 1 http annotation");
  }

  @Test
  @Order(4)
  public void given_BringContainerHasControllerBeanWithHTTPAnnotationAnnotatedAndRequestMappingAnnotatedMethod_When_GetAllPaths_Then_PathMapShouldBeEmpty() {
    given(bringContainer.getAllBeans()).willReturn(
        List.of(TestUtil.retrieveControllerWithSeveralHttpAnnotationsMethod()));

    Exception actualException = catchException(() -> webPathScanner.getAllPaths());

    assertThat(actualException)
        .isInstanceOf(AmbiguousHttpAnnotationException.class)
        .hasMessage("Method hello has more then 1 http annotation");
  }

  @Test
  @Order(5)
  public void given_BringContainerHasControllerBeanHasHTTPAnnotatedMethodsAndNotAnnotated_When_GetAllPaths_Then_ShouldReturnPathOnlyForAnnotatedMethods()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    Object testController = TestUtil.retrieveControllerWithBothHttpAnnotationsMethodAndNot();
    given(bringContainer.getAllBeans()).willReturn(List.of(testController));

    Map<String, Map<String, ControllerMethod>> allPaths = webPathScanner.getAllPaths();

    assertThat(allPaths).size().isEqualTo(1);
    assertThat(allPaths.containsKey("/testpost")).isTrue();

    Map<String, ControllerMethod> httpMethodControllerMethodMap = allPaths.get("/testpost");
    assertThat(httpMethodControllerMethodMap.containsKey(POST.name())).isTrue();

    ControllerMethod controllerMethod = httpMethodControllerMethodMap.get(POST.name());
    assertThat(controllerMethod.controller()).isEqualTo(testController);
    assertThat(controllerMethod.method()).isEqualTo(testController.getClass().getMethod("hello"));
  }

  @Test
  @Order(6)
  public void given_BringContainerHasControllerBeanHasHTTPAnnotatedMethodsWithMultiMapping_When_GetAllPaths_Then_ShouldReturnPaths()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    Object testController = TestUtil.retrieveValidControllerWithValidMethods();
    given(bringContainer.getAllBeans()).willReturn(List.of(testController));

    Map<String, Map<String, ControllerMethod>> allPaths = webPathScanner.getAllPaths();

    assertThat(allPaths).size().isEqualTo(1);
    assertThat(allPaths.containsKey("/test/test")).isTrue();

    Map<String, ControllerMethod> httpMethodControllerMethodMap = allPaths.get("/test/test");
    assertThat(httpMethodControllerMethodMap).size().isEqualTo(5);
    assertThat(httpMethodControllerMethodMap.containsKey(GET.name())).isTrue();
    assertThat(httpMethodControllerMethodMap.containsKey(PUT.name())).isTrue();
    assertThat(httpMethodControllerMethodMap.containsKey(DELETE.name())).isTrue();
    assertThat(httpMethodControllerMethodMap.containsKey(HEAD.name())).isTrue();
    assertThat(httpMethodControllerMethodMap.containsKey(POST.name())).isTrue();

    ControllerMethod controllerMethodGet = httpMethodControllerMethodMap.get(GET.name());
    assertThat(controllerMethodGet.controller()).isEqualTo(testController);
    assertThat(controllerMethodGet.method()).isEqualTo(
        testController.getClass().getMethod("helloGet"));

    ControllerMethod controllerMethodPut = httpMethodControllerMethodMap.get(PUT.name());
    assertThat(controllerMethodPut.controller()).isEqualTo(testController);
    assertThat(controllerMethodPut.method()).isEqualTo(
        testController.getClass().getMethod("helloPut"));

    ControllerMethod controllerMethodDelete = httpMethodControllerMethodMap.get(DELETE.name());
    assertThat(controllerMethodDelete.controller()).isEqualTo(testController);
    assertThat(controllerMethodDelete.method()).isEqualTo(
        testController.getClass().getMethod("helloDelete"));

    ControllerMethod controllerMethodPost = httpMethodControllerMethodMap.get(POST.name());
    assertThat(controllerMethodPost.controller()).isEqualTo(testController);
    assertThat(controllerMethodPost.method()).isEqualTo(
        testController.getClass().getMethod("helloPost"));

    ControllerMethod controllerMethodHead = httpMethodControllerMethodMap.get(HEAD.name());
    assertThat(controllerMethodHead.controller()).isEqualTo(testController);
    assertThat(controllerMethodHead.method()).isEqualTo(
        testController.getClass().getMethod("helloHead"));
  }

  @Test
  @Order(7)
  public void given_BringContainerHasControllerBeanWithHTTPAnnotationAnnotatedMethod_When_GetAllPaths_Then_PathMapShouldBeEmpty() {
    given(bringContainer.getAllBeans()).willReturn(
        List.of(TestUtil.retrieveControllerWithInvalidPathAnnotationsMethod()));

    Exception actualException = catchException(() -> webPathScanner.getAllPaths());

    assertThat(actualException)
        .isInstanceOf(WebPathValidationException.class)
        .hasMessage(WebPathValidator.PATH_SHOULD_START_WITH_SLASH.formatted("test/get"));
  }

  @Test
  @Order(8)
  public void given_given_BringContainerHasControllerBeanHasHTTPAnnotatedMethodsForPrivateMethods_When_GetAllPaths_Then_ShouldReturnPathsForPublicMethods()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    Object testController = TestUtil.retrieveControllerWithAnnotatedPrivateMethods();
    given(bringContainer.getAllBeans()).willReturn(List.of(testController));

    Map<String, Map<String, ControllerMethod>> allPaths = webPathScanner.getAllPaths();

    assertThat(allPaths).size().isEqualTo(1);
    assertThat(allPaths.containsKey("/get")).isTrue();

    Map<String, ControllerMethod> httpMethodControllerMethodMap = allPaths.get("/get");
    assertThat(httpMethodControllerMethodMap).size().isEqualTo(1);
    assertThat(httpMethodControllerMethodMap.containsKey(GET.name())).isTrue();

    ControllerMethod controllerMethodGet = httpMethodControllerMethodMap.get(GET.name());
    assertThat(controllerMethodGet.controller()).isEqualTo(testController);
    assertThat(controllerMethodGet.method()).isEqualTo(
        testController.getClass().getMethod("hello"));
  }

  @Test
  @Order(9)
  public void given_BringContainerHasControllerBeanHasHTTPAnnotatedMethodsWithRequestMapping_When_GetAllPaths_Then_ShouldReturnPaths()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    Object testController = TestUtil.retrieveValidControllerWithRequestMappingAnnotatedMethod();
    given(bringContainer.getAllBeans()).willReturn(List.of(testController));

    Map<String, Map<String, ControllerMethod>> allPaths = webPathScanner.getAllPaths();

    assertThat(allPaths).size().isEqualTo(1);
    assertThat(allPaths.containsKey("/test/post")).isTrue();

    Map<String, ControllerMethod> httpMethodControllerMethodMap = allPaths.get("/test/post");
    assertThat(httpMethodControllerMethodMap).size().isEqualTo(1);
    assertThat(httpMethodControllerMethodMap.containsKey(POST.name())).isTrue();

    ControllerMethod controllerMethodGet = httpMethodControllerMethodMap.get(POST.name());
    assertThat(controllerMethodGet.controller()).isEqualTo(testController);
    assertThat(controllerMethodGet.method()).isEqualTo(
        testController.getClass().getMethod("hello"));
  }

  @Test
  @Order(10)
  public void given_BringContainerHasControllerBeanHasHTTPAnnotatedMethodsWithRequestMappingWithoutHTTPMethod_When_GetAllPaths_Then_ShouldReturnPathsWithGetByDefault()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    Object testController = TestUtil.retrieveValidControllerWithRequestMappingAnnotatedMethodWithoutHTTPMethod();
    given(bringContainer.getAllBeans()).willReturn(List.of(testController));

    Map<String, Map<String, ControllerMethod>> allPaths = webPathScanner.getAllPaths();

    assertThat(allPaths).size().isEqualTo(1);
    assertThat(allPaths.containsKey("/test/get")).isTrue();

    Map<String, ControllerMethod> httpMethodControllerMethodMap = allPaths.get("/test/get");
    assertThat(httpMethodControllerMethodMap).size().isEqualTo(1);
    assertThat(httpMethodControllerMethodMap.containsKey(GET.name())).isTrue();

    ControllerMethod controllerMethodGet = httpMethodControllerMethodMap.get(GET.name());
    assertThat(controllerMethodGet.controller()).isEqualTo(testController);
    assertThat(controllerMethodGet.method()).isEqualTo(
        testController.getClass().getMethod("hello"));
  }

  @Test
  @Order(11)
  public void given_BringContainerHasControllerBeanHasMethodsWithSameHttpMethodAndPath_When_GetAllPaths_Then_ShouldThrowDuplicatePathException() {
    Object testController = TestUtil.retrieveControllerWithMethodsWithSameHttpMethodAndPath();
    given(bringContainer.getAllBeans()).willReturn(List.of(testController));

    Exception actualException = catchException(() -> webPathScanner.getAllPaths());

    assertThat(actualException)
        .isInstanceOf(DuplicatePathException.class)
        .hasMessage("Duplicate path /test for http method GET detected");
  }

  @Test
  @Order(12)
  public void given_BringContainerHasControllerBeanHasMethodsWithSameHttpMethodAndPathWhenUseRequestMapping_When_GetAllPaths_Then_ShouldThrowDuplicatePathException() {
    Object testController = TestUtil.retrieveControllerWithMethodsWithSameHttpMethodAndPathWhenUseRequestMapping();
    given(bringContainer.getAllBeans()).willReturn(List.of(testController));

    Exception actualException = catchException(() -> webPathScanner.getAllPaths());

    assertThat(actualException)
        .isInstanceOf(DuplicatePathException.class)
        .hasMessage("Duplicate path /post for http method POST detected");
  }
}
