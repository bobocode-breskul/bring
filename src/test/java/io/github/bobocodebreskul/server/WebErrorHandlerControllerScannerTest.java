package io.github.bobocodebreskul.server;


import static io.github.bobocodebreskul.context.utils.TestUtil.getCorrectErrorHandlerController;
import static io.github.bobocodebreskul.context.utils.TestUtil.getErrorHandlerControllerWithAnnotatedMethodParamHas2ExceptionTypeArguments;
import static io.github.bobocodebreskul.context.utils.TestUtil.getErrorHandlerControllerWithAnnotatedMethodParamHas2HttpServletRequestArguments;
import static io.github.bobocodebreskul.context.utils.TestUtil.getErrorHandlerControllerWithAnnotatedMethodParamHasMoreThen2Arguments;
import static io.github.bobocodebreskul.context.utils.TestUtil.getErrorHandlerControllerWithAnnotatedMethodParamHasNoArguments;
import static io.github.bobocodebreskul.context.utils.TestUtil.getErrorHandlerControllerWithAnnotatedMethodParamWithDuplicateErrorHandlers;
import static io.github.bobocodebreskul.context.utils.TestUtil.getErrorHandlerControllerWithAnnotatedPrivateAndPublicMethods;
import static io.github.bobocodebreskul.context.utils.TestUtil.getErrorHandlerControllerWithAnnotatedPrivateMethods;
import static io.github.bobocodebreskul.context.utils.TestUtil.getErrorHandlerControllerWithNotAnnotatedMethods;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.BDDMockito.given;

