package io.github.bobocodebreskul.context.scan.utils;

import static java.util.function.Predicate.not;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

public class ScanUtilsImpl implements ScanUtils {

  private final CharSequence[] ILLEGAL_SYMBOLS = {")", "(", "?", "~", "+", "-", "<", ">", "/", ",",
      "^", "!", "@", "#", "$", "%", "^", "&", "*"};

  @Override
  public Set<Class<?>> searchAllClasses(String packagePathPrefix) {
    Reflections reflections = new Reflections(packagePathPrefix,
        Scanners.SubTypes.filterResultsBy((s) -> true));
    return new HashSet<>(reflections.getSubTypesOf(Object.class));
  }

  @Override
  public Set<Class<?>> searchClassesByAnnotationRecursively(String packagePath,
      Class<? extends Annotation> filterByAnnotation) {
    Predicate<Class<?>> filter = clazz -> {
      Queue<Annotation> annotations = new ArrayDeque<>(Arrays.asList(clazz.getAnnotations()));
      Set<Annotation> processedAnnotations = new HashSet<>(annotations);
      while (!annotations.isEmpty()) {
        var annotation = annotations.poll();
        processedAnnotations.add(annotation);
        if (annotation.annotationType().equals(filterByAnnotation)) {
          return true;
        }
        Arrays.stream(annotation.annotationType().getAnnotations())
            .filter(not(processedAnnotations::contains))
            .forEach(annotations::add);
      }

      return false;
    };

    return searchClassesByFilter(packagePath, filter);
  }

  @Override
  public Set<Class<?>> searchClassesByFilter(String packagePath, Predicate<Class<?>> filter) {
    return searchAllClasses(packagePath)
        .stream()
        .filter(filter)
        .collect(Collectors.toSet());
  }

  @Override
  public void validatePackagesToScan(String... packagesToScan) throws IllegalArgumentException {
    if (packagesToScan == null || packagesToScan.length == 0) {
      throw new IllegalArgumentException(
          "Argument [packagesToScan] must contain at least one not null and not empty element");
    }

    if (Arrays.stream(packagesToScan).anyMatch(s -> s == null || s.isEmpty())) {
      throw new IllegalArgumentException(
          "Argument [packagesToScan] must not contain null or empty element");
    }

    if (Arrays.stream(packagesToScan)
        .anyMatch(p -> !p.matches("^[a-zA-Z0-9.]+$"))) {
      throw new IllegalArgumentException(
          "Package name must contain only letters, numbers and symbol [.]");
    }

    if (Arrays.stream(packagesToScan)
        .anyMatch(p -> containsAny(p, ILLEGAL_SYMBOLS))) {
      throw new IllegalArgumentException(
          "Package name must not contain illegal symbols");
    }
  }

  private boolean containsAny(String str, CharSequence[] searchChars) {
    for (CharSequence searchChar : searchChars) {
      if (str.contains(searchChar)) {
        return true;
      }
    }
    return false;
  }
}
