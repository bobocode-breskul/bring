package io.github.bobocodebreskul.context.config;


import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
  // TODO: test implementation
  private List<String> dependsOnNames;

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

  public List<String> getDependsOnNames() {
    return dependsOnNames;
  }

  public void setDependsOnNames(List<String> dependsOnNames) {
    this.dependsOnNames = dependsOnNames;
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

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    GenericBeanDefinition that = (GenericBeanDefinition) object;

    return new EqualsBuilder()
        .append(primary, that.primary)
        .append(autowireCandidate, that.autowireCandidate)
        .append(name, that.name)
        .append(beanClass, that.beanClass)
        .append(scope, that.scope)
        .append(dependsOn, that.dependsOn)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(name)
        .append(beanClass)
        .append(scope)
        .append(primary)
        .append(autowireCandidate).append(dependsOn).toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("name", name)
        .append("beanClass", beanClass)
        .append("scope", scope)
        .append("primary", primary)
        .append("autowireCandidate", autowireCandidate)
        .toString();
  }
}
