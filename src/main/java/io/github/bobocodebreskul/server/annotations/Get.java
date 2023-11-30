package io.github.bobocodebreskul.server.annotations;

import io.github.bobocodebreskul.server.enums.RequestMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method serves as a GET request handler within a corresponding
 * Controller. This annotation is intended for use in combination with the
 * {@link RestController @RestController} annotation.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @RestController("controller")
 * public class SampleController {
 *
 *   @Get("/test")
 *   public YourClass doGet() {
 *     return new YourClass();
 *   }
 * }}
 * </pre>
 *
 * @see RestController
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMethod.GET)
public @interface Get {

  String value() default "";
}
