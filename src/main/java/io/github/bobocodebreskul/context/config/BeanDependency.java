package io.github.bobocodebreskul.context.config;

/**
 * Bean dependency representation class. It holds all necessary information to resolve bean
 * dependency during bean creation phase.
 *
 * @param name dependency bean name
 * @param type dependency class type
 *
 * @author Vitalii Katkov
 * @author Serhii Barabash
 * @author Volodymyr Holichenko
 */
public record BeanDependency(String name, Class<?> type) {}
