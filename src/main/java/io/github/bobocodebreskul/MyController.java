package io.github.bobocodebreskul;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.Controller;
import io.github.bobocodebreskul.context.annotations.Get;

@Controller("/hello")
@BringComponent
public class MyController {

  @Get
  public String getHello() {
    return "Hello, world!";
  }
}
