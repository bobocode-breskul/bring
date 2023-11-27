package io.github.bobocodebreskul;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.BringComponentScan;
import io.github.bobocodebreskul.context.registry.BringContainer;

@BringComponentScan
@BringComponent
public class Main {

  public static void main(String[] args) {
    BringContainer.run(Main.class);
  }
}
