package io.github.bobocodebreskul.server;

import io.github.bobocodebreskul.config.PropertiesConfiguration;

public class Banner {

  private static final String BRING_BANNER = """
      $$$$$$$\\            $$\\                    \s
      $$  __$$\\           \\__|                   \s
      $$ |  $$ | $$$$$$\\  $$\\ $$$$$$$\\   $$$$$$\\ \s
      $$$$$$$\\ |$$  __$$\\ $$ |$$  __$$\\ $$  __$$\\\s
      $$  __$$\\ $$ |  \\__|$$ |$$ |  $$ |$$ /  $$ |
      $$ |  $$ |$$ |      $$ |$$ |  $$ |$$ |  $$ |
      $$$$$$$  |$$ |      $$ |$$ |  $$ |\\$$$$$$$ |
      \\_______/ \\__|      \\__|\\__|  \\__| \\____$$ |
                                        $$\\   $$ |
                                        \\$$$$$$  |
                                         \\______/\s
        """;

  /**
   * Prints a banner to the console if the banner configuration is enabled.
   */
  public static void printBanner() {
    boolean isEnabled = Boolean.parseBoolean(PropertiesConfiguration
        .getPropertyOrDefault("banner", Boolean.TRUE.toString()));
    if (isEnabled) {
      System.out.println(BRING_BANNER);
    }
  }

}
