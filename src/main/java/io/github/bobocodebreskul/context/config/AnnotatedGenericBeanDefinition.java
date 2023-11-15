package io.github.bobocodebreskul.context.config;

import io.github.bobocodebreskul.context.annotations.BringComponent;

/**
 * Java annotation-based implementation of {@link BeanDefinition}.
 *
 * <p>Requires instance of a class marked with {@link BringComponent} annotation that represents
 * this bean initialization point.</p>
 *
 * @see BringComponent
 * @see GenericBeanDefinition
 * @see BeanDefinition
 */
public class AnnotatedGenericBeanDefinition extends GenericBeanDefinition {

  public AnnotatedGenericBeanDefinition(Class<?> beanClass) {
    super(beanClass);
  }
}
