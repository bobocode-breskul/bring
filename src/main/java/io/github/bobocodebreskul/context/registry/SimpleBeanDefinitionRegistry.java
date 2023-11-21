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
    log.debug("registerAlias method invoked: name={}, alias={}", name, alias);
    log.info("Registering the alias of bean with name {}", name);
    if (isNull(name)) {
      log.error("The specified bean name is null");
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    if (isNull(alias)) {
      log.error("The specified bean alias is null");
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    if (aliasMap.containsKey(alias)) {
      log.error("The specified bean alias is already in use");
      throw new AliasDuplicateException(
          CANNOT_REGISTER_DUPLICATE_ALIAS_MESSAGE.formatted(alias));
    }
    log.trace("Registered bean alias: {}", alias);
    aliasMap.put(alias, name);
  }

  @Override
  public void removeAlias(String alias) {
    log.debug("removeAlias method invoked: alias={}", alias);
    log.info("Removing the alias {}", alias);
    if (isNull(alias)) {
      log.error("The specified bean alias is null");
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    log.trace("Removing bean alias: {}", alias);
    aliasMap.remove(alias);
  }

  @Override
  public boolean isAlias(String alias) {
    log.debug("isAlias method invoked: alias={}", alias);
    log.info("Checking is the alias {}", alias);
    if (isNull(alias)) {
      log.error("The specified bean alias is null");
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    log.trace("Checking is the alias: {}", alias);
    return aliasMap.containsKey(alias);
  }

  @Override
  public Set<String> getAliases(String name) {
    log.debug("getAliases method invoked: name={}", name);
    log.info("Getting all aliases for the name {}", name);
    if (isNull(name)) {
      log.error("The specified bean name is null");
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    log.trace("Getting all aliases for the name: {}", name);
    return aliasMap.entrySet().stream()
        .filter(entry -> name.equals(entry.getValue()))
        .map(Entry::getKey)
        .collect(Collectors.toSet());
  }

  @Override
  public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
    log.debug("registerBeanDefinition method invoked: beanName={}, beanDefinition={}",
        beanName, beanDefinition);
    log.info("Registering bean definition for the bean name {}", beanName);
    if (isNull(beanName)) {
      log.error("The specified bean name is null");
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    if (isNull(beanDefinition)) {
      log.error("The specified bean definition is null");
      throw new IllegalArgumentException(BEAN_DEFINITION_SHOULD_NOT_BE_NULL_MESSAGE);
    }
    if (beanDefinitionMap.containsKey(beanName)) {
      log.error("The specified bean name is already in use");
      throw new BeanDefinitionDuplicateException(
          CANNOT_REGISTER_DUPLICATE_BEAN_DEFINITION_MESSAGE.formatted(beanName));
    }
    beanDefinitionMap.put(beanName, beanDefinition);
  }

  @Override
  public void removeBeanDefinition(String beanName) {
    log.debug("removeBeanDefinition method invoked: beanName={}", beanName);
    log.info("Removing the bean definition for bean name {}", beanName);
    if (isNull(beanName)) {
      log.error("The specified bean name is null");
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    log.trace("Removing bean definition for bean name: {}", beanName);
    beanDefinitionMap.remove(beanName);
  }

  @Override
  public BeanDefinition getBeanDefinition(String beanName) {
    log.debug("getBeanDefinition method invoked: beanName={}", beanName);
    log.info("Getting bean definition for the bean name {}", beanName);
    if (isNull(beanName)) {
      log.error("The specified bean name is null");
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    log.trace("Getting bean definition for the bean name: {}", beanName);
    BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
    if (isNull(beanDefinition)) {
      log.error("The bean definition for specified name does not exist");
      throw new NoSuchBeanDefinitionException(
          BEAN_DEFINITION_FOR_NAME_NOT_FOUND.formatted(beanName));
    }
    return beanDefinition;
  }

  @Override
  public boolean containsBeanDefinition(String beanName) {
    log.debug("containsBeanDefinition method invoked: beanName={}", beanName);
    log.info("Checking bean definition registry contains beanName {}", beanName);
    if (isNull(beanName)) {
      log.error("The specified bean name is null");
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    log.trace("Checking bean definition registry contains beanName: {}", beanName);
    return beanDefinitionMap.containsKey(beanName);
  }

  @Override
  public Set<String> getBeanDefinitionNames() {
    log.debug("getBeanDefinitionNames method invoked");
    log.info("Getting all bean definition names");
    log.trace("Getting all bean definition names");
    return beanDefinitionMap.keySet();
  }

  @Override
  public int getBeanDefinitionCount() {
    log.debug("getBeanDefinitionCount method invoked");
    log.info("Getting bean definition count");
    log.trace("Getting bean definition count");
    return beanDefinitionMap.size();
  }

  @Override
  public boolean isBeanNameInUse(String beanName) {
    log.debug("isBeanNameInUse method invoked: beanName={}", beanName);
    log.info("Checking is the beanName {} in use", beanName);
    if (isNull(beanName)) {
      log.error("The specified bean name is null");
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    log.trace("Checking is the beanName {} in use", beanName);
    return isAlias(beanName) || containsBeanDefinition(beanName);
  }

  @Override
  public Collection<BeanDefinition> getBeanDefinitions() {
    log.debug("getBeanDefinitions method invoked");
    log.info("Getting all bean definition");
    log.trace("Getting all bean definition");
    return beanDefinitionMap.values();
  }

  @Override
  public BeanDefinition getBeanDefinitionByClass(Class<?> beanClass) {
    log.debug("getBeanDefinitionByClass method invoked: beanClass={}", beanClass);
    log.info("Getting bean definition for the class {}", beanClass);
    if (isNull(beanClass)) {
      log.error("The specified bean class is null");
      throw new IllegalArgumentException(BEAN_CLASS_SHOULD_NOT_BE_NULL);
    }
    log.trace("Getting bean definition for the class: {}", beanClass.getSimpleName());
    return getBeanDefinitions().stream()
        .filter(beanDefinition -> beanClass.equals(beanDefinition.getBeanClass()))
        .findAny().orElseThrow(() -> new NoSuchBeanDefinitionException(
            BEAN_DEFINITION_FOR_CLASS_NOT_FOUND.formatted(beanClass.getName())));
  }
}
