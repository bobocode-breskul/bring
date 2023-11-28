package io.github.bobocodebreskul.demo;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.RequestBody;
import io.github.bobocodebreskul.context.annotations.RestController;
import io.github.bobocodebreskul.context.annotations.Delete;
import io.github.bobocodebreskul.context.annotations.Get;
import io.github.bobocodebreskul.context.annotations.Head;
import io.github.bobocodebreskul.context.annotations.Post;
import io.github.bobocodebreskul.context.annotations.Put;
import io.github.bobocodebreskul.context.annotations.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

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
  public Greet getPostSecond(HttpServletRequest request, @RequestBody String greet) {
    return new Greet("Post, second" + greet);
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
