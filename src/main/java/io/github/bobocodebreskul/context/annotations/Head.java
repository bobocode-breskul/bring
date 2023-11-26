package io.github.bobocodebreskul.context.annotations;

import io.github.bobocodebreskul.server.enums.RequestMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Head annotation marks our method as a Head handler of a corresponding Controller
 *
 * @see RestController
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMethod.HEAD)
public @interface Head {

  String value() default "";
}
