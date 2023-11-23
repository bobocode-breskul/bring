package io.github.bobocodebreskul.context.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.BeanDefinitionCreationException;
import io.github.bobocodebreskul.context.exception.BeanDefinitionDuplicateException;
import io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class BeanDefinitionReaderUtilsTest {

  @Mock
  private BeanDefinitionRegistry registry;

  @Test
  @DisplayName("Generate bean name for bean definition based on bean class")
  @Order(1)
  void given_ValidBeanDefinition_When_GenerateBeanName_Then_ReturnValidBeanName() {
    //given
    var abd = new AnnotatedGenericBeanDefinition(MyComponent.class);
    String expectedBeanName = "myComponent";

    //when
    String generatedBeanName = BeanDefinitionReaderUtils.generateBeanName(abd, registry);

    //then
    assertThat(generatedBeanName)
        .isNotBlank()
        .isEqualTo(expectedBeanName);
  }

  @Test
  @DisplayName("Generate bean names for bean classes with equal name")
  @Order(2)
  void given_ClassNameInRegistry_When_GenerateBeanNames_Then_NoDuplicateExceptionIsThrown() {
    //given
    var abd1 = new AnnotatedGenericBeanDefinition(MyComponent.class);
    when(registry.isBeanNameInUse(any())).thenReturn(true);
    when(registry.getBeanDefinition(any())).thenReturn(abd1);

    //when
    //then
    assertThatNoException()
        .isThrownBy(() -> BeanDefinitionReaderUtils.generateBeanName(abd1, registry));
  }

  @Test
  @DisplayName("Generate bean names for bean classes with equal name")
  @Order(3)
  void given_EqualClassNames_When_GenerateBeanNames_Then_ThrowBeanDefinitionDuplicateException() {
    //given
    var abd1 = new AnnotatedGenericBeanDefinition(MyComponent.class);
    var abd2 = new AnnotatedGenericBeanDefinition(
        io.github.bobocodebreskul.context.support.test.data.MyComponent.class);
    when(registry.isBeanNameInUse(any())).thenReturn(true);
    when(registry.getBeanDefinition(any())).thenReturn(abd1);

    //when
    //then
    String expectedMessage = "Bean definition %s already exist".formatted(
        abd2.getBeanClass().getName());
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.generateBeanName(abd2, registry))
        .isInstanceOf(BeanDefinitionDuplicateException.class)
        .hasMessage(expectedMessage);
  }

  @Test
  @DisplayName("Throw exception when nullable bean definition specified")
  @Order(4)
  void given_NullBeanDefinition_When_GenerateBeanName_Then_ThrowException() {
    //given
    //when
    //then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.generateBeanName(null, registry))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Null bean definition specified");
  }

  @Test
  @DisplayName("Throw NullPointerException when bean class is not specified in bean definition")
  @Order(5)
  void givenNullBeanClass_WhenGenerateBeanName_ThenThrowNullPointerException() {
    //given
    var abd = new AnnotatedGenericBeanDefinition(null);
    //when
    //then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.generateBeanName(abd, registry))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Bean class has not been specified");
  }

  @Test
  @DisplayName("Generate bean name based on specified bean class")
  @Order(6)
  void given_BeanClass_When_GenerateClassBeanName_Then_ReturnValidBeanName() {
    //given
    var beanClass = MyComponent.class;
    var expectedBeanName = StringUtils.uncapitalize(beanClass.getSimpleName());

    //when
    var actualBeanName = BeanDefinitionReaderUtils.generateClassBeanName(beanClass);

    //then
    assertThat(actualBeanName).isEqualTo(expectedBeanName);
  }

  @Test
  @DisplayName("Throw NullPointerException when provided class type is null")
  @Order(7)
  void given_NullClassType_When_GenerateClassBeanName_Then_ThrowNullPointerException() {
    // when
    // then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.generateClassBeanName(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Bean class has not been specified");
  }

  @Test
  @DisplayName("Get bean dependencies from the default constructor")
  @Order(8)
  void given_BeanClassWithDefaultConstructor_When_GetConstructorBeanDependencies_Then_ReturnEmptyList() {
    //given
    var beanDefaultConstructor = MyComponent.class.getDeclaredConstructors()[0];

    //when
    var dependencies =
        BeanDefinitionReaderUtils.getConstructorBeanDependencies(beanDefaultConstructor);

    //then
    assertThat(dependencies).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("getConstructors")
  @DisplayName("Get bean dependencies for constructor with n parameters")
  @Order(9)
  void given_ConstructorsWithNArguments_When_GetConstructorBeanDependencies_Then_ReturnAllConstructorArgumentTypes(
      Constructor<?> constructor, int result) {
    //when
    var dependencies = BeanDefinitionReaderUtils.getConstructorBeanDependencies(constructor);

    //then
    var expectedTypes = constructor.getParameterTypes();
    assertThat(dependencies.stream().map(BeanDependency::type).toArray())
        .hasSize(result)
        .containsExactlyInAnyOrder(expectedTypes);
  }

  @Test
  @DisplayName("Throw NullPointerException with meaningful description for nullable constructor")
  @Order(10)
  void given_NullableConstructor_When_GetConstructorBeanDependencies_Then_ThrowNullPointerException() {
    // when
    // then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.getConstructorBeanDependencies(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Bean constructor has not been specified");
  }

  @Test
  @DisplayName("Get bean constructor when bean class has only default constructor")
  @Order(11)
  void given_BeanClassWithSingleDefaultConstructor_When_FindBeanInitConstructor_Then_ReturnDefaultConstructor() {
    //given
    var beanClass = MyComponent.class;
    var beanName = BeanDefinitionReaderUtils.generateClassBeanName(beanClass);

    //when
    var result = BeanDefinitionReaderUtils.findBeanInitConstructor(beanClass, beanName);

    //then
    assertThat(result).isEqualTo(ReflectionUtils.getDefaultConstructor(beanClass));
  }

  @Test
  @DisplayName("Get bean constructor when bean class has single constructor with parameters")
  @Order(12)
  void given_BeanClassWithSingleMultiParamsConstructor_When_FindBeanInitConstructor_Then_ReturnConstructor() {
    //given
    var beanClass = MultipleArgumentDependentComponent.class;
    var beanName = BeanDefinitionReaderUtils.generateClassBeanName(beanClass);

    //when
    var result = BeanDefinitionReaderUtils.findBeanInitConstructor(beanClass, beanName);

    //then
    var expected = beanClass.getDeclaredConstructors()[0];
    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("Get bean constructor when bean class has more than one constructor and one of them is auto wired")
  @Order(13)
  void given_BeanClassWithMultiConstructorAndOneOfThemAutowired_When_FindBeanInitConstructor_Then_ReturnConstructor() {
    //given
    var beanClass = MultipleConstructorDependentComponent.class;
    var beanName = BeanDefinitionReaderUtils.generateClassBeanName(beanClass);

    //when
    var result = BeanDefinitionReaderUtils.findBeanInitConstructor(beanClass, beanName);

    //then
    var expected = ReflectionUtils.getConstructorsAnnotatedWith(Autowired.class,
        beanClass.getDeclaredConstructors()).get(0);
    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("Get bean constructor when bean class has more than one constructor and one of them is default constructor")
  @Order(14)
  void given_BeanClassWithMultiConstructorIncludingDefault_When_FindBeanInitConstructor_Then_ReturnValidConstructor() {
    //given
    var beanClass = MultipleConstructorIncludingDefaultComponent.class;
    var beanName = BeanDefinitionReaderUtils.generateClassBeanName(beanClass);

    //when
    var result = BeanDefinitionReaderUtils.findBeanInitConstructor(beanClass, beanName);

    //then
    var expected = ReflectionUtils.getDefaultConstructor(beanClass);
    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("Throw exception when bean class has more than one constructor annotated with @Autowired")
  @Order(15)
  void given_BeanClassWithMultipleConstructorsAnnotatedWithAutowired_When_FindBeanInitConstructor_Then_ThrowException() {
    //given
    var beanClass = MultipleAutowiredConstructorComponent.class;
    var beanName = BeanDefinitionReaderUtils.generateClassBeanName(beanClass);
    var declaredConstructors = beanClass.getDeclaredConstructors();

    //when
    //then
    var expected1 = declaredConstructors[0];
    var expected2 = declaredConstructors[1];
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.findBeanInitConstructor(beanClass, beanName))
        .isInstanceOf(BeanDefinitionCreationException.class)
        .hasMessage(
            BeanDefinitionReaderUtils.MULTIPLE_AUTOWIRED_CONSTRUCTORS_MESSAGE.formatted(beanName,
                expected2, expected1));
  }

  @Test
  @DisplayName("Throw exception when bean class has more than one parameterized constructor but no default one and no auto wired one")
  @Order(16)
  void given_BeanClassWithMultiConstructorAndWithoutDefaultConstructorAndWithoutAutowired_When_FindBeanInitConstructor_Then_ThrowException() {
    //given
    var beanClass = MultipleConstructorWithoutAutowiredAndDefaultComponent.class;
    var beanName = BeanDefinitionReaderUtils.generateClassBeanName(beanClass);

    //when
    //then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.findBeanInitConstructor(beanClass, beanName))
        .isInstanceOf(BeanDefinitionCreationException.class)
        .hasMessage(
            BeanDefinitionReaderUtils.NO_DEFAULT_CONSTRUCTOR_MESSAGE.formatted(beanName,
                beanClass.getName()));
  }

  @ParameterizedTest
  @MethodSource("getBeanClassesWithoutConstructors")
  @DisplayName("Throw exception when interface/primitive/array/void class specified")
  @Order(17)
  void given_BeanClassWithoutConstructor_When_FindBeanInitConstructor_Then_ThrowException(
      Class<?> beanClass, String beanName) {
    //given
    //when
    //then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.findBeanInitConstructor(beanClass, beanName))
        .isInstanceOf(BeanDefinitionCreationException.class)
        .hasMessage(
            BeanDefinitionReaderUtils.CLASS_WITHOUT_CONSTRUCTORS_MESSAGE.formatted(beanName,
                beanClass.getName()));
  }

  @Test
  @DisplayName("Get bean dependencies when bean class has only field autowired dependencies")
  @Disabled
  @Order(18)
  void given_BeanClassWithOnlyAutowiredFieldDependencies_When_FindBeanInitConstructor_Then_ReturnValidDependencies() {
    // TODO: IMPLEMENT only field dependencies found
  }

  @Test
  @DisplayName("Get bean dependencies when bean class has only method autowired dependencies")
  @Disabled
  @Order(19)
  void given_BeanClassWithOnlyMethodDependencies_When_FindBeanInitConstructor_Then_ReturnValidMethodDependencies() {
    // TODO: IMPLEMENT only method dependencies found
  }

  @Test
  @DisplayName("Verify autowired bean class defined as autowire candidate")
  @Order(20)
  void given_BeanClassAutowireCandidate_When_IsBeanAutowireCandidate_Then_ReturnTrue() {
    //given
    var autowiredBeanClass = MyComponent.class;
    var componentClass = AnotherComponent.class;
    var abd = new AnnotatedGenericBeanDefinition(componentClass);
    abd.setDependencies(
        List.of(new BeanDependency(autowiredBeanClass.getSimpleName(), autowiredBeanClass)));
    when(registry.getBeanDefinitions()).thenReturn(List.of(abd));

    //when
    boolean result = BeanDefinitionReaderUtils.isBeanAutowireCandidate(autowiredBeanClass,
        registry);

    //then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Verify bean class without dependencies is not defined as autowire candidate")
  @Order(21)
  void given_BeanClass_When_IsBeanAutowireCandidate_Then_ReturnFalse() {
    //given
    var autowiredBeanClass = MyComponent.class;
    var abd = new AnnotatedGenericBeanDefinition(autowiredBeanClass);
    abd.setDependencies(Collections.singletonList(
        new BeanDependency(autowiredBeanClass.getSimpleName(), autowiredBeanClass)));
    when(registry.getBeanDefinitions()).thenReturn(List.of(abd));

    //when
    boolean result = BeanDefinitionReaderUtils.isBeanAutowireCandidate(autowiredBeanClass,
        registry);

    //then
    assertThat(result).isFalse();
  }

  private static Stream<Arguments> getConstructors() {
    return Stream.of(Arguments.of(AnotherComponent.class.getDeclaredConstructors()[0], 1),
        Arguments.of(MultipleArgumentDependentComponent.class.getDeclaredConstructors()[0], 2));
  }

  private static Stream<Arguments> getBeanClassesWithoutConstructors() {
    return Stream.of(Arguments.of(Function.class, "function"),
        Arguments.of(int.class, "integer"),
        Arguments.of(int[].class, "integerArray"),
        Arguments.of(void.class, "void"));
  }

  @BringComponent
  static class MyComponent {

  }

  @BringComponent
  @RequiredArgsConstructor
  static class AnotherComponent {

    private final MyComponent myComponent;
  }

  @BringComponent
  @RequiredArgsConstructor
  static class MultipleArgumentDependentComponent {

    private final MyComponent component;
    private final AnotherComponent anotherComponent;
  }

  @BringComponent
  static class MultipleConstructorDependentComponent {

    private MyComponent myComponent;
    private AnotherComponent anotherComponent;

    public MultipleConstructorDependentComponent(MyComponent myComponent) {
      this.myComponent = myComponent;
    }

    @Autowired
    public MultipleConstructorDependentComponent(AnotherComponent anotherComponent) {
      this.anotherComponent = anotherComponent;
    }
  }

  @BringComponent
  static class MultipleConstructorIncludingDefaultComponent {

    private MyComponent myComponent;
    private AnotherComponent anotherComponent;

    public MultipleConstructorIncludingDefaultComponent() {
    }

    public MultipleConstructorIncludingDefaultComponent(MyComponent myComponent) {
      this.myComponent = myComponent;
    }

    public MultipleConstructorIncludingDefaultComponent(AnotherComponent anotherComponent) {
      this.anotherComponent = anotherComponent;
    }
  }

  @BringComponent
  static class MultipleAutowiredConstructorComponent {

    private MyComponent myComponent;
    private AnotherComponent anotherComponent;

    @Autowired
    public MultipleAutowiredConstructorComponent(MyComponent myComponent) {
      this.myComponent = myComponent;
    }

    @Autowired
    public MultipleAutowiredConstructorComponent(AnotherComponent anotherComponent) {
      this.anotherComponent = anotherComponent;
    }
  }

  @BringComponent
  static class MultipleConstructorWithoutAutowiredAndDefaultComponent {

    private MyComponent myComponent;
    private AnotherComponent anotherComponent;

    public MultipleConstructorWithoutAutowiredAndDefaultComponent(MyComponent myComponent) {
      this.myComponent = myComponent;
    }

    public MultipleConstructorWithoutAutowiredAndDefaultComponent(
        AnotherComponent anotherComponent) {
      this.anotherComponent = anotherComponent;
    }
  }
}
