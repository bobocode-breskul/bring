package io.github.bobocodebreskul.context.scan.utils;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Predicate;

// TODO: docs
public interface ScanUtils {
    /**
     * Search all classes in given package except.
     * @param packagePath - package name
     * @return List of
     */
    // TODO: docs
    Set<Class<?>> searchAllClasses(String packagePath);
    // TODO: docs
    Set<Class<?>> searchClassesByAnnotationRecursively(String packagePath, Class<? extends Annotation> annotation);
    // TODO: docs
    Set<Class<?>> searchClassesByFilter(String packagePath, Predicate<Class<?>> filter);
}
