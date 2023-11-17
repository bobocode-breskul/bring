package io.github.bobocodebreskul.context.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BeanDefinitionReaderUtilsTest {

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
    assertThat(generatedBeanName).isNotBlank();
    assertThat(generatedBeanName).isEqualTo(expectedBeanName);
  }

  @Test
  @DisplayName("Generate bean names for bean classes with equal name")
  @Order(2)
  void givenEqualClassNames_WhenGenerateBeanNames_ThenReturnUniqueBeanName() {
    //given
    var abd1 = new AnnotatedGenericBeanDefinition(MyComponent.class);
    var abd2 = new AnnotatedGenericBeanDefinition(
        io.github.bobocodebreskul.context.support.test.data.MyComponent.class);
    when(registry.isBeanNameInUse(any())).thenReturn(true);
    when(registry.getBeanDefinition(any())).thenReturn(abd1);
    String expectedBeanName1 = StringUtils.uncapitalize(MyComponent.class.getSimpleName());
    String expectedBeanName2 = io.github.bobocodebreskul.context.support.test.data.MyComponent.class.getName();

    //when
    String generatedBeanName1 = BeanDefinitionReaderUtils.generateBeanName(abd1, registry);
    String generatedBeanName2 = BeanDefinitionReaderUtils.generateBeanName(abd2, registry);

    //then
    assertThat(generatedBeanName1).isNotEqualTo(generatedBeanName2);
    assertThat(generatedBeanName1).isEqualTo(expectedBeanName1);
    assertThat(generatedBeanName2).isEqualTo(expectedBeanName2);
  }

  @Test
  @DisplayName("Throw exception when nullable bean definition specified")
  @Order(3)
  void givenNullBeanDefinition_WhenGenerateBeanName_ThenThrowException() {
    //given
    //when
    //then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.generateBeanName(null, registry))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Null bean definition specified");
  }

  @Test
  @DisplayName("Throw exception when bean class is not specified in bean definition")
  @Order(4)
  void givenNullBeanClass_WhenGenerateBeanName_ThenThrowException() {
    //given
    var abd = new AnnotatedGenericBeanDefinition(null);
    //when
    //then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.generateBeanName(abd, registry))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Bean class has not been specified");
  }

  @Test
  @DisplayName("Verify autowired bean class defined as autowire candidate")
  @Order(5)
  void givenBeanClassAutowireCandidate_WhenIsBeanAutowireCandidate_ThenReturnTrue() {
    //given
    var autowiredBeanClass = MyComponent.class;
    var componentClass = AnotherComponent.class;
    var abd = new AnnotatedGenericBeanDefinition(AnotherComponent.class);
    abd.setDependsOn(Collections.singletonList(autowiredBeanClass));
    when(registry.getBeanDefinitions()).thenReturn(List.of(abd));

    //when
    boolean result = BeanDefinitionReaderUtils.isBeanAutowireCandidate(autowiredBeanClass,
        registry);

    //then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Verify bean class without dependencies is not defined as autowire candidate")
  @Order(5)
  void givenBeanClass_WhenIsBeanAutowireCandidate_ThenReturnFalse() {
    //given
    var autowiredBeanClass = MyComponent.class;
    var abd = new AnnotatedGenericBeanDefinition(autowiredBeanClass);
    abd.setDependsOn(Collections.singletonList(autowiredBeanClass));
    when(registry.getBeanDefinitions()).thenReturn(List.of(abd));

    //when
    boolean result = BeanDefinitionReaderUtils.isBeanAutowireCandidate(autowiredBeanClass,
        registry);

    //then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("Get all dependencies of component class: constructor parameters, autowired fields, autowired method arguments")
  @Order(6)
  void givenBeanClassWithDependencies_WhenGetBeanDependencies_ThenReturnAllDependencyClasses() {
    //given
    var componentClass = DependentComponent.class;
    //when
    List<Class<?>> beanDependencies = BeanDefinitionReaderUtils.getBeanDependencies(componentClass);

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
