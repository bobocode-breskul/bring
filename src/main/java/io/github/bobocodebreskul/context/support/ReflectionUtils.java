package io.github.bobocodebreskul.context.support;

import static java.util.function.Predicate.not;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods that are useful for reflective access.
 */
@Slf4j
@UtilityClass
public class ReflectionUtils {

  final static String CLASS_ANNOTATION_VALUE_ERROR_MSG_PREFIX = "Exception during [%s] annotation [%s] field value extracting. Class [%s] ";
  final static String METHOD_ANNOTATION_VALUE_ERROR_MSG_PREFIX = "Exception during [%s] annotation [%s] field value extracting. Method [%s] ";

  /**
   * Checks if a specified annotation is present on the given class.
   *
   * @param annotation The annotation type to check for. Must not be null.
   * @param clazz      The class on which to check for the presence of the annotation. Must not be
   *                   null.
   * @return {@code true} if the specified annotation is present on the given class, {@code false}
   * otherwise.
   * @throws NullPointerException If either the {@code annotation} or {@code clazz} parameter is
   *                              {@code null}.
   * @apiNote This method performs a simple check for the presence of the specified annotation on
   * the class. It does not support recursive search for annotations on superclasses or interfaces.
   * The presence of the annotation is determined using the {@link Class#isAnnotationPresent(Class)}
   * method.
   */
  public static boolean isAnnotationPresentForClass(Class<? extends Annotation> annotation,
      Class<?> clazz) {
    Objects.requireNonNull(annotation, "The annotation parameter cannot be null!");
    Objects.requireNonNull(clazz, "The class parameter cannot be null!");
    log.trace("Scanning class {} for@ @{} existence", clazz.getName(), annotation.getSimpleName());
    return clazz.isAnnotationPresent(annotation);
  }

  /**
   * Checks if a specified annotation is present on the given constructor.
   *
   * @param annotation  The annotation type to check for. Must not be null.
   * @param constructor The constructor on which to check for the presence of the annotation. Must
   *                    not be null.
   * @return {@code true} if the specified annotation is present on the given constructor,
   * {@code false} otherwise.
   * @throws NullPointerException If either the {@code annotation} or {@code constructor} parameter
   *                              is {@code null}.
   */
  public static boolean isAnnotationPresentForConstructor(Class<? extends Annotation> annotation,
      Constructor<?> constructor) {
    Objects.requireNonNull(annotation, "The annotation parameter cannot be null!");
    Objects.requireNonNull(constructor, "The constructor parameter cannot be null!");
    return constructor.isAnnotationPresent(annotation);
  }

  /**
   * Retrieves a list of constructors annotated with a specified annotation from the provided array
   * of constructors.
   *
   * @param annotation   The annotation type to filter by. Must not be null.
   * @param constructors The array of constructors to filter. Must not be null.
   * @return A list of constructors annotated with the specified annotation. The list may be empty
   * if no matching constructors are found.
   * @throws NullPointerException If either the {@code annotation} or {@code constructors} parameter
   *                              is {@code null}.
   * @see #isAnnotationPresentForConstructor
   */
  public static List<Constructor<?>> getConstructorsAnnotatedWith(
      Class<? extends Annotation> annotation, Constructor<?>... constructors) {
    return Arrays.stream(constructors)
        .filter(constructor -> isAnnotationPresentForConstructor(annotation, constructor))
        .toList();
  }

  /**
   * Retrieves the default constructor for a given class
   *
   * @param clazz The class for which to retrieve the default constructor. Must not be null.
   * @return The default constructor for the specified class.
   * @throws NullPointerException  If the {@code clazz} parameter is {@code null}.
   * @throws IllegalStateException If no default constructor is found for the specified class.
   */
  public static Constructor<?> getDefaultConstructor(Class<?> clazz) {
    Objects.requireNonNull(clazz, "The class parameter cannot be null!");
    return Arrays.stream(clazz.getDeclaredConstructors())
        .filter(constructor -> constructor.getParameterCount() == 0)
        .findAny()
        .orElseThrow(() -> new IllegalStateException(
            "Not found a default constructor for class [%s]".formatted(clazz.getName())));
  }

