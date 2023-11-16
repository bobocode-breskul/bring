package io.github.bobocodebreskul.context.scan.utils;

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
     * @param packagePath - root package for search.
     * @return Set of found classes
     */
    Set<Class<?>> searchAllClasses(String packagePath);

    /**
     * Search classes which has given annotation or annotations which has given annotation inside.
     * For example if @Service annotation has @Component annotation, and we're searching
     * by @Component we also will find all classes with @Service service annotation.
     * !Note: given annotation must have @Retention(RetentionPolicy.RUNTIME)
     *
     * @param packagePathPrefix root package for search.
     * @param annotation target annotation.
     * @return Set of all classes with given annotation.
     */
    Set<Class<?>> searchClassesByAnnotationRecursively(String packagePathPrefix, Class<? extends Annotation> annotation);

    /**
     * Search classes by filter in given package including nested packages.
     * @param packagePathPrefix root package for search.
     * @param filter filter predicate which applies on found classes
     * @return Set of all classes which pass the filter.
     */
    Set<Class<?>> searchClassesByFilter(String packagePathPrefix, Predicate<Class<?>> filter);

    /**
     * Valid incoming packages for not existing package, null input, not valid symbols
     * @param packagesToScan
     */
    void validatePackagesToScan(String... packagesToScan);
}
