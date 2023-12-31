package io.github.bobocodebreskul.server.annotations;

import io.github.bobocodebreskul.server.enums.RequestMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping web requests onto methods in request-handling classes with flexible method
 * signatures.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

  /**
   * Represents path.
   *
   * @return the path to our resource
   */
  String value() default "";

  RequestMethod[] method() default {};

}
