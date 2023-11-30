package io.github.bobocodebreskul.demo;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.server.annotations.RestController;
import io.github.bobocodebreskul.server.annotations.Delete;
import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.Head;
import io.github.bobocodebreskul.server.annotations.Post;
import io.github.bobocodebreskul.server.annotations.Put;
import io.github.bobocodebreskul.server.annotations.RequestMapping;

//TODO: remove
@RequestMapping("/pictures")
@RestController
@BringComponent
public class MyController {

  @Get("/first")
  public Greet getFirst() {
    return new Greet("My controller, first");
  }

  @Get("/second2")
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
