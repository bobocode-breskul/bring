package io.github.bobocodebreskul.context.support;

import java.lang.annotation.Annotation;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ReflectionUtils {

  public static boolean isAnnotationExistsFor(Class<? extends Annotation> annotation, Class<?> clazz) {
    log.trace("Scanning class {} for @{} existence", clazz.getName(), annotation.getSimpleName());
    return clazz.isAnnotationPresent(annotation);
  }

}
