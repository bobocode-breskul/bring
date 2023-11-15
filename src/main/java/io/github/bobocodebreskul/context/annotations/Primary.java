package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which is used to indicate that a bean should be prioritized when there are multiple
 * eligible candidates for autowiring a single-valued dependency. If there is precisely one
 * 'primary' bean among the candidates, it will be the automatically wired value.
 *
 * <p>It can be applied to any class  annotated with {@code @BringComponent}
 *
 * @see BringComponent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Primary {

}
