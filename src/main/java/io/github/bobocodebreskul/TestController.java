package io.github.bobocodebreskul;

import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.RequestParam;
import io.github.bobocodebreskul.server.annotations.RestController;

@RestController
public class TestController {


  // TODO: 1.
  @Get("/test")
  public int test(@RequestParam("test") int testParam){
    return testParam;
  }
}
