package io.github.bobocodebreskul.context.scan;

import static io.github.bobocodebreskul.context.support.GeneralConstants.EMPTY;
import static java.util.Arrays.asList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Implementation of {@link ClassPathAnnotatedBeanScanner} to recursively find all bean definitions
 * in package. Additionally, performs package scan of all found configurations.
 *
 * @author Vitalii Katkov
 */
public class RecursiveClassPathAnnotatedBeanScanner implements ClassPathAnnotatedBeanScanner {

  private static final String PACKAGE_DELIMITER = ".";

  @Override
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
    throw new UnsupportedOperationException();
  }

  private boolean isConfigurationClass(String classFullName) {
    // TODO: check if class is configuration class
    throw new UnsupportedOperationException();
  }

  private String[] findConfigurationScanPackages(String classFullName) {
    // TODO: parse configuration class and get packages to scan
    throw new UnsupportedOperationException();
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
