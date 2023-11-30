package io.github.bobocodebreskul.server.annotations;

import io.github.bobocodebreskul.server.enums.RequestMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Delete annotation marks our method as a Delete handler of a corresponding Controller. This
 * annotation is designed for use in conjunction with the {@link RestController @RestController}
 * annotation.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @RestController("controller")
 * public class SampleController {
 *
 *   @Delete("/test")
 *   public ResponseEntity doDelete() {
 *     return new ResponseEntity().ok();
 *   }
 * }}
 * </pre>
 *
 * @see RestController
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMethod.DELETE)
public @interface Delete {

  String value() default "";
}
