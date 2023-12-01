package io.github.bobocodebreskul.demointegration.errorhandler;

import io.github.bobocodebreskul.context.exception.AmbiguousHttpAnnotationException;
import io.github.bobocodebreskul.context.exception.DuplicatePathException;
import io.github.bobocodebreskul.server.annotations.ErrorHandlerController;
import io.github.bobocodebreskul.server.annotations.ExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;

@ErrorHandlerController
public class TestErrorHandlingController {

  @ExceptionHandler
  public String test(RuntimeException ex) {
    return "RuntimeException";
  }

  @ExceptionHandler
  public String test(IllegalArgumentException ex, HttpServletRequest req) {
    return "IllegalArgumentException and HttpServletRequest";
  }

  @ExceptionHandler
  public void test(DuplicatePathException ex, HttpServletRequest req) {
    // do nothing
  }

  @ExceptionHandler
  public String test(HttpServletRequest req, AmbiguousHttpAnnotationException ex) {
    return "HttpServletRequest and AmbiguousHttpAnnotationException";
  }
}
