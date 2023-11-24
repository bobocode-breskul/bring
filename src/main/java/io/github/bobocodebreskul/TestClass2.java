package io.github.bobocodebreskul;

import io.github.bobocodebreskul.context.annotations.BringComponent;

@BringComponent("bean2")
public class TestClass2 implements TestInterface{

  @Override
  public void doWork() {
    System.out.println("Do work in " + TestClass2.class.getSimpleName());
  }

}
