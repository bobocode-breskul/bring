package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated class is a ErrorHandlerController, allowing it to be scanned by the
 * ApplicationContext. A required Method annotations {@link ExceptionHandler} should be added to the
 * methods within the controller. The response from these methods will be automatically converted to
 * JSON or response wrapper.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @ErrorHandlerController("errorHandlerController")
 * public class SampleController {
 *
 *   @ExceptionHandler(ResourceNotFoundWithSubResourceException.class)
 *   public String handleResourceNotFoundWithSubResourceException(ResourceNotFoundWithSubResourceException ex) {
 *     return ex.getMessage();
 *   }
 *
 *   @ExceptionHandler(GeneralSqlException.class)
 *   public ResponseEntity<String> handleResourceNotFoundWithSubResourceException(GeneralSqlException ex) {
 *     return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
 *   }
 * }}
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@BringComponent
public @interface ErrorHandlerController {

  /**
   * Represents name of error handler controller
   *
   * @return name of error handler controller
   */
  String value() default "";
}
