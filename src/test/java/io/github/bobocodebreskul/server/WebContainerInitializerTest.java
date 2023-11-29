package io.github.bobocodebreskul.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebContainerInitializerTest {

  private static final String ERROR_MESSAGE = "error";

  @InjectMocks
  private WebContainerInitializer initializer;
  @Mock
  private WebPathScanner webPathScanner;
  @Mock
  private ControllerMethod controllerMethod;
  @Mock
  private ServletRegistration.Dynamic mockServletRegistration;
  @Mock
  private ServletContext mockServletContext;

  private static Stream<Arguments> provideExceptionsForWebPathScannerTest() {
    return Stream.of(
        Arguments.of(new InvocationTargetException(new RuntimeException(), ERROR_MESSAGE)),
        Arguments.of(new NoSuchMethodException(ERROR_MESSAGE)),
        Arguments.of(new IllegalAccessException(ERROR_MESSAGE))
    );
  }

  @Test
  @DisplayName("DispatcherServlet configured when application started")
  @Order(1)
  public void given_WebPathScannerReturnPaths_When_OnStartup_Then_DispatcherServletConfigured()
      throws ServletException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    given(webPathScanner.getAllPaths()).willReturn(
        Map.of("/test", Map.of("GET", controllerMethod)));

    given(mockServletContext.addServlet(eq("dispatcherServlet"), any(DispatcherServlet.class)))
        .willReturn(mockServletRegistration);

    initializer.onStartup(Collections.emptySet(), mockServletContext);

    then(mockServletContext).should()
        .addServlet(eq("dispatcherServlet"), any(DispatcherServlet.class));
    then(mockServletRegistration).should().addMapping("/*");
    verify(webPathScanner, times(1)).getAllPaths();
  }

  @Order(2)
  @DisplayName("Throw ServletException when WebPathScanner throw TargetException")
  @ParameterizedTest
  @MethodSource("provideExceptionsForWebPathScannerTest")
  public void given_WebPathScannerThrowsInvocationTargetException_When_OnStartup_Then_ShouldThrowServletException(
      Exception exception)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    given(webPathScanner.getAllPaths()).willThrow(exception);

    Exception actualException = catchException(
        () -> initializer.onStartup(Collections.emptySet(), mockServletContext));

    assertThat(actualException)
        .isInstanceOf(ServletException.class)
        .hasMessage("Error occurs during servlet registration due to %s".formatted(ERROR_MESSAGE));
  }
}