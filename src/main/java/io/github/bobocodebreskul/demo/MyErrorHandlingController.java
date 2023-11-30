package io.github.bobocodebreskul.demo;

import io.github.bobocodebreskul.server.annotations.ErrorHandlerController;
import io.github.bobocodebreskul.server.annotations.ExceptionHandler;

//TODO: remove
@ErrorHandlerController
public class MyErrorHandlingController {

  @ExceptionHandler
  public String getFirst(RuntimeException ex) {
    return "hello";
  }

  @ExceptionHandler
  public void getFirst(IllegalArgumentException ex) {
    return;
  }
}
