package io.github.bobocodebreskul.demo;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.Controller;
import io.github.bobocodebreskul.context.annotations.Delete;
import io.github.bobocodebreskul.context.annotations.Get;
import io.github.bobocodebreskul.context.annotations.Head;
import io.github.bobocodebreskul.context.annotations.Post;
import io.github.bobocodebreskul.context.annotations.Put;
import io.github.bobocodebreskul.context.annotations.RequestMapping;

//TODO: remove
@RequestMapping("/pictures/yours")
@Controller("/pictures/yours")
@BringComponent
public class YourController {

  @Get("/first")
  public Greet getFirst() {
    return new Greet("Your controller, first");
  }

  @Get("/second")
  public Greet getSecond() {
    return new Greet("Hello, second");
  }

  @Post("/second")
  public Greet getPostSecond() {
    return new Greet("Post, second");
  }

  @Delete("/second")
  public Greet getDeleteSecond() {
    return new Greet("Delete, second");
  }

  @Put("/second")
  public Greet getPutSecond() {
    return new Greet("Put, second");
  }

  @Head("/second")
  public Greet getHeadSecond() {
    return new Greet("Head, second");
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
