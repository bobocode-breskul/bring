package io.github.bobocodebreskul.context.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.Primary;
import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.exception.DuplicateBeanDefinitionException;
import io.github.bobocodebreskul.context.registry.AnnotatedBeanDefinitionReader;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AnnotatedBeanDefinitionReaderTest {

  private BeanDefinitionRegistry registry;
  private AnnotatedBeanDefinitionReader annotatedBeanDefinitionReader;

  @BeforeEach
  void init() {
    registry = Mockito.spy(BeanDefinitionRegistry.class);
    annotatedBeanDefinitionReader = new AnnotatedBeanDefinitionReader(registry);
  }

  @Test
  @DisplayName("Test simple bean definition is created and passed to registry")
  @Order(1)
  void givenBeanClass_WhenRegisterBean_ThenBeanDefinitionCreatedAndPassedToRegistry() {
    //given
    var beanClass = MyComponent.class;
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    //when
    annotatedBeanDefinitionReader.registerBean(beanClass);

    //then
    verify(registry, atLeastOnce()).isBeanNameInUse(anyString());
    verify(registry, atMostOnce()).registerBeanDefinition(anyString(), any());
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    assertThat(nameCaptor.getValue()).isEqualTo(
        StringUtils.uncapitalize(beanClass.getSimpleName()));
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(beanClass);
    assertThat(actualBeanDefinition.getDependsOn()).isEmpty();
    assertThat(actualBeanDefinition.isAutowireCandidate()).isFalse();
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }

  @Test
  @DisplayName("Test bean definition with dependencies is created and passed to registry")
  @Order(2)
  void givenBeanClassWithDependencies_WhenRegisterBean_ThenBeanDefinitionCreatedAndPassedToRegistry() {
    //given
    var beanClass = MyComponent.class;
    var beanClassWithDependency = AnotherComponent.class;
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    //when
    annotatedBeanDefinitionReader.register(beanClass, beanClassWithDependency);

    //then
    verify(registry, atLeastOnce()).isBeanNameInUse(anyString());
    verify(registry, times(2)).registerBeanDefinition(nameCaptor.capture(),
        definitionCaptor.capture());
    assertThat(nameCaptor.getValue()).isEqualTo(
        StringUtils.uncapitalize(beanClassWithDependency.getSimpleName()));
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(beanClassWithDependency);
    assertThat(actualBeanDefinition.getDependsOn()).isNotEmpty();
    assertThat(actualBeanDefinition.getDependsOn()).containsExactlyInAnyOrder(MyComponent.class);
    assertThat(actualBeanDefinition.isAutowireCandidate()).isFalse();
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }

  @Test
  @DisplayName("Test bean definition autowired candidate property marked as true")
  @Order(3)
  void givenBeanClassInjected_WhenRegisterBean_ThenBeanDefinitionAutowiredCandidateTrue() {
    //given
    var beanClass = MyComponent.class;
    var beanClassWithDependency = AnotherComponent.class;
    var abd = new AnnotatedGenericBeanDefinition(beanClassWithDependency);
    abd.setDependsOn(List.of(beanClass));
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);
    doReturn(Collections.singletonList(abd)).when(registry).getBeanDefinitions();

    //when
    annotatedBeanDefinitionReader.register(beanClassWithDependency, beanClass);

    //then
    verify(registry, atLeastOnce()).isBeanNameInUse(anyString());
    verify(registry, times(2)).registerBeanDefinition(nameCaptor.capture(),
        definitionCaptor.capture());
    assertThat(nameCaptor.getValue()).isEqualTo(
        StringUtils.uncapitalize(beanClass.getSimpleName()));
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(beanClass);
    assertThat(actualBeanDefinition.getDependsOn()).isEmpty();
    assertThat(actualBeanDefinition.isAutowireCandidate()).isTrue();
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }

  @Test
  @DisplayName("Test bean definition registered by specified bean name")
  @Order(4)
  void givenBeanClassAndName_WhenRegisterBean_ThenBeanDefinitionRegisteredBySpecifiedName() {
    //given
    var beanClass = MyComponent.class;
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);
    String expectedBeanName = "SomeBeanName";
    //when
    annotatedBeanDefinitionReader.registerBean(beanClass, expectedBeanName);

    //then
    verify(registry, atLeastOnce()).isBeanNameInUse(anyString());
    verify(registry, atMostOnce()).registerBeanDefinition(anyString(), any());
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    assertThat(nameCaptor.getValue()).isEqualTo(expectedBeanName);
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(beanClass);
    assertThat(actualBeanDefinition.getName()).isEqualTo(expectedBeanName);
    assertThat(actualBeanDefinition.getDependsOn()).isEmpty();
    assertThat(actualBeanDefinition.isAutowireCandidate()).isFalse();
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }


  @Test
  @DisplayName("Throw DuplicateBeanDefinitionException when duplicate bean name specified")
  @Order(5)
  void givenBeanClassWithDuplicateName_WhenRegisterBean_ThenThrowDuplicateBeanDefinitionException() {
    //given
    var firstBeanClass = MyComponent.class;
    var secondBeanClass = AnotherComponent.class;
    String expectedBeanName = "SomeBeanName";
    doReturn(true).when(registry).isBeanNameInUse(expectedBeanName);

    //when
    //then
    assertThatThrownBy(() ->
        annotatedBeanDefinitionReader.registerBean(secondBeanClass, expectedBeanName))
        .isInstanceOf(DuplicateBeanDefinitionException.class)
        .hasMessageContaining("The bean definition with specified name %s already exists"
            .formatted(expectedBeanName));
  }


  @Test
  @DisplayName("Test bean definition isPrimary property marked as true")
  @Order(6)
  void givenPrimaryBeanClass_WhenRegisterBean_ThenBeanDefinitionPrimaryPropertyTrue() {
    //given
    var primaryBeanClass = PrimaryComponent.class;
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    //when
    annotatedBeanDefinitionReader.register(primaryBeanClass);

    //then
    verify(registry, atLeastOnce()).isBeanNameInUse(anyString());
    verify(registry, atMostOnce()).registerBeanDefinition(anyString(),
        definitionCaptor.capture());
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(primaryBeanClass);
    assertThat(actualBeanDefinition.getDependsOn()).isEmpty();
    assertThat(actualBeanDefinition.isAutowireCandidate()).isFalse();
    assertThat(actualBeanDefinition.isPrimary()).isTrue();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }

  @BringComponent
  static class MyComponent {

  }

  static class AnotherComponent {

    @Autowired
    private MyComponent myComponent;
  }

  @Primary
  @BringComponent
  static class PrimaryComponent {

  }
}