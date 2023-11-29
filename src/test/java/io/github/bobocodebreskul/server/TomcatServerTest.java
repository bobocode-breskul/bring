package io.github.bobocodebreskul.server;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.bobocodebreskul.config.PropertiesConfiguration;
import io.github.bobocodebreskul.context.annotations.Get;
import io.github.bobocodebreskul.context.annotations.RequestMapping;
import io.github.bobocodebreskul.context.annotations.RestController;
import io.github.bobocodebreskul.context.registry.AnnotatedBeanDefinitionReader;
import io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry;
import io.github.bobocodebreskul.context.registry.BringContainer;
import io.github.bobocodebreskul.context.registry.SimpleBeanDefinitionRegistry;
import io.github.bobocodebreskul.context.support.BeanDependencyUtils;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TomcatServerIntegrationTest {
  private HttpClient httpClient;

  @BeforeEach
  void setUp() throws InterruptedException {
    PropertiesConfiguration.loadProperties();

    BeanDefinitionRegistry definitionRegistry = new SimpleBeanDefinitionRegistry();
    AnnotatedBeanDefinitionReader beanDefinitionReader = new AnnotatedBeanDefinitionReader(
        definitionRegistry);

    beanDefinitionReader.registerBean(Controller.class);
    BringContainer container = new BringContainer(definitionRegistry, new BeanDependencyUtils());

    definitionRegistry.getBeanDefinitions()
        .forEach(beanDefinition -> container.getBean(beanDefinition.getName()));

    TomcatServer.run(container);

    while (!TomcatServer.getStatus().equals("STARTED")) {
      Thread.sleep(100);
    }

    httpClient = HttpClient.newHttpClient();
  }

  @AfterEach
  void tearDown() {
    TomcatServer.stop();
  }

  @Test
  @DisplayName("Test endpoint creation and verify status 200 and body is equal to hello")
  void given_TomcatServer_when_oneControllerRegistered_then_returnBodyWithStatus200ForRegisteredController() throws IOException, InterruptedException {
    String url = "http://localhost:8080/myendpoint";
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("\"hello\"\n");
  }

  @RestController
  @RequestMapping("/myEndpoint")
  public static class Controller {

    @Get
    public String doGet() {
      return "hello";
    }
  }
}