package io.github.bobocodebreskul.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation used to bind a method parameter to a request parameter in the context of a web
 * application.
 * <p>
 * This annotation is typically used in mapping incoming request parameters to method parameters. It
 * is applied to method parameters to indicate the name of the request parameter that should be
 * bound to the annotated parameter.
 * <p>
 * The value() method represents the name of the request parameter, specified via the annotation.
 * When the annotated method is invoked, the framework uses this name to extract the corresponding
 * parameter from the incoming HTTP request and bind it to the annotated method parameter.
 *
 * <p>Usage:</p>
 * <pre>
 *   {@code
 *   @RestController
 *   public class SampleController {
 *
 *     @Get("/test")
 *     public ResponseEntity doGet(@RequestParam("userId") String userId) {
 *       return new ResponseEntity().ok();
 *     }
 *   }}
 *   </pre>
 *
 * @see RestController
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParam {

  /**
   * Represents the name of the request parameter specified via the annotation.
   *
   * @return The name of the request parameter to bind to the annotated method parameter.
   */
  String value();

}

