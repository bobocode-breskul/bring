package io.github.bobocodebreskul.demointegration;

import io.github.bobocodebreskul.context.annotations.BringBean;
import io.github.bobocodebreskul.context.annotations.BringConfiguration;

@BringConfiguration
public class Configuration {

  @BringBean
  public String configBean() {
    return "I config bean";
  }

}
