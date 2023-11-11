package io.github.bobocodebreskul.context.scan.utils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface ScanUtils {

    /**
     * Search all fully class names in given package.
     * @param packagePath - package name
     * @return List of
     */
    Set<String> searchAllClassNames(String packagePath);
    Set<Class<?>> searchAllClasses(String packagePath);

    Set<Class<?>> searchClassesByFilter(String packagePath, Predicate<Class<?>> filter);

    Set<Class<?>> searchClassesByAnnotationRecursively(String packagePath, Class<? extends Annotation> annotation);
    Set<Class<?>> searchClassesByAnnotation(String packagePath, Class<? extends Annotation> annotation);

}
