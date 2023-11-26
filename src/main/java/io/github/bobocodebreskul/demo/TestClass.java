package io.github.bobocodebreskul.demo;

import io.github.bobocodebreskul.context.annotations.BringComponent;

// TODO: remove
@BringComponent
public class TestClass {

  public void doWork() {
    System.out.println("Do work in " + TestClass.class.getSimpleName());
  }

}
