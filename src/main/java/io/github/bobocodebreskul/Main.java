package io.github.bobocodebreskul;

import io.github.bobocodebreskul.context.annotations.BringComponentScan;
import io.github.bobocodebreskul.context.registry.BringContainer;
import io.github.bobocodebreskul.demo.TestClass;
import java.lang.reflect.Constructor;

@BringComponentScan
public class Main {

  private final TestClass testClass1;

  public Main(TestClass testClass1) {
    this.testClass1 = testClass1;
  }

  public static void main(String[] args) {
    BringContainer run = BringContainer.run(Main.class);
    Constructor<?>[] constructors = Main.class.getConstructors();
    System.out.println("constructors.length = " + constructors.length);
    Main bean = (Main) run.getBean("main");

    System.out.println(bean.hello());

  }

  public String hello() {
    testClass1.doWork();
    return "Hello Breskul";
  }
}
