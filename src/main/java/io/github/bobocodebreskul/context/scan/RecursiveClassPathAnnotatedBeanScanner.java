package io.github.bobocodebreskul.context.scan;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.registry.BeanDefinitionReader;
import io.github.bobocodebreskul.context.scan.utils.ScanUtils;
import java.util.ArrayDeque;
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
// TODO logs
public class RecursiveClassPathAnnotatedBeanScanner implements ClassPathAnnotatedBeanScanner {

  private final ScanUtils scanUtils;
  private final BeanDefinitionReader beanDefinitionReader;

  public RecursiveClassPathAnnotatedBeanScanner(ScanUtils scanUtils,
      BeanDefinitionReader beanDefinitionReader) {
    this.scanUtils = scanUtils;
    this.beanDefinitionReader = beanDefinitionReader;
  }

  @Override
  public void scan(Class<?> configClass) {
    Set<String> scanPackages = scanUtils.readBasePackages(configClass);
    Queue<String> remainingScanPackages = new ArrayDeque<>(scanPackages);
    while (!remainingScanPackages.isEmpty()) {
      String scanPackage = remainingScanPackages.poll();

      Set<Class<?>> foundClasses = scanSingle(scanPackage).stream()
          .filter(clazz -> !clazz.isAnnotation())
          .collect(Collectors.toSet());

      foundClasses.forEach(beanDefinitionReader::registerBean);
    }
  }

  private Set<Class<?>> scanSingle(String scanPackage) {
    return scanUtils.searchClassesByAnnotationRecursively(scanPackage, BringComponent.class);
  }
}
