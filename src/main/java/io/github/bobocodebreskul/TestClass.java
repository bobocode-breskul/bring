package io.github.bobocodebreskul;

import io.github.bobocodebreskul.context.annotations.BringComponent;

// TODO: remove
@BringComponent("bean")
public class TestClass implements TestInterface{

  @Override
  public void doWork() {
    System.out.println("Do work in " + TestClass.class.getSimpleName());
  }

}
