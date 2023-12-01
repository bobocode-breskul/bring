package io.github.bobocodebreskul.demointegration.controller;

import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.RequestParam;
import io.github.bobocodebreskul.server.annotations.RestController;

@RestController
public class RequestParamController {

  @Get("/testValid")
  public String testValid(@RequestParam("name") String name){
    return name;
  }
}
