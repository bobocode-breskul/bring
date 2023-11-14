package io.github.bobocodebreskul.context.registry;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.exception.ClassNotFoundRuntimeException;
import io.github.bobocodebreskul.context.exception.InstanceCreationException;
import io.github.bobocodebreskul.context.exception.NoSuchMethodRuntimeException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class SimpleObjectFactory implements ObjectFactory {

  private final Map<Class<?>, Object> storage = new HashMap<>();

  private final SimpleBeanDefinitionRegistry definitionRegistry;

  public SimpleObjectFactory(SimpleBeanDefinitionRegistry definitionRegistry) {
    this.definitionRegistry = definitionRegistry;
  }

  @Override
  public Object getBean(String name) {
    BeanDefinition beanDefinition = definitionRegistry.getBeanDefinition(name);
    String beanClassName = beanDefinition.getBeanClassName();
    try {
      Class<?> clazz = Class.forName(beanClassName);
      if (storage.containsKey(clazz)) {
        return storage.get(clazz);
      }
      Constructor<?> declaredConstructor = clazz.getDeclaredConstructor();
      Object newInstance = declaredConstructor.newInstance();
      storage.put(clazz, newInstance);
      return newInstance;
    } catch (ClassNotFoundException e) {
      throw new ClassNotFoundRuntimeException("Class with name \"%s\" not found!".formatted(name));
    } catch (NoSuchMethodException e) {
      throw new NoSuchMethodRuntimeException(
          "No default constructor for class \"%s\"!".formatted(name));
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new InstanceCreationException(
          "Could not create an instance of \"%s\" class!".formatted(name));
    }
  }

  @Override
  public Object getBean(Class<?> clazz) {
    throw new UnsupportedOperationException();
  }
}
