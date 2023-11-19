package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Delete annotation marks our method as a Delete handler of a corresponding Controller
 *
 * @see Controller
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Delete {
  String value() default "";
}
