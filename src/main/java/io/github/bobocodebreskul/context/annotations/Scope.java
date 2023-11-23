package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the scope to use for the annotated component or bean.
 * <p>
 * This annotation is used to define the lifecycle and visibility of a bean in the context. It
 * allows specifying the desired scope for a particular component or bean.
 * <p>
 * The {@code @Scope} annotation can be applied to classes marked as Bring components or beans. The
 * specified scope determines how the Bring container manages the instance of the annotated
 * component.
 * <p>
 * If the {@code value} attribute is not explicitly set, the default scope is assumed to be
 * singleton.
 * <p>
 * Possible values for the {@code value} attribute:
 * <ul>
 *   <li>{@code ""} (empty string): Singleton scope (default)</li>
 *   <li>{@code "singleton"}: Singleton scope</li>
 *   <li>{@code "prototype"}: Prototype scope</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @Scope("prototype")
 * public class MyPrototypeBean {
 *     // ...
 * }
 * }
 * </pre>
 *
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {

  /**
   * Specifies the name of the scope to use for the annotated component/bean.
   * <p>Defaults to an empty string ({@code ""}) which implies SINGLETON
   * <p>Possible values:
   * <p>-singleton
   * <p>-prototype
   */
  String value() default "";
}
