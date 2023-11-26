package io.github.bobocodebreskul;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.Primary;

// TODO: remove
//@BringComponent("bean")
//@Primary
public class TestClass implements TestInterface{

  @Override
  public void doWork() {
    System.out.println("Do work in " + TestClass.class.getSimpleName());
  }

}
