package io.github.bobocodebreskul.context.config;

import java.util.function.Supplier;

public interface BeanDefinition {

  void setDependsOn(String... dependsOn);

  String[] getDependsOn();

  void setInstanceSupplier(Supplier<?> instanceSupplier);

  Supplier<?> getInstanceSupplier();

  void setScope(String scope);

  String getScope();

  boolean isSingleton();

  boolean isPrototype();

  void setAutowireCandidate(boolean autowireCandidate);

  boolean isAutowireCandidate();

  void setPrimary(boolean primary);

  boolean isPrimary();

  void setBeanClassName(String beanClassName);

  String getBeanClassName();
}