  /**
   * Checks if a given class has a default constructor
   *
   * @param clazz The class to check for the presence of a default constructor. Must not be null.
   * @return {@code true} if the specified class has a default constructor, {@code false} otherwise.
   * @throws NullPointerException If the {@code clazz} parameter is {@code null}.
   */
  public static boolean hasDefaultConstructor(Class<?> clazz) {
    Objects.requireNonNull(clazz, "The class parameter cannot be null!");
    return Arrays.stream(clazz.getDeclaredConstructors())
        .anyMatch(constructor -> constructor.getParameterCount() == 0);
  }

  /**
   * Check if provided class has provided annotation.
   *
   * @param clazz              target class
   * @param searchedAnnotation searched annotation.
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
   * @param classType      target class
   * @param annotationType target annotation defined in provided class
   * @param fieldName      annotation field name
   * @param fieldType      class of the target field.
   * @return provided annotation field value.
   * @throws IllegalStateException    if class don't have provided annotation or if annotation don't
   *                                  have fieldName.
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
            CLASS_ANNOTATION_VALUE_ERROR_MSG_PREFIX.formatted(annotationType.getName(), fieldName,
                classType.getName()) + "Got unexpected value type.", castException);
      } catch (Exception exception) {
        throw new IllegalStateException(
            CLASS_ANNOTATION_VALUE_ERROR_MSG_PREFIX.formatted(annotationType.getName(), fieldName,
                classType.getName()), exception);
      }
    }

    throw new IllegalStateException(
        CLASS_ANNOTATION_VALUE_ERROR_MSG_PREFIX.formatted(annotationType.getName(), fieldName,
            classType.getName()) + "Provided class don't have such annotation.");
  }

  /**
   * Method for extracting values from parameter annotations.
   *
   * @param executable            method or constructor for searching annotation.
   * @param targetAnnotation      searched annotation.
   * @param annotationTargetField annotation field name which will be extracted.
   * @param valueType             class type of extracted value
   * @return map of param names and extracted values.
   */
  public static <T> Map<String, T> extractMethodAnnotationValues(Executable executable,
      Class<? extends Annotation> targetAnnotation, String annotationTargetField,
      Class<T> valueType) {
    Parameter[] parameters = executable.getParameters();
    return Arrays.stream(parameters)
        .filter(parameter -> parameter.isAnnotationPresent(targetAnnotation))
        .collect(Collectors.toMap(Parameter::getName,
            parameter -> extractParameterAnnotationValue(parameter, annotationTargetField,
                targetAnnotation, valueType)));
  }

  private static <T> T extractParameterAnnotationValue(Parameter parameter, String targetField,
      Class<? extends Annotation> targetAnnotation, Class<T> valueType) {
    Annotation annotation = parameter.getAnnotation(targetAnnotation);
    try {
      return valueType.cast(annotation.annotationType().getMethod(targetField).invoke(annotation));
    } catch (ClassCastException | NoSuchMethodException e) {
      String methodName = parameter.getDeclaringExecutable().getName();
      throw new IllegalArgumentException(
          METHOD_ANNOTATION_VALUE_ERROR_MSG_PREFIX.formatted(targetAnnotation.getName(),
              targetField,
              methodName), e);
    } catch (IllegalAccessException | InvocationTargetException e) {
      String methodName = parameter.getDeclaringExecutable().getName();
      throw new IllegalStateException(
          METHOD_ANNOTATION_VALUE_ERROR_MSG_PREFIX.formatted(targetAnnotation.getName(),
              targetField,
              methodName), e);
    }
  }

  /**
   * Check if annotation is {@link BringComponent} or contains inside {@link BringComponent}
   * annotation
   *
   * @param annotation provided annotation
   * @return true - if annotation is {@link BringComponent} or has it inside on any depth level.
   */
  public static boolean isComponentAnnotation(Annotation annotation) {
    return annotation.annotationType().equals(BringComponent.class)
        || checkIfClassHasAnnotationRecursively(annotation.annotationType(),
        BringComponent.class);
  }

