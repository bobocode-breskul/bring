package io.github.bobocodebreskul;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.RestController;
import io.github.bobocodebreskul.context.annotations.Get;

@RestController("/hello")
@BringComponent
public class MyController {

  @Get
  public String getHello() {
    return "Hello, world!";
  }
}
