package io.github.bobocodebreskul.context.registry;

import io.github.bobocodebreskul.context.exception.BeanDefinitionDuplicateException;
import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;
import io.github.bobocodebreskul.context.config.BeanDefinition;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
What is BeanDefinitionRegistry?
In the Spring Framework, a BeanDefinitionRegistry is an interface that represents a registry of bean definitions.
It is responsible for managing the bean definitions in a Spring IoC (Inversion of Control) container.
The IoC container uses the bean definition registry to store and retrieve information about beans.

The BeanDefinitionRegistry interface defines methods for registering and accessing bean definitions.
A bean definition contains metadata about a bean, such as its class, scope, lifecycle callbacks, dependencies, etc.
By using the BeanDefinitionRegistry, you can dynamically register, modify, or remove bean definitions during the application context's lifecycle.

The base interfaces has been created according to the description provided here: #2

Base branch: feature/bean-definition

Scope of this task

Implement SimpleBeanDefinitionRegistry
Cover you solution with tests
Add logging for your code
Create a javadoc for public methods and classes you are working with
 */
public class SimpleBeanDefinitionRegistry implements BeanDefinitionRegistry {

  private final Map<String, String> aliasMap = new ConcurrentHashMap<>();
  private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

  @Override
  public void registerAlias(String name, String alias) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeAlias(String alias) {
    throw new UnsupportedOperationException();
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
    if (beanDefinitionMap.containsKey(beanName)) {
      throw new BeanDefinitionDuplicateException(
          "Cannot registered a beanDefinition with name %s because a beanDefinition with that name is already registered%n".formatted(
              beanName));
    } else {
      beanDefinitionMap.put(beanName, beanDefinition);
    }
  }

  @Override
  public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
    beanDefinitionMap.remove(beanName);
  }

  @Override
  public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
    return beanDefinitionMap.get(beanName);
  }

  @Override
  public boolean containsBeanDefinition(String beanName) {
    return beanDefinitionMap.containsKey(beanName);
  }

  @Override
  public String[] getBeanDefinitionNames() {
    Set<String> beanDefinitionNamesSet = beanDefinitionMap.keySet();
    String[] beanDefinitionNames = new String[beanDefinitionNamesSet.size()];
    int i = 0;
    for (String beanName : beanDefinitionNamesSet) {
      beanDefinitionNames[i++] = beanName;
    }
    return beanDefinitionNames;
  }

  @Override
  public int getBeanDefinitionCount() {
    return beanDefinitionMap.size();
  }

  @Override
  public boolean isBeanNameInUse(String beanName) {
    return false;
  }
}