  /**
   * Check if annotation has annotation type
   *
   * @param annotation     provided annotation
   * @param annotationType provided annotation type
   * @return true - if annotation has annotation type.
   */
  public static boolean checkIfAnnotationHasAnnotationType(Annotation annotation,
      Class<? extends Annotation> annotationType) {
    return annotation.annotationType().isAnnotationPresent(annotationType);
  }

  /**
   * Invokes annotation's method
   *
   * @param annotation provided annotation
   * @param methodName provided method name
   * @throws IllegalAccessException    if this {@code Method} object is enforcing Java language
   *                                   access control and the underlying method is inaccessible.
   * @throws IllegalArgumentException  if the method is an instance method and the specified object
   *                                   argument is not an instance of the class or interface
   *                                   declaring the underlying method (or of a subclass or
   *                                   implementor thereof); if the number of actual and formal
   *                                   parameters differ; if an unwrapping conversion for primitive
   *                                   arguments fails; or if, after possible unwrapping, a
   *                                   parameter value cannot be converted to the corresponding
   *                                   formal parameter type by a method invocation conversion.
   * @throws InvocationTargetException if the underlying method throws an exception.
   */
  public static Object invokeAnnotationMethod(Annotation annotation, String methodName)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    return annotation.annotationType().getMethod(methodName).invoke(annotation);
  }

  /**
   * Converts the given string value to the specified primitive or wrapper type.
   * <p>
   * This method is designed to handle the conversion of string values to various primitive types
   * such as int, double, float, long, short, byte, char, and boolean. It supports both primitive
   * types and their corresponding wrapper classes. If the target type is String, the method returns
   * the input value as is.
   *
   * @param value         The string value to be converted.
   * @param parameterType The target type to which the value should be converted.
   * @return The converted value of the specified type.
   * @throws IllegalArgumentException If the conversion is not supported for the given type or if
   *                                  the string value cannot be parsed into the specified primitive
   *                                  type.
   */
  public static Object castValue(String value, Class<?> parameterType) {
    if (String.class.isAssignableFrom(parameterType)) {
      return value;
    }

    try {
      if (int.class.isAssignableFrom(parameterType) || Integer.class.isAssignableFrom(
          parameterType)) {
        return Integer.valueOf(value);
      } else if (double.class.isAssignableFrom(parameterType) || Double.class.isAssignableFrom(
          parameterType)) {
        return Double.valueOf(value);
      } else if (float.class.isAssignableFrom(parameterType) || Float.class.isAssignableFrom(
          parameterType)) {
        return Float.valueOf(value);
      } else if (long.class.isAssignableFrom(parameterType) || Long.class.isAssignableFrom(
          parameterType)) {
        return Long.valueOf(value);
      } else if (short.class.isAssignableFrom(parameterType) || Short.class.isAssignableFrom(
          parameterType)) {
        return Short.valueOf(value);
      } else if (byte.class.isAssignableFrom(parameterType) || Byte.class.isAssignableFrom(
          parameterType)) {
        return Byte.valueOf(value);
      } else if (char.class.isAssignableFrom(parameterType) || Character.class.isAssignableFrom(
          parameterType)) {
        if (value.length() == 1) {
          return value.charAt(0);
        } else {
          throw new IllegalArgumentException("String value cannot be converted to char: [%s]".formatted(value));
        }
      } else if (boolean.class.isAssignableFrom(parameterType) || Boolean.class.isAssignableFrom(
          parameterType)) {
        return Boolean.valueOf(value);
      } else {
        throw new IllegalArgumentException(
            "Unsupported primitive type: " + parameterType.getName());
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "Failed to convert value of type '%s' to required type '%s' for input string: [\"%s\"]"
              .formatted(String.class.getName(), parameterType, value));
    }
  }
}
