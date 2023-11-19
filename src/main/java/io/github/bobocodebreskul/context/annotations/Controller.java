package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which is used to mark types as Controller in order to be scanned by ApplicationContext
 * <br/> You need to specify the required request mapping value
 * <p>Usage:</p>
 * <pre>
 * {@code @BringComponent(name = "componentName")
 * public class MyComponent {
 *
 * }}
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

  /**
   * Represents component name if the component is installed automatically.
   *
   * @return the filled component name if a value has been specified
   */
  String value();
}

