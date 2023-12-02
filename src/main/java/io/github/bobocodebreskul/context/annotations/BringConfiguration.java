package io.github.bobocodebreskul.context.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class is a configuration component. In which we can add methods and mark them
 * with {@link BringBean} then from this methods will be used for bean creation.
 *
 * <pre>
 *   {@code
 * @BringConfiguration
 * public class Config {
 *
 *   @BringBean
 *   public YourObject beanName() {
 *     return new YourObject();
 *   }
 * }}
 * </pre>
 *
 * @see BringBean
 * @see BringComponent
 */
@BringComponent
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BringConfiguration {

  /**
   * Represents component name if the component is installed automatically.
   *
   * @return the filled component name if a value has been specified
   */
  String value() default "";
}
