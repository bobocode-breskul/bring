package io.github.bobocodebreskul.context.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which is used to mark types as BringContext in order to be scanned by
 * {@link BringApplicationContext}
 * <p>Usage:</p>
 * <pre>
 * {@code @BringComponent(name = "componentName")
 * public class MyComponent {
 *
 * }}
 * </pre>
 * <p>
 * This annotation indicates that the class is a "component" and further such classes are candidates
 * for automatic detection when using configuration-based annotation and classpath scanning.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BringComponent {

  /**
   * Represents component name if the component is installed automatically.
   * @return the filled component name if a value has been specified
   */
  String value() default "";

}
