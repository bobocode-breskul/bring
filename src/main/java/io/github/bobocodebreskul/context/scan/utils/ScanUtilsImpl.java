package io.github.bobocodebreskul.context.scan.utils;

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

  public static void main(String[] args) {
    var a = new ScanUtilsImpl();
    a.searchAllClassNames("io.github.bobocodebreskul");
  }

  @Override
  public Set<String> searchAllClassNames(String packagePathPrefix) {
    return searchAllClasses(packagePathPrefix)
      .stream()
      .map(Class::getName)
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Class<?>> searchAllClasses(String packagePathPrefix) {
    Reflections reflections = new Reflections(packagePathPrefix,
      Scanners.SubTypes.filterResultsBy((s) -> true));

    return new HashSet<>(reflections.getSubTypesOf(Object.class));
  }

  @Override
  public Set<Class<?>> searchClassesByFilter(String packagePath, Predicate<Class<?>> filter) {
    return searchAllClasses(packagePath)
      .stream()
      .filter(filter)
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Class<?>> searchClassesByAnnotationRecursively(String packagePath,
    Class<? extends Annotation> filterByAnnotation) {
    Predicate<Class<?>> filter = clazz -> {
      Queue<Annotation> annotations = new ArrayDeque<>(Arrays.asList(clazz.getAnnotations()));
      while (!annotations.isEmpty()) {
        var annotation = annotations.poll();
        if (annotation.annotationType().equals(filterByAnnotation)) {
          return true;
        }
        annotations.addAll(Arrays.asList(annotation.annotationType().getAnnotations()));
      }

      return false;
    };

    return searchClassesByFilter(packagePath, filter);
  }

  @Override
  public Set<Class<?>> searchClassesByAnnotation(String packagePath,
    Class<? extends Annotation> filterByAnnotation) {
    return searchAllClasses(packagePath).stream()
      .filter(
        clazz -> Arrays.stream(clazz.getAnnotations())
          .map(Annotation::annotationType)
          .anyMatch(filterByAnnotation::equals))
      .collect(Collectors.toSet());
  }
}
