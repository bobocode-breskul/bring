package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method serves as a GET request handler within a corresponding Controller.
 * This annotation is intended for use in combination with the {@link RestController @RestController} annotation.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @RestController("/sample")
 * public class SampleController {
 *
 *   @Get
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
public @interface Get {

}
