package io.github.bobocodebreskul.context.registry;

/**
 * A factory interface for creating and obtaining instances of beans.
 */
public interface ObjectFactory {

  /**
   * Get a bean instance by its name.
   *
   * @param name the name of the bean to retrieve
   * @return the instance of the requested bean
   */
  Object getBean(String name);

  /**
   * Get a bean instance by its class type.
   *
   * @param clazz the class type of the bean to retrieve
   * @return the instance of the requested bean
   */
  Object getBean(Class<?> clazz);
}

