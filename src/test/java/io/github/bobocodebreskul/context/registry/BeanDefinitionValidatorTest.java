package io.github.bobocodebreskul.context.registry;

import static io.github.bobocodebreskul.context.registry.BeanDefinitionValidator.DISALLOWED_BEAN_NAME_CHARACTERS_EXCEPTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;

import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.BeanDefinitionValidationException;
import io.github.bobocodebreskul.context.support.BeanDependencyUtils;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BeanDefinitionValidatorTest {

  private BeanDefinitionRegistry definitionRegistry;
  private BeanDefinitionValidator beanDefinitionValidator;

  @BeforeEach
  public void setUp() {
    definitionRegistry = new SimpleBeanDefinitionRegistry();
    beanDefinitionValidator = new BeanDefinitionValidator(definitionRegistry,
        new BeanDependencyUtils());
  }

  @Test
  @DisplayName("When bean definition with empty dependencies then nothing thrown")
  @Order(1)
  void given_BeanDefinitionWithEmptyDependencies_When_validateBeanDefinitions_Then_NothingThrown() {
    //given
    var beanDefinition = new AnnotatedGenericBeanDefinition(A.class);
    beanDefinition.setName("a");
    definitionRegistry.registerBeanDefinition("a", beanDefinition);

    //when
    //then
    Assertions.assertThatNoException()
        .isThrownBy(() -> beanDefinitionValidator.validateBeanDefinitions());
  }

  @Test
  @DisplayName("When bean definition with dependencies without circular dependency then nothing thrown")
  @Order(2)
  void given_BeanDefinitionWithDependenciesWithoutCircularDependency_When_validateBeanDefinitions_Then_NothingThrown() {
    //given
    var aBeanDefinition = new AnnotatedGenericBeanDefinition(A.class);
    aBeanDefinition.setName("a");
    var bBeanDefinition = new AnnotatedGenericBeanDefinition(B.class);
    bBeanDefinition.setName("b");
    definitionRegistry.registerBeanDefinition("a", aBeanDefinition);
    definitionRegistry.registerBeanDefinition("b", bBeanDefinition);

    //when
    //then
    Assertions.assertThatNoException()
        .isThrownBy(() -> beanDefinitionValidator.validateBeanDefinitions());
  }

  @Test
  @DisplayName("When bean definition with circular dependency of format: a -> b -> c -> a then exception thrown")
  @Order(3)
  void given_BeanDefinitionWithSimpleCircularDependency_When_validateBeanDefinitions_Then_ExceptionThrown() {
    //given
    var aBeanDefinition = new AnnotatedGenericBeanDefinition(A.class);
    aBeanDefinition.setName("a");
    aBeanDefinition.setDependencies(List.of(new BeanDependency("b", null, B.class)));

    var bBeanDefinition = new AnnotatedGenericBeanDefinition(B.class);
    bBeanDefinition.setName("b");
    bBeanDefinition.setDependencies(List.of(new BeanDependency("c", null, C.class)));

    var cBeanDefinition = new AnnotatedGenericBeanDefinition(C.class);
    cBeanDefinition.setName("c");
    cBeanDefinition.setDependencies(List.of(new BeanDependency("a", null, A.class)));

    definitionRegistry.registerBeanDefinition("a", aBeanDefinition);
    definitionRegistry.registerBeanDefinition("b", bBeanDefinition);
    definitionRegistry.registerBeanDefinition("c", cBeanDefinition);

    //when
    //then
    StringBuilder stringBuilder = new StringBuilder("The dependencies of some of the beans form a cycle:%n");
    stringBuilder.append("┌─────┐%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("↑     ↓%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("↑     ↓%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("└─────┘%n");
    String expectedMessage = stringBuilder.toString()
        .formatted("a", getFileLocation(A.class), "b", getFileLocation(B.class), "c",
        getFileLocation(C.class));
    assertThatThrownBy(() -> beanDefinitionValidator.validateBeanDefinitions())
        .isInstanceOf(BeanDefinitionValidationException.class)
        .hasMessage(expectedMessage);
  }

  @Test
  @DisplayName("When bean definition with circular dependency of format a -> d (interface, d1 impl) -> c -> a then exception thrown")
  @Order(4)
  void given_BeanDefinitionWithCircularDependencyWithInterfaces_When_validateBeanDefinitions_Then_ExceptionThrown() {
    // TODO: When bean definition with circular dependency of format a -> d (interface, d1 impl) -> c -> a
    //given
    var aBeanDefinition = new AnnotatedGenericBeanDefinition(A.class);
    aBeanDefinition.setName("a");
    aBeanDefinition.setDependencies(List.of(new BeanDependency("d", null, D.class)));

    var dBeanDefinition = new AnnotatedGenericBeanDefinition(D1.class);
    dBeanDefinition.setName("d1");
    dBeanDefinition.setDependencies(List.of(new BeanDependency("c", null, C.class)));

    var cBeanDefinition = new AnnotatedGenericBeanDefinition(C.class);
    cBeanDefinition.setName("c");
    cBeanDefinition.setDependencies(List.of(new BeanDependency("a", null, A.class)));

    definitionRegistry.registerBeanDefinition("a", aBeanDefinition);
    definitionRegistry.registerBeanDefinition("d1", dBeanDefinition);
    definitionRegistry.registerBeanDefinition("c", cBeanDefinition);

    //when
    //then
    StringBuilder stringBuilder = new StringBuilder("The dependencies of some of the beans form a cycle:%n");
    stringBuilder.append("┌─────┐%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("↑     ↓%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("↑     ↓%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("└─────┘%n");
    String expectedMessage = stringBuilder.toString()
        .formatted("a", getFileLocation(A.class), "d1", getFileLocation(D1.class), "c",
        getFileLocation(C.class));
    assertThatThrownBy(() -> beanDefinitionValidator.validateBeanDefinitions())
        .isInstanceOf(BeanDefinitionValidationException.class)
        .hasMessage(expectedMessage);
  }

  @Test
  @DisplayName("When bean definition with circular dependency of format a -> d (interface with d1, qualified d2 impl) -> c -> a then exception thrown")
  @Order(5)
  void given_BeanDefinitionWithCircularDependencyWithQualifiedInterface_When_validateBeanDefinitions_Then_ExceptionThrown() {
    //given
    var aBeanDefinition = new AnnotatedGenericBeanDefinition(A.class);
    aBeanDefinition.setName("a");
    aBeanDefinition.setDependencies(List.of(new BeanDependency(D.class.getName(), "d2", D.class)));

    var d1BeanDefinition = new AnnotatedGenericBeanDefinition(D1.class);
    d1BeanDefinition.setName("d1");
    d1BeanDefinition.setDependencies(List.of(new BeanDependency("c", null, C.class)));

    var d2BeanDefinition = new AnnotatedGenericBeanDefinition(D2.class);
    d2BeanDefinition.setName("d2");
    d2BeanDefinition.setDependencies(List.of(new BeanDependency("c", null, C.class)));

    var cBeanDefinition = new AnnotatedGenericBeanDefinition(C.class);
    cBeanDefinition.setName("c");
    cBeanDefinition.setDependencies(List.of(new BeanDependency("a", null, A.class)));

    definitionRegistry.registerBeanDefinition("a", aBeanDefinition);
    definitionRegistry.registerBeanDefinition("d1", d1BeanDefinition);
    definitionRegistry.registerBeanDefinition("d2", d2BeanDefinition);
    definitionRegistry.registerBeanDefinition("c", cBeanDefinition);

    //when
    //then
    StringBuilder stringBuilder = new StringBuilder("The dependencies of some of the beans form a cycle:%n");
    stringBuilder.append("┌─────┐%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("↑     ↓%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("↑     ↓%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("└─────┘%n");
    String expectedMessage = stringBuilder.toString()
        .formatted("a", getFileLocation(A.class), "d2", getFileLocation(D2.class), "c",
        getFileLocation(C.class));
    assertThatThrownBy(() -> beanDefinitionValidator.validateBeanDefinitions())
        .isInstanceOf(BeanDefinitionValidationException.class)
        .hasMessage(expectedMessage);
  }

  @Test
  @DisplayName("When bean definition with circular dependency of format a -> d (interface with d1, primary d2 impl) -> c -> a  then exception thrown")
  @Order(6)
  void given_BeanDefinitionWithCircularDependencyWithPrimaryInterfaceImpl_When_validateBeanDefinitions_Then_ExceptionThrown() {
    //given
    var aBeanDefinition = new AnnotatedGenericBeanDefinition(A.class);
    aBeanDefinition.setName("a");
    aBeanDefinition.setDependencies(List.of(new BeanDependency(D.class.getName(), null, D.class)));

    var d1BeanDefinition = new AnnotatedGenericBeanDefinition(D1.class);
    d1BeanDefinition.setName("d1");
    d1BeanDefinition.setPrimary(true);
    d1BeanDefinition.setDependencies(List.of(new BeanDependency("c", null, C.class)));

    var d2BeanDefinition = new AnnotatedGenericBeanDefinition(D2.class);
    d2BeanDefinition.setName("d2");
    d2BeanDefinition.setDependencies(List.of(new BeanDependency("c", null, C.class)));

    var cBeanDefinition = new AnnotatedGenericBeanDefinition(C.class);
    cBeanDefinition.setName("c");
    cBeanDefinition.setDependencies(List.of(new BeanDependency("a", null, A.class)));

    definitionRegistry.registerBeanDefinition("a", aBeanDefinition);
    definitionRegistry.registerBeanDefinition("d1", d1BeanDefinition);
    definitionRegistry.registerBeanDefinition("d2", d2BeanDefinition);
    definitionRegistry.registerBeanDefinition("c", cBeanDefinition);

    //when
    //then
    StringBuilder stringBuilder = new StringBuilder("The dependencies of some of the beans form a cycle:%n");
    stringBuilder.append("┌─────┐%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("↑     ↓%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("↑     ↓%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("└─────┘%n");
    String expectedMessage = stringBuilder.toString()
        .formatted("a", getFileLocation(A.class), "d1", getFileLocation(D1.class), "c",
        getFileLocation(C.class));
    assertThatThrownBy(() -> beanDefinitionValidator.validateBeanDefinitions())
        .isInstanceOf(BeanDefinitionValidationException.class)
        .hasMessage(expectedMessage);
  }

  @Test
  @DisplayName("When bean definition with circular dependency of format a -> d (interface with qualified d1, d2 impl) then nothing thrown")
  @Order(7)
  void given_BeanDefinitionWithInterfaceQualifiedDependencyAndNoCircularDependency_When_validateBeanDefinitions_Then_NoExceptionThrown() {
    var aBeanDefinition = new AnnotatedGenericBeanDefinition(A.class);
    aBeanDefinition.setName("a");
    aBeanDefinition.setDependencies(List.of(new BeanDependency(D.class.getName(), "d1", D.class)));

    var d1BeanDefinition = new AnnotatedGenericBeanDefinition(D1.class);
    d1BeanDefinition.setName("d1");

    var d2BeanDefinition = new AnnotatedGenericBeanDefinition(D2.class);
    d2BeanDefinition.setName("d2");

    definitionRegistry.registerBeanDefinition("a", aBeanDefinition);
    definitionRegistry.registerBeanDefinition("d1", d1BeanDefinition);
    definitionRegistry.registerBeanDefinition("d2", d2BeanDefinition);

    //when
    //then
    Assertions.assertThatNoException()
        .isThrownBy(() -> beanDefinitionValidator.validateBeanDefinitions());
  }

  @Test
  @DisplayName("When bean definition with circular dependency of format a -> d (interface with primary d1, d2 impl) then nothing thrown")
  @Order(8)
  void given_BeanDefinitionWithInterfacePrimaryDependencyAndNoCircularDependency_When_validateBeanDefinitions_Then_NoExceptionThrown() {

    var aBeanDefinition = new AnnotatedGenericBeanDefinition(A.class);
    aBeanDefinition.setName("a");
    aBeanDefinition.setDependencies(List.of(new BeanDependency(D.class.getName(), null, D.class)));

    var d1BeanDefinition = new AnnotatedGenericBeanDefinition(D1.class);
    d1BeanDefinition.setPrimary(true);
    d1BeanDefinition.setName("d1");

    var d2BeanDefinition = new AnnotatedGenericBeanDefinition(D2.class);
    d2BeanDefinition.setName("d2");

    definitionRegistry.registerBeanDefinition("a", aBeanDefinition);
    definitionRegistry.registerBeanDefinition("d1", d1BeanDefinition);
    definitionRegistry.registerBeanDefinition("d2", d2BeanDefinition);

    //when
    //then
    Assertions.assertThatNoException()
        .isThrownBy(() -> beanDefinitionValidator.validateBeanDefinitions());
  }

  @Test
  @DisplayName("When bean definition with circular dependency of format a -> b -> c -> d -> b then exception thrown and only cycle participants present in the error message")
  @Order(9)
  void given_BeanDefinitionsWithCircularDependency_When_validateBeanDefinitions_Then_ExceptionThrownWithOnlyCycleParticipants() {
    //given
    var aBeanDefinition = new AnnotatedGenericBeanDefinition(A.class);
    aBeanDefinition.setName("a");
    aBeanDefinition.setDependencies(List.of(new BeanDependency("b", null, B.class)));

    var bBeanDefinition = new AnnotatedGenericBeanDefinition(B.class);
    bBeanDefinition.setName("b");
    bBeanDefinition.setDependencies(List.of(new BeanDependency("c", null, C.class)));

    var cBeanDefinition = new AnnotatedGenericBeanDefinition(C.class);
    cBeanDefinition.setName("c");
    cBeanDefinition.setDependencies(List.of(new BeanDependency("d", null, D.class)));

    var dBeanDefinition = new AnnotatedGenericBeanDefinition(D1.class);
    dBeanDefinition.setName("d");
    dBeanDefinition.setDependencies(List.of(new BeanDependency("b", null, B.class)));

    definitionRegistry.registerBeanDefinition("d", dBeanDefinition);
    definitionRegistry.registerBeanDefinition("a", aBeanDefinition);
    definitionRegistry.registerBeanDefinition("b", bBeanDefinition);
    definitionRegistry.registerBeanDefinition("c", cBeanDefinition);

    //when
    //then
    StringBuilder stringBuilder = new StringBuilder("The dependencies of some of the beans form a cycle:%n");
    stringBuilder.append("┌─────┐%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("↑     ↓%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("↑     ↓%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("└─────┘%n");
    String expectedMessage = stringBuilder.toString()
        .formatted("b", getFileLocation(B.class), "c", getFileLocation(C.class), "d",
        getFileLocation(D1.class));
    assertThatThrownBy(() -> beanDefinitionValidator.validateBeanDefinitions())
        .isInstanceOf(BeanDefinitionValidationException.class)
        .hasMessage(expectedMessage);
  }

  @Test
  @DisplayName("When bean definition with circular dependency of format a -> a then exception thrown")
  @Order(10)
  void given_BeanDefinitionWithSelfReferenceCircularDependency_When_validateBeanDefinitions_Then_ExceptionThrown() {
    //given
    var aBeanDefinition = new AnnotatedGenericBeanDefinition(A.class);
    aBeanDefinition.setName("a");
    aBeanDefinition.setDependencies(List.of(new BeanDependency("a", null, A.class)));
    definitionRegistry.registerBeanDefinition("a", aBeanDefinition);

    //when
    //then
    StringBuilder stringBuilder = new StringBuilder("The dependencies of some of the beans form a cycle:%n");
    stringBuilder.append("┌─────┐%n");
    stringBuilder.append("|  %s defined in file [%s]%n");
    stringBuilder.append("└─────┘%n");
    String expectedMessage = stringBuilder.toString()
        .formatted("a", getFileLocation(A.class));
    assertThatThrownBy(() -> beanDefinitionValidator.validateBeanDefinitions())
        .isInstanceOf(BeanDefinitionValidationException.class)
        .hasMessage(expectedMessage);
  }

  @Test
  @DisplayName("When bean definition without illegal characters then nothing thrown")
  @Order(11)
  void given_BeanDefinitionWithValidBeanName_When_validateBeanDefinitions_Then_NoExceptionThrown() {
    //given
    var aBeanDefinition = new AnnotatedGenericBeanDefinition(A.class);
    aBeanDefinition.setName("a");
    definitionRegistry.registerBeanDefinition("a", aBeanDefinition);

    //when
    assertThatNoException().isThrownBy(() -> beanDefinitionValidator.validateBeanDefinitions());
  }

  @ParameterizedTest
  @MethodSource("getBeanDefinitionsWithInvalidBeanName")
  @DisplayName("When bean definition contains illegal character then exception thrown")
  @Order(12)
  void given_BeanDefinitionBeanNameWithIllegalCharacters_When_validateBeanDefinitions_Then_ExceptionThrown(
      String beanName) {
    //given
    var aBeanDefinition = new AnnotatedGenericBeanDefinition(A.class);
    aBeanDefinition.setName(beanName);
    definitionRegistry.registerBeanDefinition(beanName, aBeanDefinition);

    //when
    assertThatThrownBy(() -> beanDefinitionValidator.validateBeanDefinitions())
        .isInstanceOf(BeanDefinitionValidationException.class)
        .hasMessage(DISALLOWED_BEAN_NAME_CHARACTERS_EXCEPTION_MESSAGE.formatted(A.class));
  }

  private static Stream<Arguments> getBeanDefinitionsWithInvalidBeanName() {
    return Stream.of(of(""),
        of("test\nname"),
        of("test\bname"),
        of("test\fname"),
        of("test\rname"),
        of("test\tname"));
  }

  private String getFileLocation(Class<?> beanClass) {
    return Path.of(beanClass.getName()).toAbsolutePath().toString()
        .replace(".", "/")
        .concat(".java");
  }

  static class A {

  }

  static class B {

  }

  static class C {

  }

  interface D {

  }

  static class D1 implements D {

  }

  static class D2 implements D {

  }
}
