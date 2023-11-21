package io.github.bobocodebreskul.context.config;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import java.util.List;

/**
 * Describes a bean instance, which has {@code name}, {@code beanClass}, {@code dependsOn},
 * {@code scope}, {@code primary}, {@code autowireCandidate},  and further information supplied by
 * concrete implementations.
 *
 * @see BringComponent
 * @see GenericBeanDefinition
 */
public interface BeanDefinition {

  /**
   * Scope identifier for the default singleton scope: "singleton".
   */
  String SINGLETON_SCOPE = "singleton";

  /**
   * Scope identifier for the prototype scope: "prototype".
   */
  String PROTOTYPE_SCOPE = "prototype";


  /**
   * Method for getting name of current {@link BeanDefinition}.
   *
   * @return name of current {@link BeanDefinition}
   */
  String getName();

  /**
   * Set the name of current {@link BeanDefinition}
   */
  void setName(String name);

  /**
   * Set all bean dependencies.
   * @param dependencies bean dependencies
   */
  void setDependencies(List<BeanDependency> dependencies);

  /**
   * Return all bean dependencies.
   * @return bean dependencies
   */
  List<BeanDependency> getDependencies();

  /**
   * Override the target scope of this bean, specifying a new scope name.
   *
   * @see #SINGLETON_SCOPE
   * @see #PROTOTYPE_SCOPE
   */
  void setScope(String scope);

  /**
   * Return the name of the current target scope for this bean, or {@code null} if not known yet.
   */
  String getScope();

  /**
   * Return whether this a singleton with a single, shared instance returned on all calls.
   *
   * @see #SINGLETON_SCOPE
   */
  boolean isSingleton();


  /**
   * Return whether this a prototype with an independent instance returned for each call.
   *
   * @see #PROTOTYPE_SCOPE
   */
  boolean isPrototype();

  /**
   * Set whether this bean is a candidate for getting autowired into some other bean.
   */
  void setAutowireCandidate(boolean autowireCandidate);

  /**
   * Return whether this bean is a candidate for getting autowired into some other bean.
   */
  boolean isAutowireCandidate();

  /**
   * Set whether this bean is a primary autowire candidate.
   * <p>If this value is {@code true} for exactly one bean among multiple
   * matching candidates, it will serve as the main autowire candidate.
   */
  void setPrimary(boolean primary);

  /**
   * Return whether this bean is a primary autowire candidate.
   */
  boolean isPrimary();

  /**
   * Method for setting type of current {@link BeanDefinition}
   *
   * @param beanClass type of current bean definition
   */
  void setBeanClass(Class<?> beanClass);

  /**
   * Method for getting type of current {@link BeanDefinition}.
   *
   * @return type of current {@link BeanDefinition}
   */
  Class<?> getBeanClass();
}
