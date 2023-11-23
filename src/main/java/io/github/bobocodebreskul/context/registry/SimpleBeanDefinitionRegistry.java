package io.github.bobocodebreskul.context.registry;

import static java.util.Objects.isNull;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.exception.AliasDuplicateException;
import io.github.bobocodebreskul.context.exception.BeanDefinitionDuplicateException;
import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleBeanDefinitionRegistry implements BeanDefinitionRegistry {

  private final Map<String, String> aliasMap = new ConcurrentHashMap<>();
  private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

  private static final String CANNOT_REGISTER_DUPLICATE_ALIAS_MESSAGE =
      "Cannot registered an alias with name %s because an alias with that name is already registered%n";
  private static final String CANNOT_REGISTER_DUPLICATE_BEAN_DEFINITION_MESSAGE =
      "Cannot registered a beanDefinition with name %s because a beanDefinition with that name is already registered%n";
  private static final String ALIAS_SHOULD_NOT_BE_NULL = "alias should not be null";
  private static final String BEAN_NAME_SHOULD_NOT_BE_NULL = "beanName should not be null";
  private static final String BEAN_CLASS_SHOULD_NOT_BE_NULL = "beanClass should not be null";
  private static final String BEAN_DEFINITION_SHOULD_NOT_BE_NULL_MESSAGE = "beanDefinition should not be null";
  private static final String BEAN_DEFINITION_FOR_CLASS_NOT_FOUND = "BeanDefinition for bean with class %s is not found!";
  private static final String BEAN_DEFINITION_FOR_NAME_NOT_FOUND = "BeanDefinition for bean with name %s is not found!";

  @Override
  public void registerAlias(String name, String alias) {
    if (isNull(name)) {
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    if (isNull(alias)) {
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    if (aliasMap.containsKey(alias)) {
      throw new AliasDuplicateException(
          CANNOT_REGISTER_DUPLICATE_ALIAS_MESSAGE.formatted(alias));
    }
    aliasMap.put(alias, name);
  }

  @Override
  public void removeAlias(String alias) {
    if (isNull(alias)) {
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    aliasMap.remove(alias);
  }

  @Override
  public boolean isAlias(String alias) {
    if (isNull(alias)) {
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    return aliasMap.containsKey(alias);
  }

  @Override
  public Set<String> getAliases(String name) {
    if (isNull(name)) {
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    return aliasMap.entrySet().stream()
        .filter(entry -> name.equals(entry.getValue()))
        .map(Entry::getKey)
        .collect(Collectors.toSet());
  }

  @Override
  public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
    if (isNull(beanName)) {
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    if (isNull(beanDefinition)) {
      throw new IllegalArgumentException(BEAN_DEFINITION_SHOULD_NOT_BE_NULL_MESSAGE);
    }
    if (beanDefinitionMap.containsKey(beanName)) {
      throw new BeanDefinitionDuplicateException(
          CANNOT_REGISTER_DUPLICATE_BEAN_DEFINITION_MESSAGE.formatted(beanName));
    }
    beanDefinitionMap.put(beanName, beanDefinition);
  }

  @Override
  public void removeBeanDefinition(String beanName) {
    if (isNull(beanName)) {
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    beanDefinitionMap.remove(beanName);
  }

  @Override
  public BeanDefinition getBeanDefinition(String beanName) {
    if (isNull(beanName)) {
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
    if (isNull(beanDefinition)) {
      throw new NoSuchBeanDefinitionException(
          BEAN_DEFINITION_FOR_NAME_NOT_FOUND.formatted(beanName));
    }
    return beanDefinition;
  }

  @Override
  public boolean containsBeanDefinition(String beanName) {
    if (isNull(beanName)) {
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    return beanDefinitionMap.containsKey(beanName);
  }

  @Override
  public Set<String> getBeanDefinitionNames() {
    return beanDefinitionMap.keySet();
  }

  @Override
  public int getBeanDefinitionCount() {
    return beanDefinitionMap.size();
  }

  @Override
  public boolean isBeanNameInUse(String beanName) {
    if (isNull(beanName)) {
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    return isAlias(beanName) || containsBeanDefinition(beanName);
  }

  @Override
  public Collection<BeanDefinition> getBeanDefinitions() {
    return beanDefinitionMap.values();
  }

  @Override
  public BeanDefinition getBeanDefinitionByClass(Class<?> beanClass) {
    if (isNull(beanClass)) {
      throw new IllegalArgumentException(BEAN_CLASS_SHOULD_NOT_BE_NULL);
    }
    return getBeanDefinitions().stream()
        .filter(beanDefinition -> beanClass.equals(beanDefinition.getBeanClass()))
        .findAny().orElseThrow(() -> new NoSuchBeanDefinitionException(
            BEAN_DEFINITION_FOR_CLASS_NOT_FOUND.formatted(beanClass.getName())));
  }
}
