package io.github.bobocodebreskul.context.registry;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.exception.InstanceCreationException;
import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;
import io.github.bobocodebreskul.context.exception.NoSuchMethodRuntimeException;
import io.github.bobocodebreskul.context.scan.RecursiveClassPathAnnotatedBeanScanner;
import io.github.bobocodebreskul.context.scan.utils.ScanUtilsImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the {@link ObjectFactory} as Bring beans container. Creates and holds
 * all found and registered beans.
 *
 * @author Ruslan Hladchenko
 * @author Roman Pryshchepa
 * @author Mykola Filimonov
 * @author Vitalii Katkov
 */
@Slf4j
public class BringContainer implements ObjectFactory {

  private final Map<Class<?>, Object> storageByClass = new ConcurrentHashMap<>(16);
  private final Map<String, Object> storageByName = new ConcurrentHashMap<>(16);
  private final BeanDefinitionRegistry definitionRegistry;

  public BringContainer(BeanDefinitionRegistry definitionRegistry) {
    this.definitionRegistry = definitionRegistry;
  }

  /**
   * Collect all bean definitions by specified scan packages and build container to create and hold all found beans.
   *
   * @param scanPackages packages where to search beans
   * @return created beans container
   */
  public static BringContainer run(String... scanPackages) {
    BeanDefinitionRegistry definitionRegistry = new SimpleBeanDefinitionRegistry();
    AnnotatedBeanDefinitionReader beanDefinitionReader = new AnnotatedBeanDefinitionReader(definitionRegistry);
    RecursiveClassPathAnnotatedBeanScanner scanner = new RecursiveClassPathAnnotatedBeanScanner(new ScanUtilsImpl(), beanDefinitionReader);
    scanner.scan(scanPackages);

    return new BringContainer(definitionRegistry);
  }

  // TODO: 1. add dependency Injection by one constructor with parameters
  // TODO: 2. add dependency injection by @Autowired field
  @Override
  public Object getBean(String name) {
    BeanDefinition beanDefinition = definitionRegistry.getBeanDefinition(name);
    if (beanDefinition == null) {
      throw new NoSuchBeanDefinitionException("BeanDefinition for bean with name %s is not found! Check configuration and register this bean".formatted(name));
    }
    Class<?> beanClass = beanDefinition.getBeanClass();
    try {
      if (storageByName.containsKey(name)) {
        return storageByName.get(name);
      }
      Constructor<?> declaredConstructor = beanClass.getDeclaredConstructor();
      Object newInstance = declaredConstructor.newInstance();
      storageByClass.put(beanClass, newInstance);
      storageByName.put(beanDefinition.getName(), newInstance);
      return newInstance;
    } catch (NoSuchMethodException e) {
      // TODO: add additional logging with some input parameters
      // TODO: cover with tests
      throw new NoSuchMethodRuntimeException(
          "No default constructor for class \"%s\"!".formatted(name), e);
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
      // TODO: add additional logging with some input parameters
      // TODO: cover with tests
      throw new InstanceCreationException(
          "Could not create an instance of \"%s\" class!".formatted(name), e);
    }
  }

  @Override
  public Object getBean(Class<?> clazz) {
    throw new UnsupportedOperationException();
  }
}
