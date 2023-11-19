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
import io.github.bobocodebreskul.context.exception.BeanDefinitionDuplicateException;
import io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BeanDefinitionReaderUtilsTest {

  private BeanDefinitionRegistry registry;

  @BeforeEach
  void init() {
    registry = Mockito.mock(BeanDefinitionRegistry.class);
  }

  @Test
  @DisplayName("Generate bean name for bean definition based on bean class")
  @Order(1)
  void givenValidBeanDefinition_WhenGenerateBeanName_ThenReturnValidBeanName() {
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
  void givenClassNameInRegistry_WhenGenerateBeanNames_ThenNoDuplicateExceptionIsThrown() {
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
  void givenEqualClassNames_WhenGenerateBeanNames_ThenThrowBeanDefinitionDuplicateException() {
    //given
    var abd1 = new AnnotatedGenericBeanDefinition(MyComponent.class);
    var abd2 = new AnnotatedGenericBeanDefinition(
        io.github.bobocodebreskul.context.support.test.data.MyComponent.class);
    when(registry.isBeanNameInUse(any())).thenReturn(true);
    when(registry.getBeanDefinition(any())).thenReturn(abd1);

    //when
    //then
    String expectedMessage = "Bean definition %s already exist".formatted(abd2.getBeanClass().getName());
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.generateBeanName(abd2, registry))
        .isInstanceOf(BeanDefinitionDuplicateException.class)
        .hasMessage(expectedMessage);
  }

  @Test
  @DisplayName("Throw exception when nullable bean definition specified")
  @Order(4)
  void givenNullBeanDefinition_WhenGenerateBeanName_ThenThrowException() {
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
  void givenBeanClass_WhenGenerateClassBeanName_ThenReturnValidBeanName(){
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
  void givenNullClassType_WhenGenerateClassBeanName_ThenThrowNullPointerException() {
    // when
    // then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.generateClassBeanName(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Bean class has not been specified");
  }

  @Test
  @DisplayName("Get all dependencies of component class: constructor parameters, autowired fields, autowired method arguments")
  @Order(8)
  void givenBeanClassWithDependencies_WhenGetBeanDependencies_ThenReturnAllDependencyClasses() {
    //given
    Class<DependentComponent> componentClass = DependentComponent.class;
    //when
    List<Class<?>> beanDependencies = BeanDefinitionReaderUtils.getBeanDependencies(componentClass)
        .stream()
        .map(BeanDependency::type)
        .collect(Collectors.toList());

    //then
    assertThat(beanDependencies).containsExactlyInAnyOrder(
        MyComponent.class,
        AnotherComponent.class,
        OneMoreComponent.class);

  }

  @Test
  @DisplayName("Get bean dependencies from the single class constructor")
  @Order(9)
  void givenBeanClassWithOneConstructor_WhenGetBeanDependencies_ThenReturnValidDependency(){
    //given
    var beanClass = ConstructorDependentComponent.class;
    var expectedDependencyClass = MyComponent.class;

    //when
    var dependencies = BeanDefinitionReaderUtils.getBeanDependencies(beanClass);

    //then
    assertThat(dependencies.stream().map(BeanDependency::type).toArray())
        .containsExactly(expectedDependencyClass);
  }

  @Test
  @DisplayName("Throw BeanDefinitionCreationException when bean has more then 1 constructor")
  @Order(10)
  void givenBeanClassWithSeveralConstructors_WhenGetBeanDependencies_ThenShouldThrowException() {
    //given
    //when
    //then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.getBeanDependencies(ComponentWith2Constructors.class))
        .isInstanceOf(BeanDefinitionCreationException.class)
        .hasMessageContaining("Bean candidate should have only one constructor declared");
  }

  @Test
  @DisplayName("Throw exception when bean class has more than one parameterized constructor but no default one and no auto wired one")
  @Disabled
  @Order(11)
  void givenBeanClassWithMultiConstructorAndWithoutDefaultConstructorAndWithoutAutowired_WhenGetBeanDependencies_ThenThrowException() {
    // TODO: IMPLEMENT trow exception when there are several constructors without @Autowired and a default constructor is not present
  }

  @Test
  @DisplayName("Get bean dependencies when bean class has more than one constructor and one of them is default constructor")
  @Disabled
  @Order(12)
  void givenBeanClassWithMultiConstructorIncludingDefault_WhenGetBeanDependencies_ThenReturnValidDependency() {
    // TODO: IMPLEMENT return valid when more than one constructor present and one of the is default
  }

  @Test
  @DisplayName("Get bean dependencies when bean class has more than one constructor and on of them is auto wired")
  @Disabled
  @Order(13)
  void givenBeanClassWithMultiConstructorAndOneOfThemAutowired_WhenGetBeanDependencies_ThenReturnValidDependency() {
    // TODO: IMPLEMENT find single autowired constructor if more than 1 constructor present and one on of them marked as @Autowired
  }

  @Test
  @DisplayName("Throw exception when bean class has more than one constructor annotated with @Autowired")
  @Disabled
  @Order(14)
  void givenBeanClassWithMultipleConstructorsAnnotatedWithAutowired_WhenGetBeanDependencies_ThenThrowException(){
    // TODO: IMPLEMENT throw exception when more than one constructor present and more than one @Autowired present
  }

  @Test
  @DisplayName("Get bean dependencies when bean class has only field autowired dependencies")
  @Disabled
  @Order(15)
  void givenBeanClassWithOnlyAutowiredFieldDependencies_WhenGetBeanDependencies_ThenReturnValidDependencies() {
  // TODO: IMPLEMENT only field dependencies found
  }

  @Test
  @DisplayName("Get bean dependencies when bean class has only method autowired dependencies")
  @Disabled
  @Order(16)
  void givenBeanClassWithOnlyMethodDependencies_WhenGetBeanDependencies_ThenReturnValidMethodDependencies() {
    // TODO: IMPLEMENT only method dependencies found
  }

  @Test
  @DisplayName("Verify autowired bean class defined as autowire candidate")
  @Order(17)
  void givenBeanClassAutowireCandidate_WhenIsBeanAutowireCandidate_ThenReturnTrue() {
    //given
    var autowiredBeanClass = MyComponent.class;
    var componentClass = AnotherComponent.class;
    var abd = new AnnotatedGenericBeanDefinition(componentClass);
    abd.setDependencies(List.of(new BeanDependency(autowiredBeanClass.getSimpleName(), autowiredBeanClass)));
    when(registry.getBeanDefinitions()).thenReturn(List.of(abd));

    //when
    boolean result = BeanDefinitionReaderUtils.isBeanAutowireCandidate(autowiredBeanClass,
        registry);

    //then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Verify bean class without dependencies is not defined as autowire candidate")
  @Order(18)
  void givenBeanClass_WhenIsBeanAutowireCandidate_ThenReturnFalse() {
    //given
    var autowiredBeanClass = MyComponent.class;
    var abd = new AnnotatedGenericBeanDefinition(autowiredBeanClass);
    abd.setDependencies(Collections.singletonList(new BeanDependency(autowiredBeanClass.getSimpleName(),autowiredBeanClass)));
    when(registry.getBeanDefinitions()).thenReturn(List.of(abd));

    //when
    boolean result = BeanDefinitionReaderUtils.isBeanAutowireCandidate(autowiredBeanClass,
        registry);

    //then
    assertThat(result).isFalse();
  }

  @BringComponent
  static class MyComponent {

  }

  @BringComponent
  static class AnotherComponent {

    @Autowired
    private MyComponent myComponent;

  }

  @BringComponent
  static class OneMoreComponent {

  }

  @BringComponent
  static class IgnoreComponent {

  }

  @BringComponent
  static class DependentComponent {

    private final MyComponent myComponent;

    @Autowired
    private AnotherComponent anotherComponent;

    private OneMoreComponent oneMoreComponent;
    private IgnoreComponent ignoreComponent;

    public DependentComponent(MyComponent myComponent) {
      this.myComponent = myComponent;
    }

    @Autowired
    public void setOneMoreComponent(OneMoreComponent oneMoreComponent) {
      this.oneMoreComponent = oneMoreComponent;
    }
  }
}
