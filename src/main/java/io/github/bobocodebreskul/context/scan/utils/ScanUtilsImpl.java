package io.github.bobocodebreskul.context.scan.utils;

import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.annotations.BringComponentScan;
import io.github.bobocodebreskul.context.exception.BringComponentScanNotFoundException;
import io.github.bobocodebreskul.context.support.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;

public class ScanUtilsImpl implements ScanUtils {

  private final static Logger log = LoggerFactory.getLogger(ScanUtilsImpl.class);
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
    Predicate<Class<?>> filter = clazz -> ReflectionUtils.checkIfClassHasAnnotationRecursively(
        clazz,
        filterByAnnotation);

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
  public Set<String> readBasePackages(Class<?> annotatedClass) {
    BringComponentScan bringComponentScan = annotatedClass.getAnnotation(BringComponentScan.class);
    if (!ReflectionUtils.isAnnotationPresentForClass(BringComponentScan.class, annotatedClass)) {
      String errorMessage =
          "No @BringComponentScan annotation found on class: " + annotatedClass.getSimpleName()
              + ".class";
      log.warn(errorMessage);
      throw new BringComponentScanNotFoundException(errorMessage);
    }
    if (bringComponentScan.basePackages().length != 0) {
      String[] basePackages = bringComponentScan.basePackages();
      log.info("Base packages read from @BringComponentScan annotation: " + String.join(", ",
          basePackages));
      return new HashSet<>(Arrays.asList(basePackages));
    } else {
      String basePackage = annotatedClass.getPackage().getName();
      log.info("Using default base package: " + basePackage);
      return Collections.singleton(basePackage);
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
