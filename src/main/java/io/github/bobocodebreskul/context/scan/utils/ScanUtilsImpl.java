package io.github.bobocodebreskul.context.scan.utils;

import static java.util.function.Predicate.not;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.BringComponentScan;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

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

  /**
   * Reads the base package names specified in the {@code @BringComponentScan} annotation on the
   * given class. If the annotation is present, retrieves and returns the base packages; otherwise,
   * returns an empty array.
   *
   * <p>The {@code @BringComponentScan} annotation allows configuration of package scanning for
   * components.
   * The specified base packages and their sub-packages will be searched for classes annotated with
   * stereotypes, such as {@code @BringComponent}.
   *
   * @param annotatedClass The class annotated with {@code @BringComponentScan} from which to read
   *                       base packages.
   * @return An array of base package names to be scanned. If not specified in the annotation,
   * returns an empty array.
   * @see BringComponentScan
   * @see BringComponentScan#basePackages()
   */
  public static String[] readBasePackages(Class<?> annotatedClass) {
    BringComponentScan bringComponentScan = annotatedClass.getAnnotation(BringComponentScan.class);

    if (bringComponentScan != null) {
      String[] basePackages = bringComponentScan.basePackages();
      log.info("Base packages read from @BringComponentScan annotation: " + String.join(", ",
          basePackages));
      return basePackages;
    } else {
      log.warn("No @BringComponentScan annotation found on class: " + annotatedClass.getName());
      return new String[]{};
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
