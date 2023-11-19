package io.github.bobocodebreskul;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.Controller;
import io.github.bobocodebreskul.context.annotations.Get;

@Controller("/pictures")
@BringComponent
public class MyController {

    @Get("/first")
    public Greet getFirst() {
        return new Greet("Hello, first");
    }

    @Get("/second")
    public Greet getSecond() {
        return new Greet("Hello, second");
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
