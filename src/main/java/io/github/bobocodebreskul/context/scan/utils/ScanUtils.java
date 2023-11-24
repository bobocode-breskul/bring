package io.github.bobocodebreskul.context.scan.utils;

import io.github.bobocodebreskul.context.annotations.BringComponentScan;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Util class for searching classes in packages.
 *
 * @author Oleksandr Karpachov
 * @author Oleh Yakovenko
 */
public interface ScanUtils {

  /**
   * Search all classes, (except enums and records) in given package including nested packages.
   *
   * @param packagePath - root package for search.
   * @return Set of found classes
   */
  Set<Class<?>> searchAllClasses(String packagePath);

  /**
   * Search classes which has given annotation or annotations which has given annotation inside. For
   * example if @Service annotation has @BringComponent annotation, and we're searching by
   * @BringComponent we also will find all classes with @Service service annotation. !Note: given
   * annotation must have {@code @retention(RetentionPolicy.RUNTIME)}
   *
   * @param packagePathPrefix root package for search.
   * @param annotation        target annotation.
   * @return Set of all classes with given annotation.
   */
  Set<Class<?>> searchClassesByAnnotationRecursively(String packagePathPrefix,
      Class<? extends Annotation> annotation);

  /**
   * Search classes by filter in given package including nested packages.
   *
   * @param packagePathPrefix root package for search.
   * @param filter            filter predicate which applies on found classes
   * @return Set of all classes which pass the filter.
   */
  Set<Class<?>> searchClassesByFilter(String packagePathPrefix, Predicate<Class<?>> filter);

  /**
   * Reads the base package names specified in the {@code @BringComponentScan} annotation on the
   * given class. If the annotation is present, retrieves and returns the base packages; otherwise,
   * returns an empty array.
   *
   * <p>The {@code @BringComponentScan} annotation allows configuration of package scanning for
   * components. The specified base packages and their sub-packages will be searched for classes
   * annotated with stereotypes, such as {@code @BringComponent}.
   *
   * @param annotatedClass The class annotated with {@code @BringComponentScan} from which to read
   *                       base packages.
   * @return An array of base package names to be scanned. If not specified in the annotation,
   * returns an empty array.
   * @see BringComponentScan
   * @see BringComponentScan#basePackages()
   */
  Set<String> readBasePackages(Class<?> annotatedClass);

}
