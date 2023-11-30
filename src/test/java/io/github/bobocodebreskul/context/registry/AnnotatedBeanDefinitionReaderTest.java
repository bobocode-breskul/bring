package io.github.bobocodebreskul.context.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.bobocodebreskul.Config;
import io.github.bobocodebreskul.context.annotations.BringBean;
import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.BringConfiguration;
import io.github.bobocodebreskul.context.annotations.Primary;
import io.github.bobocodebreskul.context.annotations.Scope;
import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.config.ConfigurationBeanDefinition;
import io.github.bobocodebreskul.context.exception.BeanDefinitionCreationException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnnotatedBeanDefinitionReaderTest {

  @Spy
  private BeanDefinitionRegistry registry;
  @InjectMocks
  private BeanDefinitionReader annotatedBeanDefinitionReader;

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
    verify(registry, atMostOnce()).registerBeanDefinition(anyString(), any());
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    assertThat(nameCaptor.getValue()).isEqualTo(beanClass.getName());
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(beanClass);
    assertThat(actualBeanDefinition.getDependencies()).isEmpty();
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
    verify(registry, times(2)).registerBeanDefinition(nameCaptor.capture(),
        definitionCaptor.capture());
    assertThat(nameCaptor.getValue()).isEqualTo(
        StringUtils.uncapitalize(beanClassWithDependency.getName()));
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(beanClassWithDependency);
    assertThat(actualBeanDefinition.getDependencies()).isNotEmpty();
    assertThat(actualBeanDefinition.getDependencies().stream().map(BeanDependency::type).toArray())
        .containsExactlyInAnyOrder(MyComponent.class);
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }

  @Test
  @DisplayName("Test bean definition isPrimary property marked as true")
  @Order(4)
  void given_PrimaryBeanClass_When_RegisterBean_Then_BeanDefinitionPrimaryPropertyTrue() {
    //given
    var primaryBeanClass = PrimaryComponent.class;
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    //when
    annotatedBeanDefinitionReader.register(primaryBeanClass);

    //then
    verify(registry, atMostOnce()).registerBeanDefinition(anyString(),
        definitionCaptor.capture());
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(primaryBeanClass);
    assertThat(actualBeanDefinition.getDependencies()).isEmpty();
    assertThat(actualBeanDefinition.isPrimary()).isTrue();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }
  @Test
  @DisplayName("Test bean name gets from BringComponent annotation.")
  @Order(5)
  void given_ClassWithNamedComponentAnnotation_When_RegisterBean_Then_BeanDefinitionCreatedWithCorrectName() {
    // data
    var beanClass = NamedComponent1.class;
    var beanName = "singleName";
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    //when
    annotatedBeanDefinitionReader.registerBean(beanClass);

    //then
    verify(registry, atMostOnce()).registerBeanDefinition(anyString(), any());
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    assertThat(nameCaptor.getValue()).isEqualTo(beanName);
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(beanClass);
    assertThat(actualBeanDefinition.getName()).isEqualTo(beanName);
    assertThat(actualBeanDefinition.getDependencies()).isEmpty();
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
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

  @Test
  @DisplayName("Test bean name gets from TestAnnotation with BringComponent annotation.")
  @Order(6)
  void given_ClassWithNamedComponentAnnotationAsParent_When_RegisterBean_Then_BeanDefinitionCreatedWithCorrectName() {
    // data
    var beanClass = NamedComponent2.class;
    var beanName = "hasParenComponent";
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    //when
    annotatedBeanDefinitionReader.registerBean(beanClass);

    //then
    verify(registry, atMostOnce()).registerBeanDefinition(anyString(), any());
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    assertThat(nameCaptor.getValue()).isEqualTo(StringUtils.uncapitalize(beanName));
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(beanClass);
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }

  @Test
  @DisplayName("Test bean name gets from TestAnnotation with BringComponent annotation.")
  @Order(7)
  void given_TwoComponentAnnotationsWithTheSameName_When_RegisterBean_Then_BeanDefinitionCreatedWithSetName() {
    // data
    var beanClass = TwoComponentAnnotationBean.class;
    var beanName = "similarName";
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    //when
    annotatedBeanDefinitionReader.registerBean(beanClass);

    //then
    verify(registry, atMostOnce()).registerBeanDefinition(anyString(), any());
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    assertThat(nameCaptor.getValue()).isEqualTo(StringUtils.uncapitalize(beanName));
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(beanClass);
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }

  @Test
  @DisplayName("Register Bean from configuration class")
  @Order(8)
  public void given_ConfigurationClassWithOneBringBeanMethod_when_RegisterBringBean_then_ReturnBuiltBeanDefinition() {
    Class<?> configurationClass = Config1.class;

    annotatedBeanDefinitionReader.registerBean(configurationClass);

    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    verify(registry, atMostOnce()).registerBeanDefinition(anyString(), any());
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(ConfigurationBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(configurationClass.getMethods()[0].getReturnType());
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
    assertThat(actualBeanDefinition.getName()).isEqualTo(configurationClass.getMethods()[0].getName());
  }

  @Test
  @DisplayName("Register Primary Bean from configuration class")
  @Order(8)
  public void given_ConfigurationClassWithOneBringBeanMethod_when_RegisterBringBeanAndItPrimary_then_ReturnBuiltBeanDefinition() {
    Class<?> configurationClass = Config2.class;

    annotatedBeanDefinitionReader.registerBean(configurationClass);

    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    verify(registry, atMostOnce()).registerBeanDefinition(anyString(), any());
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(ConfigurationBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(configurationClass.getMethods()[0].getReturnType());
    assertThat(actualBeanDefinition.isPrimary()).isTrue();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
    assertThat(actualBeanDefinition.getName()).isEqualTo(configurationClass.getMethods()[0].getName());
  }

  @Test
  @DisplayName("Register bean from configuration class do not have default constructor then throw error")
  @Order(9)
  public void given_ConfigurationClassWithOneBringBeanMethod_when_ConfigurationClassWithOutDefaultConstructor_then_ThrowException() {
    Class<?> configurationClass = Config3.class;

    assertThatThrownBy(() -> annotatedBeanDefinitionReader.registerBean(configurationClass))
        .isInstanceOf(BeanDefinitionCreationException.class)
        .hasMessage("Default constructor invoke for configuration fails: %s".formatted(configurationClass));
  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @BringComponent
  public @interface AnnotationWithComponent {

    String value() default "";
  }

  @BringComponent()
  static class MyComponent {

  }

  @BringComponent("singleName")
  static class NamedComponent1 {

  }

  @BringComponent("similarName")
  @AnnotationWithComponent("similarName")
  static class TwoComponentAnnotationBean {

  }

  @AnnotationWithComponent("hasParenComponent")
  static class NamedComponent2 {

  }

  static class AnotherComponent {

    private MyComponent myComponent;

    public AnotherComponent(MyComponent myComponent) {
      this.myComponent = myComponent;
    }
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

  @BringConfiguration
  public static class Config1 {

    @BringBean
    public String bean() {
      return "hello";
    }
  }

  @BringConfiguration
  public static class Config2 {

    @BringBean
    @Primary
    public String bean() {
      return "hello";
    }
  }

  @BringConfiguration
  public static class Config3 {

    public Config3(String s) {
    }

    @BringBean
    public String bean() {
      return "hello";
    }
  }
}
