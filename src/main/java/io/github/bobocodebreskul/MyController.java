package io.github.bobocodebreskul;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.Controller;
import io.github.bobocodebreskul.context.annotations.Get;

@Controller("/hello")
@BringComponent
public class MyController {

    @Get
    public Greet getHello() {
        return new Greet("Hello, value");
    }

    public class Greet {

        private String value;

        public Greet(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
