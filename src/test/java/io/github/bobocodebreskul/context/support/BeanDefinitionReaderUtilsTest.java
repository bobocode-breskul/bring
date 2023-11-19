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
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
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
  void givenNullClassType_WhenGenerateClassBeanName_ThenThrowNullPointerException() {
    // when
    // then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.generateClassBeanName(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Bean class has not been specified");
  }

  @Test
  @DisplayName("Verify autowired bean class defined as autowire candidate")
  @Order(6)
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
  @Order(7)
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

  @Test
  @DisplayName("Get all dependencies of component class: constructor parameters, autowired fields, autowired method arguments")
  @Order(8)
  void givenBeanClassWithDependencies_WhenGetBeanDependencies_ThenReturnAllDependencyClasses() {
    //given
    Class<DependentComponent> componentClass = DependentComponent.class;
    //when
    List<Class<?>> beanDependencies = BeanDefinitionReaderUtils.getBeanDependencies(componentClass).stream()
        .map(BeanDependency::type)
        .collect(Collectors.toList());

    //then
    assertThat(beanDependencies).containsExactlyInAnyOrder(
        MyComponent.class,
        AnotherComponent.class,
        OneMoreComponent.class);

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
