package io.github.bobocodebreskul.context.registry;

import java.util.List;

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
   * Retrieves a bean of the specified type from the container.
   *
   * @param clazz the class type of the bean to retrieve
   * @param <T>   the type of the bean
   * @return the bean instance of the specified type, or {@code null} if not found
   */
  <T> T getBean(Class<T> clazz);

  /**
   * Retrieves a list of all beans in the container.
   *
   * @return a list containing all beans in the container
   */
  List<Object> getAllBeans();
}

