package io.github.bobocodebreskul.context.scan.utils;

import static java.util.function.Predicate.not;

import io.github.bobocodebreskul.context.annotations.BringComponentScan;
import io.github.bobocodebreskul.context.exception.BringComponentScanNotFoundException;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

@Slf4j
public class ScanUtilsImpl implements ScanUtils {

  @Override
  public Set<Class<?>> searchAllClasses(String packagePathPrefix) {
    log.trace("Search all classes for {} package", packagePathPrefix);
    validatePackagesToScan(packagePathPrefix);
    Reflections reflections = new Reflections(packagePathPrefix,
        Scanners.SubTypes.filterResultsBy((s) -> true));
    return new HashSet<>(reflections.getSubTypesOf(Object.class));
  }

  @Override
  public Set<Class<?>> searchClassesByAnnotationRecursively(String packagePath,
      Class<? extends Annotation> filterByAnnotation) {
    log.trace("Search all classes for {} package which has @{} annotation",
        packagePath, filterByAnnotation);
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
  public String[] readBasePackages(Class<?> annotatedClass) {
    BringComponentScan bringComponentScan = annotatedClass.getAnnotation(BringComponentScan.class);
    if (bringComponentScan != null) {
      if (bringComponentScan.basePackages().length != 0) {
        String[] basePackages = bringComponentScan.basePackages();
        log.info("Base packages read from @BringComponentScan annotation: " + String.join(", ",
            basePackages));
        return basePackages;
      } else {
        String basePackage = annotatedClass.getPackage().getName();
        log.info("Using default base package: " + basePackage);
        return new String[]{basePackage};
      }
    } else {
      String errorMessage =
          "No @BringComponentScan annotation found on class: " + annotatedClass.getSimpleName()
              + ".class";
      log.warn(errorMessage);
      throw new BringComponentScanNotFoundException(errorMessage);
    }
  }


  /**
   * Valid incoming packages for not existing package, null input, not valid symbols
   *
   * @param packagesToScan packages to scan
   */
  void validatePackagesToScan(String... packagesToScan) throws IllegalArgumentException {
    if (packagesToScan == null || packagesToScan.length == 0) {
      log.error(
          "Argument packages to scan must contain at least one not null and not empty element");
      throw new IllegalArgumentException(
          "Argument [packagesToScan] must contain at least one not null and not empty element");
    }

    if (Arrays.stream(packagesToScan).anyMatch(s -> s == null || s.isBlank())) {
      log.error("Argument packages to scan must not contain null or empty element");
      throw new IllegalArgumentException(
          "Argument [packagesToScan] must not contain null or empty element");
    }

    var optBrokenName = Arrays.stream(packagesToScan)
        .filter(p -> !p.matches("^[a-zA-Z0-9.]+$"))
        .findFirst();
    if (optBrokenName.isPresent()) {
      log.error("Argument [packagesToScan='%s'] must contain only letters, numbers and symbol [.]"
          .formatted(optBrokenName.get()));
      throw new IllegalArgumentException(
          "Argument [packagesToScan='%s'] must contain only letters, numbers and symbol [.]"
              .formatted(optBrokenName.get()));
    }
  }
}
