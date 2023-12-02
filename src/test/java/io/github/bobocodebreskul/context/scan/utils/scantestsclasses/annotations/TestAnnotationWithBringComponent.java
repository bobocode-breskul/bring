package io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@BringComponent
public @interface TestAnnotationWithBringComponent {

}
