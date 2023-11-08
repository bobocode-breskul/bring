package io.github.bobocodebreskul.context.registry;

public interface AliasRegistry {

  void registerAlias(String name, String alias);

  void removeAlias(String alias);

  boolean isAlias(String name);

  String[] getAliases(String name);
}
