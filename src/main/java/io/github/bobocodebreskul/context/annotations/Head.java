package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Head annotation marks our method as a Head handler of a corresponding Controller
 *  @see Controller
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Head {
  String value() default "";
}
