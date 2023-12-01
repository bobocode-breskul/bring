package io.github.bobocodebreskul;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.context.annotations.BringBean;
import io.github.bobocodebreskul.context.annotations.BringConfiguration;

@BringConfiguration
public class Config {

  @BringBean
  public ObjectMapper mapper() {
    return new ObjectMapper();
  }
}
