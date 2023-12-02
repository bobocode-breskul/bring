package io.github.bobocodebreskul.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method serves as an exception handler within a corresponding
 * Controller. This annotation is intended for use in combination with the
 * {@link ErrorHandlerController @ErrorHandlerController} annotation.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @ErrorHandlerController("errorHandlerController")
 * public class SampleController {
 *
 *   @ExceptionHandler
 *   public String handleResourceNotFoundWithSubResourceException(ResourceNotFoundWithSubResourceException ex) {
 *     return ex.getMessage();
 *   }
 *
 *   @ExceptionHandler
 *   public ResponseEntity<String> handleResourceNotFoundWithSubResourceException(GeneralSqlException ex) {
 *     return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
 *   }
 * }}
 * </pre>
 *
 * @see ErrorHandlerController
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandler {

}
