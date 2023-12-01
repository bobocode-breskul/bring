package io.github.bobocodebreskul;

import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.RequestMapping;
import io.github.bobocodebreskul.server.annotations.RestController;

@RestController
@RequestMapping("/mother")
public class TestController {

  @Get("/mother")
  public String getTest() {
    return "test";
  }
}
