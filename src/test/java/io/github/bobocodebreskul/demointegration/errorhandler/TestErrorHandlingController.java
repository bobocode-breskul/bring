package io.github.bobocodebreskul.demointegration.errorhandler;

import io.github.bobocodebreskul.server.exception.AmbiguousHttpAnnotationException;
import io.github.bobocodebreskul.server.exception.DuplicatePathException;
import io.github.bobocodebreskul.context.exception.PropertyNotFoundException;
import io.github.bobocodebreskul.server.BringResponse;
import io.github.bobocodebreskul.server.annotations.ErrorHandlerController;
import io.github.bobocodebreskul.server.annotations.ExceptionHandler;
import io.github.bobocodebreskul.server.enums.ResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

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

  @ExceptionHandler
  public BringResponse<String> test(PropertyNotFoundException ex) {
    return new BringResponse<>("Hello from BringResponse", Map.of("TestHeader", "TestValue"),
        ResponseStatus.BAD_GATEWAY);
  }
}
