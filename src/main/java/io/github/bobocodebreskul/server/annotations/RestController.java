package io.github.bobocodebreskul.server.annotations;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated class is a RestController, allowing it to be scanned by the
 * ApplicationContext. A required request mapping value must be specified. Additionally, HTTP
 * Request Method annotations {@link RequestMapping} or some more specific annotation should be added to the methods
 * within the controller. The response from these methods will be automatically converted to JSON
 * and sent as the client's response.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @RestController("controllerName")
 * @RequestMapping("/youpath)
 * public class SampleController {
 *
 *   @Get("/test")
 *   public YourClass doGet() {
 *     return new YourClass();
 *   }
 * }}
 * </pre>
 *
 * @see Get
 * @see Post
 * @see Head
 * @see Delete
 * @see Put
 * @see RequestMapping
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@BringComponent
public @interface RestController {

  /**
   * Represents component name if the component is installed automatically.
   *
   * @return the filled component name if a value has been specified
   */
  String value() default "";
}

