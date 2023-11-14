package io.github.bobocodebreskul.context.support;

import java.lang.annotation.Annotation;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReflectionUtils {

  public static boolean isAnnotationExistsFor(Class<? extends Annotation> annotation, Class<?> clazz) {
    return clazz.isAnnotationPresent(annotation);
  }

}
