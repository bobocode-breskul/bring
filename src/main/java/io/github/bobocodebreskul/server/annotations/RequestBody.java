package io.github.bobocodebreskul.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to method parameters in controller method which specified with RequestMapping.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @RestController("controller")
 * public class SampleController {
 *   @Post
 *  public String doPostWithRequestBody(@RequestBody BodyClass body) {
 *    return body;
 *  }
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestBody {

}
