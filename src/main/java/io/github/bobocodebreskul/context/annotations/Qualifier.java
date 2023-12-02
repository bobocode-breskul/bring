package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to qualify a component for dependency injection. This annotation is applied to
 * method parameters in components, providing a name for the desired component to be injected.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Qualifier {

  /**
   * Represents name of the component which we want to inject.
   *
   * @return the filled component name if a value has been specified
   */
  String value();
}
