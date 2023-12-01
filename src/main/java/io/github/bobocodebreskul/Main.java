package io.github.bobocodebreskul;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.BringComponentScan;
import io.github.bobocodebreskul.context.registry.BringContainer;

@BringComponentScan
@BringComponent
public class Main {

  private final ObjectMapper mapper;
  public Main(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public static void main(String[] args) {
    BringContainer run = BringContainer.run(Main.class);

    Main bean = (Main) run.getBean(Main.class.getName());
    bean.doHello();
  }

  public void doHello() {
    System.out.println(mapper);
  }
}


