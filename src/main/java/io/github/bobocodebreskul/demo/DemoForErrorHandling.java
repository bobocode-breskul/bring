package io.github.bobocodebreskul.demo;

import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.RequestMapping;
import io.github.bobocodebreskul.server.annotations.RestController;

//TODO: remove
@RequestMapping("/error")
@RestController
public class DemoForErrorHandling {

  @Get("/test")
  public Greet getFirst() {
    throw new RuntimeException("Ooops something goes wrong");
  }

  public class Greet {

    private String value;

    public Greet(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
