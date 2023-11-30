package io.github.bobocodebreskul.context.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


//TODO: write doc

@BringComponent
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BringConfiguration {

  /**
   * Represents component name if the component is installed automatically.
   *
   * @return the filled component name if a value has been specified
   */
  String value() default "";
}
