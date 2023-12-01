package io.github.bobocodebreskul.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates that methods will be used for bean creation. This Annotation will be processed only if
 * it used inside class which marked as {@link BringConfiguration}.
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
 * @see BringConfiguration
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BringBean {

}
