package io.github.bobocodebreskul;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.registry.BringContainer;

@BringComponent
public class Main {
    public static void main(String[] args) {
        BringContainer run = BringContainer.run("io.github.bobocodebreskul");
        Main bean = (Main) run.getBean("main");

        System.out.println(bean.hello());

    }

    public String hello() {
        return "Hello Breskul";
    }
}
