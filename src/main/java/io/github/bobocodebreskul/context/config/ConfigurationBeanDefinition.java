package io.github.bobocodebreskul.context.config;

import io.github.bobocodebreskul.context.annotations.BringConfiguration;
import io.github.bobocodebreskul.context.annotations.BringBean;
import java.lang.reflect.Method;


/**
 * Java Configuration-based implementation of {@link BeanDefinition}.
 *
 * <p>Requires instance of a class marked with {@link BringConfiguration} annotation that
 * represents object with methods marked with {@link BringBean} which initialise beans
 * initialization point.</p>
 *
 * @see BringConfiguration
 * @see BringBean
 * @see GenericBeanDefinition
 * @see BeanDefinition
 */
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
