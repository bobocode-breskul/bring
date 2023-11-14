package io.github.bobocodebreskul.context.config;


import java.util.List;

/**
 * Abstract implementation of {@link BeanDefinition} interface with base methods implemented.
 *
 * @see AnnotatedGenericBeanDefinition
 */
public abstract class GenericBeanDefinition implements BeanDefinition {

  private String name;
  private Class<?> beanClass;
  private String scope;
  private boolean primary;
  private boolean autowireCandidate;
  private List<Class<?>> dependsOn;

  public GenericBeanDefinition(Class<?> beanClass) {
    setBeanClass(beanClass);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setDependsOn(List<Class<?>> dependsOn) {
    this.dependsOn = dependsOn;
  }

  @Override
  public List<Class<?>> getDependsOn() {
    return dependsOn;
  }

  @Override
  public void setScope(String scope) {
    this.scope = scope;
  }

  @Override
  public String getScope() {
    return this.scope;
  }

  @Override
  public boolean isSingleton() {
    return BeanDefinition.SINGLETON_SCOPE.equals(scope);
  }

  @Override
  public boolean isPrototype() {
    return BeanDefinition.PROTOTYPE_SCOPE.equals(scope);
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
  public void setBeanClass(Class<?> beanClass) {
    this.beanClass = beanClass;
  }

  @Override
  public Class<?> getBeanClass() {
    return this.beanClass;
  }
}
