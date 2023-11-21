package io.github.bobocodebreskul.context.registry;

import static io.github.bobocodebreskul.context.registry.AnnotatedBeanDefinitionReader.UNCERTAIN_BEAN_NAME_EXCEPTION_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.Primary;
import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.exception.DuplicateBeanDefinitionException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
    assertThat(nameCaptor.getValue()).isEqualTo(StringUtils.uncapitalize(beanClass.getSimpleName()));
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
    assertThat(nameCaptor.getValue()).isEqualTo(StringUtils.uncapitalize(beanClass.getSimpleName()));
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
    // data
    var primaryBeanClass = PrimaryComponent.class;
    // given
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

  @Test
  @DisplayName("Test bean name gets from BringComponent annotation.")
  @Order(7)
  void given_ClassWithNamedComponentAnnotation_When_RegisterBean_Then_BeanDefinitionCreatedWithCorrectName() {
    // data
    var beanClass = NamedComponent1.class;
    var beanName = "singleName";
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    // given
    given(registry.isBeanNameInUse(beanName)).willReturn(false);

    //when
    annotatedBeanDefinitionReader.registerBean(beanClass);

    //then
    verify(registry, atLeastOnce()).isBeanNameInUse(anyString());
    verify(registry, atMostOnce()).registerBeanDefinition(anyString(), any());
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    assertThat(nameCaptor.getValue()).isEqualTo(StringUtils.uncapitalize(beanName));
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(beanClass);
    assertThat(actualBeanDefinition.getDependsOn()).isEmpty();
    assertThat(actualBeanDefinition.isAutowireCandidate()).isFalse();
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }

  @Test
  @DisplayName("Test bean name gets from TestAnnotation with BringComponent annotation.")
  @Order(8)
  void given_ClassWithNamedComponentAnnotationAsParent_When_RegisterBean_Then_BeanDefinitionCreatedWithCorrectName() {
    // data
    var beanClass = NamedComponent2.class;
    var beanName = "hasParenComponent";
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    // given
    given(registry.isBeanNameInUse(beanName)).willReturn(false);

    //when
    annotatedBeanDefinitionReader.registerBean(beanClass);

    //then
    verify(registry, atLeastOnce()).isBeanNameInUse(anyString());
    verify(registry, atMostOnce()).registerBeanDefinition(anyString(), any());
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    assertThat(nameCaptor.getValue()).isEqualTo(StringUtils.uncapitalize(beanName));
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(beanClass);
    assertThat(actualBeanDefinition.getDependsOn()).isEmpty();
    assertThat(actualBeanDefinition.isAutowireCandidate()).isFalse();
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
  }

  @Test
  @DisplayName("When class have two component annotation with different names then throw exception.")
  @Order(9)
  void given_TwoComponentAnnotationsWithDifferentNames_When_RegisterBean_Then_ThrowException() {
    // data
    var beanClass = UncertainNameComponent.class;
    var expectedMessage = UNCERTAIN_BEAN_NAME_EXCEPTION_MSG
      .formatted(beanClass.getName(), "firstName, secondName");

    // given

    //when
    Exception actualException = catchException(
      () -> annotatedBeanDefinitionReader.registerBean(beanClass));

    //then
    assertThat(actualException)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage(expectedMessage);
    then(registry).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("Test bean name gets from TestAnnotation with BringComponent annotation.")
  @Order(10)
  void given_TwoComponentAnnotationsWithTheSameName_When_RegisterBean_Then_BeanDefinitionCreatedWithSetName() {
    // data
    var beanClass = TwoComponentAnnotationBean.class;
    var beanName = "similarName";
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BeanDefinition> definitionCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

    // given
    given(registry.isBeanNameInUse(beanName)).willReturn(false);

    //when
    annotatedBeanDefinitionReader.registerBean(beanClass);

    //then
    verify(registry, atLeastOnce()).isBeanNameInUse(anyString());
    verify(registry, atMostOnce()).registerBeanDefinition(anyString(), any());
    verify(registry).registerBeanDefinition(nameCaptor.capture(), definitionCaptor.capture());
    assertThat(nameCaptor.getValue()).isEqualTo(StringUtils.uncapitalize(beanName));
    BeanDefinition actualBeanDefinition = definitionCaptor.getValue();
    assertThat(actualBeanDefinition).isInstanceOf(AnnotatedGenericBeanDefinition.class);
    assertThat(actualBeanDefinition.getBeanClass()).isEqualTo(beanClass);
    assertThat(actualBeanDefinition.getDependsOn()).isEmpty();
    assertThat(actualBeanDefinition.isAutowireCandidate()).isFalse();
    assertThat(actualBeanDefinition.isPrimary()).isFalse();
    assertThat(actualBeanDefinition.isSingleton()).isTrue();
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


  @BringComponent("firstName")
  @AnnotationWithComponent("secondName")
  static class UncertainNameComponent {

  }

  @BringComponent("similarName")
  @AnnotationWithComponent("similarName")
  static class TwoComponentAnnotationBean {

  }

  static class AnotherComponent {

    @Autowired
    private MyComponent myComponent;
  }

  @AnnotationWithComponent("hasParenComponent")
  static class NamedComponent2 {

  }

  @Primary
  @BringComponent
  static class PrimaryComponent {

  }
}
