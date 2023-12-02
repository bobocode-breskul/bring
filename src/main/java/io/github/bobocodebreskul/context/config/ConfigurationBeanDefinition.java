package io.github.bobocodebreskul.context.config;

import io.github.bobocodebreskul.context.annotations.BringBean;
import io.github.bobocodebreskul.context.annotations.BringConfiguration;
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

  private final Method beanMethod;
  private final Object configurationInstance;

  /**
   * Constructs a {@code ConfigurationBeanDefinition} with the specified bean class, bean method,
   * and configuration instance.
   *
   * @param beanClass             the class of the bean
   * @param beanMethod            the method responsible for initializing the bean
   * @param configurationInstance the instance of the configuration class
   */
  public ConfigurationBeanDefinition(Class<?> beanClass, Method beanMethod,
      Object configurationInstance) {
    super(beanClass);
    this.beanMethod = beanMethod;
    this.configurationInstance = configurationInstance;
  }

  /**
   * Gets the method responsible for initializing the bean.
   *
   * @return the {@code Method} object representing the bean initialization method
   */
  public Method getBeanMethod() {
    return beanMethod;
  }

  /**
   * Gets the instance of the configuration class.
   *
   * @return the instance of the configuration class used for bean initialization
   */
  public Object getConfigurationInstance() {
    return configurationInstance;
  }

}
