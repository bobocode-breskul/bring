package io.github.bobocodebreskul.context.annotations;

import io.github.bobocodebreskul.server.enums.RequestMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method serves as a POST request handler within a corresponding
 * Controller. This annotation is designed for use in conjunction with the
 * {@link RestController @RestController} annotation.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @RestController("controller")
 * public class SampleController {
 *
 *   @Post("/test")
 *   public YourClass doPost() {
 *     return new YourClass();
 *   }
 * }}
 * </pre>
 *
 * @see RestController
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMethod.POST)
public @interface Post {

  String value() default "";

}
