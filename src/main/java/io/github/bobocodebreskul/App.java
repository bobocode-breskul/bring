package io.github.bobocodebreskul;

import io.github.bobocodebreskul.context.annotations.BringComponentScan;
import io.github.bobocodebreskul.context.registry.BringContainer;

@BringComponentScan
public class App {
  public static void main(String[] args) {
    BringContainer.run(App.class);
  }
}
