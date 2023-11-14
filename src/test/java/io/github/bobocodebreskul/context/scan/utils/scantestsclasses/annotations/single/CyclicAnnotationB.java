package io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.single;

import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.CyclicAnnotationA;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CyclicAnnotationA
public @interface CyclicAnnotationB {

}
