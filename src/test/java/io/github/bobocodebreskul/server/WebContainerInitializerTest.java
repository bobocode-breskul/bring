package io.github.bobocodebreskul.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

import io.github.bobocodebreskul.context.annotations.RestController;
import io.github.bobocodebreskul.context.registry.BringContainer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebContainerInitializerTest {

  @InjectMocks
  private WebContainerInitializer initializer;
  @Mock
  private BringContainer mockContainer;
  @Mock
  private ServletContext mockServletContext;
  @Mock
  private ServletRegistration.Dynamic mockServletRegistration;

  @Test
  public void givenBringContainerWithControllers_whenOnStartup_thenDispatcherServletConfigured()
      throws ServletException {
    given(mockContainer.getAllBeans()).willReturn(List.of(new SampleController()));
    given(mockServletContext.addServlet(eq("dispatcherServlet"), any(DispatcherServlet.class)))
        .willReturn(mockServletRegistration);

    initializer.onStartup(Collections.emptySet(), mockServletContext);

    then(mockContainer).should().getAllBeans();
    then(mockServletContext).should().addServlet(eq("dispatcherServlet"), any(DispatcherServlet.class));
    then(mockServletRegistration).should().addMapping("/*");
  }

  // Example class annotated with @Controller for testing
  @RestController("/sample")
  private static class SampleController {
  }
}