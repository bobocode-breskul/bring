package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify the base packages to be scanned for annotated components
 * within the Bring.
 *
 * <p>When using {@code @BringComponentScan}, you can provide an array of base
 * package names to be scanned. These packages and their sub-packages will be searched for
 * components annotated with stereotypes like {@code @BringComponent}.
 *
 * @author Oleh Yakovenko
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BringComponentScan {

  /**
   * The base package names to be scanned. These packages and their sub-packages will be searched
   * for components annotated with stereotypes like {@code @BringComponent}.
   *
   * @return the base packages to scan
   */
  String[] basePackages() default {};
}
