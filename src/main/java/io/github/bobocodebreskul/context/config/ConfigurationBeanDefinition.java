package io.github.bobocodebreskul.context.config;

import java.lang.reflect.Method;

public class ConfigurationBeanDefinition extends GenericBeanDefinition {

  private Method beanMethod;
  private Object configurationInstance;

  public ConfigurationBeanDefinition(Class<?> beanClass, Method beanMethod,
      Object configurationInstance) {
    super(beanClass);
    this.beanMethod = beanMethod;
    this.configurationInstance = configurationInstance;
  }

  public Method getBeanMethod() {
    return beanMethod;
  }


  public Object getConfigurationInstance() {
    return configurationInstance;
  }

}
