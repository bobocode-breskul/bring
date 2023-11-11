package io.github.bobocodebreskul.context.scan;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;

/**
 * Class to recursively scan packages and search for bean definition classes. Additionally, performs package scan of
 * all found configurations.
 * Return found bean definitions in all package tree.
 *
 * @author Vitalii Katkov
 */
public class ClassPathAnnotatedBeanScanner {

    private static final String PACKAGE_DELIMITER = ".";
    private static final String EMPTY = "";

    // TODO: convert found classes to bean definitions
    // TODO: change return type from 'String' to bean definition class
    /**
     * Recursively scan packages and find all bean definitions located in those packages or in packages
     * provided by found configurations.
     *
     * @param scanPackages packages to scan
     * @return found bean definitions
     */
    public Set<String> scan(String... scanPackages) {
        Set<String> allFoundClasses = new HashSet<>();

        Queue<String> remainingScanPackages = new ArrayDeque<>(asList(scanPackages));
        Set<String> processedScanPackages = new HashSet<>();
        while (!remainingScanPackages.isEmpty()) {
            String scanPackage = remainingScanPackages.poll();
            processedScanPackages.add(scanPackage);

            Set<String> foundClasses = scanSingle(scanPackage);
            processedScanPackages.addAll(collectScannedPackages(foundClasses));
            allFoundClasses.addAll(foundClasses);

            foundClasses.stream()
                    .filter(this::isConfigurationClass)
                    .map(this::findConfigurationScanPackages)
                    .filter(configurationScanPackages -> configurationScanPackages.length > 0)
                    .flatMap(Arrays::stream)
                    .filter(not(processedScanPackages::contains))
                    .forEach(remainingScanPackages::add);

        }

        return allFoundClasses;
    }

    private Set<String> scanSingle(String scanPackage) {
        // TODO: read all packages using Reflections
        return Collections.emptySet();
    }

    private boolean isConfigurationClass(String classFullName) {
        // TODO: check if class is configuration class
        return false;
    }

    private String[] findConfigurationScanPackages(String classFullName) {
        // TODO: parse configuration class and get packages to scan
        return new String[0];
    }

    private Set<String> collectScannedPackages(Set<String> foundClasses) {
        return foundClasses.stream()
                .map(this::getClassPackage)
                .collect(toSet());
    }

    private String getClassPackage(String classFullName) {
        if (classFullName.contains(PACKAGE_DELIMITER)) {
            return classFullName.substring(0, classFullName.lastIndexOf(PACKAGE_DELIMITER));
        }
        return EMPTY;
    }
}
