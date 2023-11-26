package io.github.bobocodebreskul;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.Qualifier;
import io.github.bobocodebreskul.context.registry.BringContainer;
import java.lang.reflect.Constructor;

@BringComponent
public class Main {

    private final TestInterface testClass1;

    public Main(@Qualifier("bean2") TestInterface testClass1, TestInterface testClass2) {
        this.testClass1 = testClass1;
    }

    public static void main(String[] args) {
        Constructor<?>[] constructors = Main.class.getConstructors();
        System.out.println("constructors.length = " + constructors.length);

        BringContainer run = BringContainer.run("io.github.bobocodebreskul");
        Main bean = (Main) run.getBean("main");

        System.out.println(bean.hello());

    }

    public String hello() {
        testClass1.doWork();
        return "Hello Breskul";
    }
}
