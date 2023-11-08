package io.github.bobocodebreskul.context.registry;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;

public interface BeanDefinitionRegistry extends AliasRegistry {

  void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

  void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

  BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

  boolean containsBeanDefinition(String beanName);

  String[] getBeanDefinitionNames();

  int getBeanDefinitionCount();


  boolean isBeanNameInUse(String beanName);
}
