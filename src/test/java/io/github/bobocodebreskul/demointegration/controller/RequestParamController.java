package io.github.bobocodebreskul.demointegration.controller;

import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.RequestParam;
import io.github.bobocodebreskul.server.annotations.RestController;

@RestController
public class RequestParamController {

  @Get("/test")
  public String test(@RequestParam("test") String testParam) {
    return testParam;
  }

  @Get("/test/primitive")
  public int test(@RequestParam("test") int testParam) {
    return testParam;
  }

  @Get("/test/wrapper")
  public int test(@RequestParam("test") Integer testParam) {
    return testParam;
  }


  @Get("/test/exception")
  public UnsupportedRequestParamTest test(@RequestParam("test") UnsupportedRequestParamTest testParam) {
    return testParam;
  }

}
