package io.github.bobocodebreskul.context.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.Primary;
import io.github.bobocodebreskul.context.annotations.Scope;
import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.BeanDefinitionCreationException;
import io.github.bobocodebreskul.context.exception.DuplicateBeanDefinitionException;
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
class AnnotatedBeanDefinitionReaderTest {

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
  void given_BeanClass_When_RegisterBean_Then_BeanDefinitionCreatedAndPassedToRegistry() {
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
    assertThat(actualBeanDefinition.getDependencies()).isEmpty();
    assertThat(actualBeanDefinition.isAutowireCandidate()).isFalse();
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }

  @Test
  @DisplayName("Test bean definition with dependencies is created and passed to registry")
  @Order(2)
  void given_BeanClassWithDependencies_When_RegisterBean_Then_BeanDefinitionCreatedAndPassedToRegistry() {
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
    assertThat(actualBeanDefinition.getDependencies()).isNotEmpty();
    assertThat(actualBeanDefinition.getDependencies().stream().map(BeanDependency::type).toArray())
        .containsExactlyInAnyOrder(MyComponent.class);
    assertThat(actualBeanDefinition.isAutowireCandidate()).isFalse();
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }

  @Test
  @DisplayName("Test bean definition autowired candidate property marked as true")
  @Order(3)
  void given_BeanClassInjected_When_RegisterBean_Then_BeanDefinitionAutowiredCandidateTrue() {
    //given
    var beanClass = MyComponent.class;
    var beanClassWithDependency = AnotherComponent.class;
    var abd = new AnnotatedGenericBeanDefinition(beanClassWithDependency);
    abd.setDependencies(List.of(new BeanDependency(beanClass.getSimpleName(), beanClass)));
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
    assertThat(actualBeanDefinition.getDependencies()).isEmpty();
    assertThat(actualBeanDefinition.isAutowireCandidate()).isTrue();
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }

  @Test
  @DisplayName("Test bean definition registered by specified bean name")
  @Order(4)
  void given_BeanClassAndName_When_RegisterBean_Then_BeanDefinitionRegisteredBySpecifiedName() {
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
    assertThat(actualBeanDefinition.getDependencies()).isEmpty();
    assertThat(actualBeanDefinition.isAutowireCandidate()).isFalse();
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }


  @Test
  @DisplayName("Throw DuplicateBeanDefinitionException when duplicate bean name specified")
  @Order(5)
  void given_BeanClassWithDuplicateName_When_RegisterBean_Then_ThrowDuplicateBeanDefinitionException() {
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
  void given_PrimaryBeanClass_When_RegisterBean_Then_BeanDefinitionPrimaryPropertyTrue() {
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
    assertThat(actualBeanDefinition.getDependencies()).isEmpty();
    assertThat(actualBeanDefinition.isAutowireCandidate()).isFalse();
    assertThat(actualBeanDefinition.isPrimary()).isTrue();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }



  @Test
  @Order(7)
  @DisplayName("Test when scope singleton when no scope provided")
  void given_SingletonBeanClass_When_No_Scope_Provided_Then_BeanDefinitionIsSingletonTrue(){
    //data
    var beanClass = DefaultSingletonComponent.class;

    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    //when
    annotatedBeanDefinitionReader.registerBean(beanClass);

    //then
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
    assertThat(actualBeanDefinition.isPrototype()).isFalse();
  }



  @Test
  @DisplayName("Test when scope singleton provided")
  @Order(8)
  void given_BeanClassWithSingletonScope_When_RegisterBean_Then_BeanDefinitionIsSingletonTrue() {
    var beanClass = MySingletonComponent.class;
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    //when
    annotatedBeanDefinitionReader.registerBean(beanClass);

    //then
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
    assertThat(actualBeanDefinition.isPrototype()).isFalse();
  }

  @Test
  @Order(9)
  @DisplayName("Test scope prototype when prototype scope provided")
  void given_PrototypeBeanClass_When_RegisterBean_Then_BeanDefinitionIsPrototypeTrue(){
    //data
    var beanClass = PrototypeComponent.class;

    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    //when
    annotatedBeanDefinitionReader.registerBean(beanClass);

    //then
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition.isSingleton()).isFalse();
    assertThat(actualBeanDefinition.isPrototype()).isTrue();
  }

  @Test
  @DisplayName("Test when default scope provided")
  @Order(10)
  void given_BeanClassWithDefaultScope_When_RegisterBean_Then_BeanDefinitionIsSingletonTrue() {
    var beanClass = MyDefaultScopeComponent.class;
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    //when
    annotatedBeanDefinitionReader.registerBean(beanClass);

    //then
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
    assertThat(actualBeanDefinition.isPrototype()).isFalse();
  }

  @Test
  @DisplayName("Test when non existing scope provided")
  @Order(11)
  void given_BeanClassWithNonExistingScope_When_RegisterBean_Then_ThrowBeanDefinitionCreationException() {
    //given
    var beanClass = MyNonExistingScopeComponent.class;
    String scopeName = beanClass.getAnnotation(Scope.class).value();

    //when
    //then
    assertThatThrownBy(() ->
        annotatedBeanDefinitionReader.registerBean(beanClass))
        .isInstanceOf(BeanDefinitionCreationException.class)
        .hasMessageContaining("Invalid scope name provided %s".formatted(scopeName));
  }

  @BringComponent
  static class MyComponent {}

  static class AnotherComponent {

    @Autowired
    private MyComponent myComponent;
  }

  @Primary
  @BringComponent
  static class PrimaryComponent {}

  @BringComponent
  @Scope(BeanDefinition.SINGLETON_SCOPE)
  static class MySingletonComponent {}

  @BringComponent
  @Scope()
  static class MyDefaultScopeComponent {}

  @BringComponent
  @Scope("non existing")
  static class MyNonExistingScopeComponent {}

  @BringComponent
  static class DefaultSingletonComponent {}

  @BringComponent
  @Scope(BeanDefinition.PROTOTYPE_SCOPE)
  static class PrototypeComponent {}
}
