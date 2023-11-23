package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO add java docs
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {

  /**
   * Specifies the name of the scope to use for the annotated component/bean.
   * <p>Defaults to an empty string ({@code ""}) which implies SINGLETON
   * <p>Possible values:
   * <p>-singleton
   * <p>-prototype
   */
  String value() default "";
}
