package io.github.bobocodebreskul.context.scan;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.registry.AnnotatedBeanDefinitionReader;
import io.github.bobocodebreskul.context.scan.utils.ScanUtils;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ClassPathAnnotatedBeanScanner} to recursively find all bean definitions
 * in package and register them. Additionally, performs package scan of all found configurations.
 *
 * @author Vitalii Katkov
 * @author Oleksandr Karpachov
 */
public class RecursiveClassPathAnnotatedBeanScanner implements ClassPathAnnotatedBeanScanner {

  private final ScanUtils scanUtils;
  private final AnnotatedBeanDefinitionReader beanDefinitionReader;

  public RecursiveClassPathAnnotatedBeanScanner(ScanUtils scanUtils,
      AnnotatedBeanDefinitionReader beanDefinitionReader) {
    this.scanUtils = scanUtils;
    this.beanDefinitionReader = beanDefinitionReader;
  }

  @Override
  public void scan(Class<?> configClass) {
    Set<String> scanPackages = scanUtils.readBasePackages(configClass);
    Queue<String> remainingScanPackages = new ArrayDeque<>(scanPackages);
    Set<String> processedScanPackages = new HashSet<>();
    while (!remainingScanPackages.isEmpty()) {
      String scanPackage = remainingScanPackages.poll();
      processedScanPackages.add(scanPackage);

      Set<Class<?>> foundClasses = scanSingle(scanPackage).stream()
        .filter(clazz -> !clazz.isAnnotation())
        .collect(Collectors.toSet());

      foundClasses.forEach(beanDefinitionReader::registerBean);
      // TODO: implement package scan found on discovered configurations
      // recursively find all bean from other found configurations
//      processedScanPackages.addAll(collectScannedPackages(foundClasses));
//      remainingScanPackages.addAll(findConfigurationScanPackages(foundClasses, processedScanPackages));
    }
  }


  private Set<Class<?>> scanSingle(String scanPackage) {
    return scanUtils.searchClassesByAnnotationRecursively(scanPackage, BringComponent.class);
  }
}
