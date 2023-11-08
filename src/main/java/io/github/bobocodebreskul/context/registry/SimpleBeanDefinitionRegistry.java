package io.github.bobocodebreskul.context.registry;

import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;
import io.github.bobocodebreskul.context.config.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleBeanDefinitionRegistry implements BeanDefinitionRegistry {

  private final Map<String, String> aliasMap = new ConcurrentHashMap<>();
  private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

  @Override
  public void registerAlias(String name, String alias) {

  }

  @Override
  public void removeAlias(String alias) {

  }

  @Override
  public boolean isAlias(String name) {
    return false;
  }

  @Override
  public String[] getAliases(String name) {
    return new String[0];
  }

  @Override
  public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {

  }

  @Override
  public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {

  }

  @Override
  public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
    return null;
  }

  @Override
  public boolean containsBeanDefinition(String beanName) {
    return false;
  }

  @Override
  public String[] getBeanDefinitionNames() {
    return new String[0];
  }

  @Override
  public int getBeanDefinitionCount() {
    return 0;
  }

  @Override
  public boolean isBeanNameInUse(String beanName) {
    return false;
  }
}
