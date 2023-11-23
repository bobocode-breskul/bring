package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method serves as a POST request handler within a corresponding Controller.
 * This annotation is designed for use in conjunction with the {@link RestController @RestController} annotation.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @RestController("/sample")
 * public class SampleController {
 *
 *   @Post
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
public @interface Post {

}
