package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated class is a RestController, allowing it to be scanned by the
 * ApplicationContext. A required request mapping value must be specified. Additionally, HTTP
 * Request Method annotations such as {@link Get} or {@link Post} should be added to the methods
 * within the controller. The response from these methods will be automatically converted to JSON
 * and sent as the client's response.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @RestController("restCotroller")
 * public class SampleController {
 *
 *   @Get("/test")
 *   public YourClass doGet() {
 *     return new YourClass();
 *   }
 * }}
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestController {

  /**
   * Represents path to the resource.
   *
   * @return the filled controller path
   */
  String value() default "";
}

