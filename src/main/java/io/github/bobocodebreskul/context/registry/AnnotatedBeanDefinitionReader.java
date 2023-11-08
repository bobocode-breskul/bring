package io.github.bobocodebreskul.context.registry;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

public class AnnotatedBeanDefinitionReader {

  private final BeanDefinitionRegistry beanDefinitionRegistry;

  public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
    this.beanDefinitionRegistry = registry;
  }

  public void registerBean(Class<?> beanClass) {
    doRegisterBean(beanClass, null, null, null);
  }

  public void registerBean(Class<?> beanClass, String name) {
    doRegisterBean(beanClass, name, null, null);
  }

  public <T> void registerBean(Class<T> beanClass, Supplier<T> supplier) {
    doRegisterBean(beanClass, null, null, supplier);
  }

  public <T> void registerBean(Class<T> beanClass, String name, Supplier<T> supplier) {
    doRegisterBean(beanClass, name, null, supplier);
  }

  public void registerBean(Class<?> beanClass,
      String name,
      Class<? extends Annotation>... qualifiers) {
    doRegisterBean(beanClass, name, qualifiers, null);
  }


  private <T> void doRegisterBean(Class<T> beanClass,
      String name,
      Class<? extends Annotation>[] qualifiers,
      Supplier<T> supplier) {
    // TODO: write registerBean method logic: beanDefinitionRegistry.registerBeanDefinition();
  }

}
