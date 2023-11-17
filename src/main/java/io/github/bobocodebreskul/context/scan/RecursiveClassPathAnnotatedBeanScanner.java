package io.github.bobocodebreskul.context.scan;

import static io.github.bobocodebreskul.context.support.GeneralConstants.EMPTY;
import static java.util.Arrays.asList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.registry.AnnotatedBeanDefinitionReader;
import io.github.bobocodebreskul.context.scan.utils.ScanUtils;
import io.github.bobocodebreskul.context.scan.utils.ScanUtilsImpl;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Implementation of {@link ClassPathAnnotatedBeanScanner} to recursively find all bean definitions
 * in package and register them. Additionally, performs package scan of all found configurations.
 *
 * @author Vitalii Katkov
 * @author Oleksandr Karpachov
 */
public class RecursiveClassPathAnnotatedBeanScanner implements ClassPathAnnotatedBeanScanner {

  private static final String PACKAGE_DELIMITER = ".";
  private final ScanUtils scanUtils;
  private final AnnotatedBeanDefinitionReader beanDefinitionReader;

  public RecursiveClassPathAnnotatedBeanScanner(ScanUtils scanUtils,
      AnnotatedBeanDefinitionReader beanDefinitionReader) {
    this.scanUtils = scanUtils;
    this.beanDefinitionReader = beanDefinitionReader;
  }

  @Override
  public void scan(String... scanPackages) {
    scanUtils.validatePackagesToScan(scanPackages);
    Queue<String> remainingScanPackages = new ArrayDeque<>(asList(scanPackages));
    Set<String> processedScanPackages = new HashSet<>();
    while (!remainingScanPackages.isEmpty()) {
      String scanPackage = remainingScanPackages.poll();
      processedScanPackages.add(scanPackage);

      Set<Class<?>> foundClasses = scanSingle(scanPackage);
      foundClasses.forEach(beanDefinitionReader::registerBean);
      // TODO: implement package scan found on discovered configurations
      // recursively find all bean from other found configurations
//      processedScanPackages.addAll(collectScannedPackages(foundClasses));
//      remainingScanPackages.addAll(findConfigurationScanPackages(foundClasses, processedScanPackages));
    }
  }

  private List<String> findConfigurationScanPackages(Set<Class<?>> foundClasses,
      Set<String> processedScanPackages) {
    return foundClasses.stream()
        .filter(this::isConfigurationClass)
        .map(this::findConfigurationScanPackages)
        .filter(configurationScanPackages -> configurationScanPackages.length > 0)
        .flatMap(Arrays::stream)
        .filter(not(processedScanPackages::contains))
        .toList();
  }

  private Set<Class<?>> scanSingle(String scanPackage) {
    return scanUtils.searchClassesByAnnotationRecursively(scanPackage, BringComponent.class);
  }

  private boolean isConfigurationClass(Class<?> clazz) {
    // TODO: check if class is configuration class
    throw new UnsupportedOperationException();
  }

  private String[] findConfigurationScanPackages(Class<?> clazz) {
    // TODO: parse configuration class and get packages to scan
    throw new UnsupportedOperationException();
  }

  private Set<String> collectScannedPackages(Set<Class<?>> foundClasses) {
    return foundClasses.stream()
        .map(Class::getName)
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