import io.github.bobocodebreskul.server.exception.AmbiguousHttpAnnotationException;
import io.github.bobocodebreskul.server.exception.DuplicateErrorHandlerException;
import io.github.bobocodebreskul.server.exception.DuplicatePathException;
import io.github.bobocodebreskul.context.exception.MethodValidationException;
import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebErrorHandlerControllerScannerTest {
  @InjectMocks
  private WebErrorHandlerControllerScanner scanner;
  @Mock
  private BringContainer bringContainer;

  @Test
  @DisplayName("When no controller beans are available in BringContainer then path map should be empty")
  @Order(1)
  public void given_BringContainerDoesNotHaveControllerBeans_When_GetAllWebErrorHandlerControllers_Then_ResultMapShouldBeEmpty() {
    given(bringContainer.getAllBeans()).willReturn(List.of("Bean"));

    Map<Class<?>, ControllerMethod> allWebErrorHandlerControllers = scanner.getAllWebErrorHandlerControllers();

    assertThat(allWebErrorHandlerControllers).isEmpty();
  }

  @Test
  @DisplayName("Container has error handler controller beans but does not have methods annotated with exception handler then should return empty map")
  @Order(2)
  public void given_BringContainerHasControllerBeanButWithoutAnnotatedMethodsWithExceptionHandler_When_GetAllWebErrorHandlerControllers_Then_ResultMapShouldBeEmpty() {
    given(bringContainer.getAllBeans()).willReturn(List.of(getErrorHandlerControllerWithNotAnnotatedMethods()));

    Map<Class<?>, ControllerMethod> allWebErrorHandlerControllers = scanner.getAllWebErrorHandlerControllers();

    assertThat(allWebErrorHandlerControllers).isEmpty();
  }

  @Test
  @DisplayName("When container has error handler controller beans but only private methods annotated with exception handler then should return empty map")
  @Order(3)
  public void given_BringContainerHasControllerBeansOnlyPrivateMethodsAnnotatedWithExceptionHandler_When_GetAllPaths_Then_ResultMapShouldBeEmpty() {
    given(bringContainer.getAllBeans()).willReturn(List.of(getErrorHandlerControllerWithAnnotatedPrivateMethods()));

    Map<Class<?>, ControllerMethod> allWebErrorHandlerControllers = scanner.getAllWebErrorHandlerControllers();

    assertThat(allWebErrorHandlerControllers).isEmpty();
  }

  @Test
  @DisplayName("When container has error handler controller beans and have private and public methods annotated with exception handler then should return only public methods in map")
  @Order(4)
  public void given_BringContainerHasControllerBeanWithoutAnnotatedPrivateAndPublicMethodsWithExceptionHandler_When_GetAllWebErrorHandlerControllers_Then_ResultMapShouldHaveErrorHandler()
      throws NoSuchMethodException {
    Object errorController = getErrorHandlerControllerWithAnnotatedPrivateAndPublicMethods();
    given(bringContainer.getAllBeans()).willReturn(List.of(errorController));

    Map<Class<?>, ControllerMethod> allWebErrorHandlerControllers = scanner.getAllWebErrorHandlerControllers();

    assertThat(allWebErrorHandlerControllers).isNotEmpty()
        .size().isEqualTo(1);

    ControllerMethod controllerMethod = allWebErrorHandlerControllers.get(RuntimeException.class);
    assertThat(controllerMethod).isNotNull();
    assertThat(controllerMethod.controller()).isEqualTo(errorController);
    assertThat(controllerMethod.method()).isEqualTo(errorController.getClass().getMethod("hi", RuntimeException.class));
  }

  @Test
  @DisplayName("When container has error handler controller beans but method param has no arguments then should throw MethodValidationException")
  @Order(5)
  public void given_BringContainerHasControllerBeansMethodsAnnotatedWithExceptionHandlerButMethodParamHasNoArguments_When_GetAllWebErrorHandlerControllers_Then_ShouldThrowMethodValidationException() {
    given(bringContainer.getAllBeans()).willReturn(List.of(getErrorHandlerControllerWithAnnotatedMethodParamHasNoArguments()));

    Exception actualException = catchException(() -> scanner.getAllWebErrorHandlerControllers());

    assertThat(actualException)
        .isInstanceOf(MethodValidationException.class)
        .hasMessage("Invalid parameter quantity");
  }

  @Test
  @DisplayName("When container has error handler controller beans but method param has more then 2 arguments then should throw MethodValidationException")
  @Order(6)
  public void given_BringContainerHasControllerBeanWithAnnotatedMethodsButMethodParamsHasMoreThen2arguments_When_GetAllWebErrorHandlerControllers_Then_ShouldThrowMethodValidationException() {
    Object errorController = getErrorHandlerControllerWithAnnotatedMethodParamHasMoreThen2Arguments();
    given(bringContainer.getAllBeans()).willReturn(List.of(errorController));

    Exception actualException = catchException(() -> scanner.getAllWebErrorHandlerControllers());

    assertThat(actualException)
        .isInstanceOf(MethodValidationException.class)
        .hasMessage("Invalid parameter quantity");
  }

  @Test
  @DisplayName("When container has error handler controller beans but method param has 2 exception type arguments then should throw MethodValidationException")
  @Order(7)
  public void given_BringContainerHasControllerBeansMethodsAnnotatedWithExceptionHandlerButMethodParamHas2ExceptionTypeArguments_When_GetAllWebErrorHandlerControllers_Then_ShouldThrowMethodValidationException() {
    given(bringContainer.getAllBeans()).willReturn(List.of(getErrorHandlerControllerWithAnnotatedMethodParamHas2ExceptionTypeArguments()));

    Exception actualException = catchException(() -> scanner.getAllWebErrorHandlerControllers());

    assertThat(actualException)
        .isInstanceOf(MethodValidationException.class)
        .hasMessage("Only 1 exceptions is allowed for errorhandler method");
  }

  @Test
  @DisplayName("When container has error handler controller beans but method param has 2 HttpServletRequest type arguments then should throw MethodValidationException")
  @Order(8)
  public void given_BringContainerHasControllerBeanWithAnnotatedMethodsButMethodParamsHas2HttpServletRequestArguments_When_GetAllWebErrorHandlerControllers_Then_ShouldThrowMethodValidationException() {
    Object errorController = getErrorHandlerControllerWithAnnotatedMethodParamHas2HttpServletRequestArguments();
    given(bringContainer.getAllBeans()).willReturn(List.of(errorController));

    Exception actualException = catchException(() -> scanner.getAllWebErrorHandlerControllers());

    assertThat(actualException)
        .isInstanceOf(MethodValidationException.class)
        .hasMessage("There should be at least 1 HttpServletRequest");
  }

  @Test
  @DisplayName("When container has error handler controller beans and have right defined methods then should return valid controllerMethodMap")
  @Order(9)
  public void given_BringContainerHasControllerBeanWithAnnotatedMethodsAndCorrectAnnotatedMethods_When_GetAllWebErrorHandlerControllers_Then_ShouldReturnResultMap()
      throws NoSuchMethodException {
    Object errorController = getCorrectErrorHandlerController();
    given(bringContainer.getAllBeans()).willReturn(List.of(errorController));

    Map<Class<?>, ControllerMethod> allWebErrorHandlerControllers = scanner.getAllWebErrorHandlerControllers();

    assertThat(allWebErrorHandlerControllers).isNotEmpty()
        .size().isEqualTo(3);

    ControllerMethod controllerMethod1 = allWebErrorHandlerControllers.get(RuntimeException.class);
    assertThat(controllerMethod1).isNotNull();
    assertThat(controllerMethod1.controller()).isEqualTo(errorController);
    assertThat(controllerMethod1.method()).isEqualTo(errorController.getClass().getMethod("hi", RuntimeException.class));

    ControllerMethod controllerMethod2 =
        allWebErrorHandlerControllers.get(AmbiguousHttpAnnotationException.class);
    assertThat(controllerMethod2).isNotNull();
    assertThat(controllerMethod2.controller()).isEqualTo(errorController);
    assertThat(controllerMethod2.method()).isEqualTo(errorController.getClass().getMethod("hello", AmbiguousHttpAnnotationException.class, HttpServletRequest.class));

    ControllerMethod controllerMethod3 = allWebErrorHandlerControllers.get(DuplicatePathException.class);
    assertThat(controllerMethod3).isNotNull();
    assertThat(controllerMethod3.controller()).isEqualTo(errorController);
    assertThat(controllerMethod3.method()).isEqualTo(errorController.getClass().getMethod("golang", HttpServletRequest.class, DuplicatePathException.class));
  }

  @Test
  @DisplayName("When container has error handler controller beans and have right defined methods but have duplicate error handlers then should throw DuplicateErrorHandlerException")
  @Order(10)
  public void _given_BringContainerHasControllerBeanWithAnnotatedMethodsAndCorrectAnnotatedMethodsButHaveDuplicateErrorHandlers_When_GetAllWebErrorHandlerControllers_Then_ShouldThrowDuplicateErrorHandlerException() {
    given(bringContainer.getAllBeans()).willReturn(List.of(getErrorHandlerControllerWithAnnotatedMethodParamWithDuplicateErrorHandlers()));

    Exception actualException = catchException(() -> scanner.getAllWebErrorHandlerControllers());

    String errorClass = RuntimeException.class.getName();

    assertThat(actualException)
        .isInstanceOf(DuplicateErrorHandlerException.class)
        .hasMessage("Error [class %s] is already mapped".formatted(errorClass));
  }
}