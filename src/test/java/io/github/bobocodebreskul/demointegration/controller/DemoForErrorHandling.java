package io.github.bobocodebreskul.demointegration.controller;

import io.github.bobocodebreskul.server.exception.AmbiguousHttpAnnotationException;
import io.github.bobocodebreskul.server.exception.DuplicatePathException;
import io.github.bobocodebreskul.context.exception.PropertyNotFoundException;
import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.RequestMapping;
import io.github.bobocodebreskul.server.annotations.RestController;

@RequestMapping("/error")
@RestController
public class DemoForErrorHandling {

  @Get("/runtime")
  public String getFirst() {
    throw new RuntimeException("runtime");
  }

  @Get("/illegalargument")
  public String getSecond() {
    throw new IllegalArgumentException("illegal");
  }

  @Get("/duplicate")
  public String getThird() {
    throw new DuplicatePathException("duplicate");
  }

  @Get("/ambiguos")
  public String getFourth() {
    throw new AmbiguousHttpAnnotationException("duplicate");
  }

  @Get("/property")
  public String getFifth() {
    throw new PropertyNotFoundException("duplicate");
  }

}
