package io.github.bobocodebreskul.context.scan.utils.scan.scantestsclasses.annotations;

import io.github.bobocodebreskul.context.scan.utils.scan.scantestsclasses.annotations.single.CyclicAnnotationB;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CyclicAnnotationB
public @interface CyclicAnnotationA {

}
