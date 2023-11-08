package io.github.bobocodebreskul.context.config;


import java.util.function.Supplier;

public abstract class GenericBeanDefinition implements BeanDefinition {

  private String beanClass;
  private Supplier<?> instanceSupplier;
  private String scope;
  private boolean primary;
  private boolean autowireCandidate;
  private String[] dependsOn;

  @Override
  public void setDependsOn(String... dependsOn) {

  }

  @Override
  public String[] getDependsOn() {
    return dependsOn;
  }

  @Override
  public void setInstanceSupplier(Supplier<?> instanceSupplier) {
    this.instanceSupplier = instanceSupplier;
  }

  @Override
  public Supplier<?> getInstanceSupplier() {
    return this.instanceSupplier;
  }

  @Override
  public void setScope(String scope) {
    // todo: implement this method and corresponding logic
  }

  @Override
  public String getScope() {
    return this.scope;
  }

  @Override
  public boolean isSingleton() {
    // todo: implement this method and corresponding logic
    return false;
  }

  @Override
  public boolean isPrototype() {
    // todo: implement this method and corresponding logic
    return false;
  }

  @Override
  public void setAutowireCandidate(boolean autowireCandidate) {
    this.autowireCandidate = autowireCandidate;
  }

  @Override
  public boolean isAutowireCandidate() {
    return autowireCandidate;
  }

  @Override
  public void setPrimary(boolean primary) {
    this.primary = primary;
  }

  @Override
  public boolean isPrimary() {
    return primary;
  }

  @Override
  public void setBeanClassName(String beanClassName) {
    // todo: implement this method and corresponding logic
  }

  @Override
  public String getBeanClassName() {
    // todo: implement this method and corresponding logic
    return null;
  }
}
