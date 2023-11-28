package io.github.bobocodebreskul.context.annotations;

import io.github.bobocodebreskul.server.enums.RequestMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Head annotation marks our method as a Head handler of a corresponding Controller. This annotation
 * is designed for use in conjunction with the {@link RestController @RestController} annotation.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @RestController("controller")
 * public class SampleController {
 *
 *   @Head("/test")
 *   public ResponseEntity doHead() {
 *     return new ResponseEntity().ok();
 *   }
 * }}
 * </pre>
 *
 * @see RestController
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMethod.HEAD)
public @interface Head {

  String value() default "";
}
