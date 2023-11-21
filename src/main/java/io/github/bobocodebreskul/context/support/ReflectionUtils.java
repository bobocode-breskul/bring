package io.github.bobocodebreskul.context.support;

import static java.util.function.Predicate.not;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ReflectionUtils {
  final static String ANNOTATION_VALUE_ERROR_MSG_PREFIX = "Exception during [%s] annotation [%s] field value extracting. Class [%s] ";

  public static boolean isAnnotationExistsFor(Class<? extends Annotation> annotation, Class<?> clazz) {
    log.trace("Scanning class {} for @{} existence", clazz.getName(), annotation.getSimpleName());
    return clazz.isAnnotationPresent(annotation);
  }

  /**
   * Check if provided class has provided annotation.
   *
   * @param clazz target class
   * @param searchedAnnotation searched annotation.
   *
   * @return true even if class have the searched annotation or if any of it annotations have the
   * searched annotation inside.
   */
  public static boolean checkIfClassHasAnnotationRecursively(Class<?> clazz,
    Class<? extends Annotation> searchedAnnotation) {
    Queue<Annotation> annotations = new ArrayDeque<>(Arrays.asList(clazz.getAnnotations()));
    Set<Annotation> processedAnnotations = new HashSet<>(annotations);
    while (!annotations.isEmpty()) {
      var annotation = annotations.poll();
      processedAnnotations.add(annotation);
      if (annotation.annotationType().equals(searchedAnnotation)) {
        return true;
      }
      Arrays.stream(annotation.annotationType().getAnnotations())
        .filter(not(processedAnnotations::contains))
        .forEach(annotations::add);
    }
    return false;
  }

  /**
   * Extract annotation field value.
   *
   * @param classType target class
   * @param annotationType target annotation defined in provided class
   * @param fieldName annotation field name
   * @return provided annotation field value.
   *
   * @throws IllegalStateException if class don't have provided annotation or if annotation don't
   *                               have fieldName.
   * @throws IllegalArgumentException if annotation field value is not matches provided fieldType
   */
  public static <T> T getClassAnnotationValue(Class<?> classType,
    Class<? extends Annotation> annotationType, String fieldName, Class<T> fieldType) {
    if (classType.isAnnotationPresent(annotationType)) {
      Annotation annotation = classType.getAnnotation(annotationType);
      try {
        return fieldType.cast(annotation.annotationType().getMethod(fieldName)
          .invoke(annotation));
      } catch (ClassCastException castException) {
        throw new IllegalArgumentException(
          ANNOTATION_VALUE_ERROR_MSG_PREFIX.formatted(annotationType.getName(), fieldName,
            classType.getName()) + "Got unexpected value type.", castException);
      } catch (Exception exception) {
        throw new IllegalStateException(
          ANNOTATION_VALUE_ERROR_MSG_PREFIX.formatted(annotationType.getName(), fieldName,
            classType.getName()), exception);
      }
    }

    throw new IllegalStateException(
      ANNOTATION_VALUE_ERROR_MSG_PREFIX.formatted(annotationType.getName(), fieldName,
        classType.getName()) + "Provided class don't have such annotation.");
  }

  /**
   * Check if annotation is {@link BringComponent} or contains inside {@link BringComponent} annotation
   * @param annotation provided annotation
   * @return true - if annotation is {@link BringComponent} or has it inside on any depth level.
   */
  public static boolean isComponentAnnotation(Annotation annotation) {
    return annotation.annotationType().equals(BringComponent.class)
      || ReflectionUtils.checkIfClassHasAnnotationRecursively(annotation.annotationType(),
      BringComponent.class);
  }
}
