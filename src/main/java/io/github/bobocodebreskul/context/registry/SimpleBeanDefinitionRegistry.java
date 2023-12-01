package io.github.bobocodebreskul.context.registry;

import static java.util.Objects.isNull;

import io.github.bobocodebreskul.config.LoggerFactory;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.exception.AliasDuplicateException;
import io.github.bobocodebreskul.context.exception.BeanDefinitionDuplicateException;
import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;

public class SimpleBeanDefinitionRegistry implements BeanDefinitionRegistry {

  private final static Logger log = LoggerFactory.getLogger(SimpleBeanDefinitionRegistry.class);
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
    log.debug("Registering the alias of bean with name '{}'", name);
    if (isNull(name)) {
      log.error("Alias registration failed. Name should not be null.");
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    if (isNull(alias)) {
      log.error("Alias registration failed. Alias should not be null.");
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    if (aliasMap.containsKey(alias)) {
      log.error("Alias registration failed. Duplicate alias '{}'", alias);
      throw new AliasDuplicateException(
          CANNOT_REGISTER_DUPLICATE_ALIAS_MESSAGE.formatted(alias));
    }
    aliasMap.put(alias, name);
    log.debug("Alias '{}' registered for bean '{}'", alias, name);
    log.trace("Alias registration completed successfully.");
  }

  @Override
  public void removeAlias(String alias) {
    log.debug("removeAlias method invoked: alias={}", alias);
    log.debug("Removing the alias '{}'", alias);
    if (isNull(alias)) {
      log.error("Alias removal failed. Alias should not be null.");
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    if (aliasMap.containsKey(alias)) {
      aliasMap.remove(alias);
      log.debug("Alias '{}' removed", alias);
      log.trace("Alias removal completed successfully.");
    } else {
      log.error("Alias removal failed. Alias '{}' not found", alias);
    }
  }

  @Override
  public boolean isAlias(String alias) {
    log.debug("isAlias method invoked: alias={}", alias);
    log.debug("Checking '{}' is alias", alias);
    if (isNull(alias)) {
      log.error("Alias check failed. Alias should not be null.");
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    boolean isAlias = aliasMap.containsKey(alias);
    log.trace("'{}' is{} an alias.", alias, isAlias ? "" : " not");
    return isAlias;
  }

  @Override
  public Set<String> getAliases(String name) {
    log.debug("getAliases method invoked: name={}", name);
    log.debug("Retrieving all aliases for the name '{}'", name);
    if (isNull(name)) {
      log.error("Alias retrieval failed. Name should not be null.");
      throw new IllegalArgumentException(ALIAS_SHOULD_NOT_BE_NULL);
    }
    Set<String> aliases = aliasMap.entrySet().stream()
        .filter(entry -> name.equals(entry.getValue()))
        .map(Entry::getKey)
        .collect(Collectors.toSet());

    log.trace("Aliases for bean '{}' are: {}", name, aliases);
    return aliases;
  }

  @Override
  public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
    log.debug("registerBeanDefinition method invoked: beanName={}, beanDefinition={}",
        beanName, beanDefinition);
    log.debug("Registering bean definition for the bean name '{}'", beanName);
    if (isNull(beanName)) {
      log.error("BeanDefinition registration failed. Bean name should not be null.");
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    if (isNull(beanDefinition)) {
      log.error("BeanDefinition registration failed. BeanDefinition should not be null.");
      throw new IllegalArgumentException(BEAN_DEFINITION_SHOULD_NOT_BE_NULL_MESSAGE);
    }
    if (beanDefinitionMap.containsKey(beanName)) {
      log.error("BeanDefinition registration failed. Duplicate bean definition '{}'", beanName);
      throw new BeanDefinitionDuplicateException(
          CANNOT_REGISTER_DUPLICATE_BEAN_DEFINITION_MESSAGE.formatted(beanName));
    }
    beanDefinitionMap.put(beanName, beanDefinition);
    log.debug("BeanDefinition '{}' registered with name '{}'", beanDefinition, beanName);
    log.trace("BeanDefinition registration completed successfully.");
  }

  @Override
  public void removeBeanDefinition(String beanName) {
    log.debug("removeBeanDefinition method invoked: beanName={}", beanName);
    log.debug("Removing the bean definition for bean name '{}'", beanName);
    if (isNull(beanName)) {
      log.error("BeanDefinition removal failed. Bean name should not be null.");
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    if (beanDefinitionMap.containsKey(beanName)) {
      beanDefinitionMap.remove(beanName);
      log.debug("BeanDefinition '{}' removed", beanName);
      log.trace("BeanDefinition removal completed successfully.");
    } else {
      log.error("BeanDefinition removal failed. BeanDefinition '{}' not found", beanName);
    }
  }

  @Override
  public BeanDefinition getBeanDefinition(String beanName) {
    log.debug("getBeanDefinition method invoked: beanName={}", beanName);
    log.debug("Retrieving bean definition for '{}'", beanName);
    if (isNull(beanName)) {
      log.error("BeanDefinition retrieval failed. Bean name should not be null.");
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
    if (isNull(beanDefinition)) {
      log.error("The bean definition for specified name does not exist");
      throw new NoSuchBeanDefinitionException(
          BEAN_DEFINITION_FOR_NAME_NOT_FOUND.formatted(beanName));
    }
    log.trace("Bean definition for '{}' retrieved successfully.", beanName);
    return beanDefinition;
  }

  @Override
  public boolean containsBeanDefinition(String beanName) {
    log.debug("containsBeanDefinition method invoked: beanName={}", beanName);
    if (isNull(beanName)) {
      log.error("Checking for contains BeanDefinition failed. Bean name should not be null.");
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    log.trace("Checking if a BeanDefinition exists for '{}'", beanName);
    boolean containsDefinition = beanDefinitionMap.containsKey(beanName);
    log.debug("BeanDefinition for '{}' {} present in the registry.", beanName,
        containsDefinition ? "is" : "is not");
    return beanDefinitionMap.containsKey(beanName);
  }

  @Override
  public Set<String> getBeanDefinitionNames() {
    log.debug("getBeanDefinitionNames method invoked");
    Set<String> beanDefinitionNames = beanDefinitionMap.keySet();
    log.trace("Retrieved bean definition names: {}", beanDefinitionNames);
    return beanDefinitionNames;
  }

  @Override
  public int getBeanDefinitionCount() {
    log.debug("getBeanDefinitionCount method invoked");
    int beanDefinitionCount = beanDefinitionMap.size();
    log.trace("Retrieved bean definition count: {}", beanDefinitionCount);
    return beanDefinitionCount;
  }

  @Override
  public boolean isBeanNameInUse(String beanName) {
    log.debug("isBeanNameInUse method invoked: beanName={}", beanName);
    if (isNull(beanName)) {
      log.error("Checking is bean name in use failed. Bean name should not be null.");
      throw new IllegalArgumentException(BEAN_NAME_SHOULD_NOT_BE_NULL);
    }
    boolean isBeanNameInUse = isAlias(beanName) || containsBeanDefinition(beanName);
    log.debug("Bean name '{}' {} in use.", beanName, isBeanNameInUse ? "is" : "is not");
    return isBeanNameInUse;
  }

  @Override
  public Collection<BeanDefinition> getBeanDefinitions() {
    log.debug("getBeanDefinitions method invoked");
    Collection<BeanDefinition> beanDefinitions = beanDefinitionMap.values();
    log.trace("Retrieved all bean definitions: {}", beanDefinitions);
    return beanDefinitions;
  }

  @Override
  public BeanDefinition getBeanDefinitionByClass(Class<?> beanClass) {
    log.debug("getBeanDefinitionByClass method invoked: beanClass={}", beanClass);
    if (isNull(beanClass)) {
      log.error("BeanDefinition retrieval failed. Bean class should not be null.");
      throw new IllegalArgumentException(BEAN_CLASS_SHOULD_NOT_BE_NULL);
    }
    log.trace("Retrieving bean definition for the class: {}", beanClass);
    BeanDefinition beanDefinition = getBeanDefinitions().stream()
        .filter(def -> beanClass.equals(def.getBeanClass()))
        .findAny()
        .orElseThrow(() -> new NoSuchBeanDefinitionException(
            BEAN_DEFINITION_FOR_CLASS_NOT_FOUND.formatted(beanClass.getName())));
    log.trace("Bean definition for class '{}' retrieved successfully.", beanClass);
    return beanDefinition;
  }

  @Override
  public List<BeanDefinition> getBeanDefinitionByType(Class<?> type) {
    return beanDefinitionMap.values().stream()
        .filter(beanDefinition -> type.isAssignableFrom(beanDefinition.getBeanClass()))
        .toList();
  }
}
