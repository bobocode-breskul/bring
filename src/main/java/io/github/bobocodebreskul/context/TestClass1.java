package io.github.bobocodebreskul.context;

import io.github.bobocodebreskul.context.annotations.BringComponent;

@BringComponent
public class TestClass1 {

  public void doWork() {
    System.out.println("Do work in " + TestClass1.class.getSimpleName());
  }

}
