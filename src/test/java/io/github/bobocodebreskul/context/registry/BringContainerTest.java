package io.github.bobocodebreskul.context.registry;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.bobocodebreskul.context.annotations.Scope;
import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.InstanceCreationException;
import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;
import io.github.bobocodebreskul.context.support.ReflectionUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BringContainerTest {

  private static final String TEST_BEAN_NAME_1 = "TEST_BEAN_NAME_1";
  private static final String TEST_BEAN_NAME_2 = "TEST_BEAN_NAME_2";
  private static final String TEST_BEAN_NAME_3 = "TEST_BEAN_NAME_3";
  public static final String TEST_BEAN_NAME_4 = "TEST_BEAN_NAME_4";
  public static final String TEST_BEAN_NAME_5 = "TEST_BEAN_NAME_5";

  @InjectMocks
  private BringContainer objectFactory;

  @Mock
  private BeanDefinitionRegistry beanDefinitionRegistry;

  @Test
  @DisplayName("Create single bean by bean name")
  @Order(1)
  void given_BeanName_When_BeanIsRegistered_Then_ReturnBean() {
    // data
    String inputBeanName = TEST_BEAN_NAME_1;
    // given
    BeanClass1 expectedBean = new BeanClass1();
    AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(
        BeanClass1.class);
    beanDefinition.setName(inputBeanName);
    beanDefinition.setInitConstructor(ReflectionUtils.getDefaultConstructor(BeanClass1.class));

    given(beanDefinitionRegistry.getBeanDefinition(inputBeanName)).willReturn(beanDefinition);
    // when
    Object actualBean = objectFactory.getBean(inputBeanName);
    // then
    assertThat(actualBean).isInstanceOf(BeanClass1.class);
    assertThat(actualBean)
        .usingRecursiveComparison()
        .isEqualTo(expectedBean);
  }

  @Test
  @DisplayName("Create and return same bean when called twice by same bean name")
  @Order(2)
  void given_BeanName_When_BeanIsRegistered_Then_OnSecondTimeReturnSameBeanAgain() {
    // data
    String inputBeanName = TEST_BEAN_NAME_1;
    // given
    AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(
        BeanClass1.class);
    beanDefinition.setName(inputBeanName);
    beanDefinition.setInitConstructor(ReflectionUtils.getDefaultConstructor(BeanClass1.class));


    given(beanDefinitionRegistry.getBeanDefinition(inputBeanName)).willReturn(beanDefinition);
    // when
    Object actualBean1 = objectFactory.getBean(inputBeanName);
    Object actualBean2 = objectFactory.getBean(inputBeanName);
    // then
    assertThat(actualBean1).isSameAs(actualBean2);
    verify(beanDefinitionRegistry, times(1)).getBeanDefinition(inputBeanName);
  }

  @Test
  @DisplayName("Throw NoSuchBeanDefinitionException when take bean without bean definition by name")
  @Order(3)
  void given_BeanName_When_BeanNameNotRegistered_Then_ThrowNoSuchBeanDefinitionException() {
    assertThatThrownBy(() -> objectFactory.getBean(TEST_BEAN_NAME_1))
        .isInstanceOf(NoSuchBeanDefinitionException.class)
        .hasMessage(
            "BeanDefinition for bean with name %s is not found! Check configuration and register this bean",
            TEST_BEAN_NAME_1);
  }

  @Test
  @DisplayName("Create bean by bean name with 2 level depth dependency")
  @Order(4)
  void given_BeanNameForClassWithDependencyThatHasDependency_When_GetBeanByName_Then_ReturnBean() {
    // data
    String inputBeanName = TEST_BEAN_NAME_3;
    // given
    AnnotatedGenericBeanDefinition beanDefinition3 = new AnnotatedGenericBeanDefinition(
        BeanClass3.class);
    beanDefinition3.setName(inputBeanName);
    beanDefinition3.setDependencies(List.of(
        new BeanDependency(TEST_BEAN_NAME_2, BeanClass2.class),
        new BeanDependency(TEST_BEAN_NAME_1, BeanClass1.class)
    ));
    beanDefinition3.setInitConstructor(BeanClass3.class.getDeclaredConstructors()[0]);
    AnnotatedGenericBeanDefinition beanDefinition2 = new AnnotatedGenericBeanDefinition(
        BeanClass2.class);
    beanDefinition2.setName(TEST_BEAN_NAME_2);
    beanDefinition2.setDependencies(List.of(
        new BeanDependency(TEST_BEAN_NAME_1, BeanClass1.class),
        new BeanDependency(TEST_BEAN_NAME_4, BeanClass4.class)
    ));
    beanDefinition2.setInitConstructor(BeanClass2.class.getDeclaredConstructors()[0]);
    AnnotatedGenericBeanDefinition beanDefinition1 = new AnnotatedGenericBeanDefinition(
        BeanClass1.class);
    beanDefinition1.setName(TEST_BEAN_NAME_1);
    beanDefinition1.setInitConstructor(BeanClass1.class.getDeclaredConstructors()[0]);
    AnnotatedGenericBeanDefinition beanDefinition4 = new AnnotatedGenericBeanDefinition(
        BeanClass4.class);
    beanDefinition4.setName(TEST_BEAN_NAME_4);
    beanDefinition4.setInitConstructor(BeanClass4.class.getDeclaredConstructors()[0]);

    given(beanDefinitionRegistry.getBeanDefinition(inputBeanName)).willReturn(beanDefinition3);
    given(beanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME_2)).willReturn(beanDefinition2);
    given(beanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME_1)).willReturn(beanDefinition1);
    given(beanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME_4)).willReturn(beanDefinition4);
    // when
    Object actualBean = objectFactory.getBean(inputBeanName);
    // then
    BeanClass3 expectedBean = new BeanClass3(new BeanClass2(new BeanClass1(), new BeanClass4()), new BeanClass1());
    assertThat(actualBean).isInstanceOf(BeanClass3.class);
    assertThat(actualBean)
        .usingRecursiveComparison()
        .isEqualTo(expectedBean);
  }


  @Test
  @DisplayName("Return bean by name with multi arguments constructor and simple dependencies")
  @Order(5)
  void given_BeanClassWithSingleMultiArgsConstructorAndSimpleDependencies_When_GetBeanByName_Then_ReturnBeanBySpecifiedName() {
    // data
    var inputBeanName = TEST_BEAN_NAME_2;
    //given
    var firstDependencyBeanName = TEST_BEAN_NAME_1;
    var firstDependencyBeanClass = BeanClass1.class;
    var firstDependencyBeanDefinition = new AnnotatedGenericBeanDefinition(firstDependencyBeanClass);
    firstDependencyBeanDefinition.setName(firstDependencyBeanName);
    firstDependencyBeanDefinition.setInitConstructor(firstDependencyBeanClass.getDeclaredConstructors()[0]);

    var secondDependencyBeanName = TEST_BEAN_NAME_4;
    var secondDependencyBeanClass = BeanClass4.class;
    var secondDependencyBeanDefinition = new AnnotatedGenericBeanDefinition(secondDependencyBeanClass);
    secondDependencyBeanDefinition.setName(secondDependencyBeanName);
    secondDependencyBeanDefinition.setInitConstructor(secondDependencyBeanClass.getDeclaredConstructors()[0]);

    var mainBeanDefinition = new AnnotatedGenericBeanDefinition(BeanClass2.class);
    mainBeanDefinition.setName(inputBeanName);
    mainBeanDefinition.setDependencies(
        List.of(new BeanDependency(firstDependencyBeanName, secondDependencyBeanClass),
            new BeanDependency(secondDependencyBeanName, secondDependencyBeanClass)));
    mainBeanDefinition.setInitConstructor(BeanClass2.class.getDeclaredConstructors()[0]);

    given(beanDefinitionRegistry.getBeanDefinition(inputBeanName)).willReturn(mainBeanDefinition);
    given(beanDefinitionRegistry.getBeanDefinition(firstDependencyBeanName)).willReturn(
        firstDependencyBeanDefinition);
    given(beanDefinitionRegistry.getBeanDefinition(secondDependencyBeanName)).willReturn(
        secondDependencyBeanDefinition);

    //when
    var actualBean = objectFactory.getBean(inputBeanName);

    //then
    var expectedBean = new BeanClass2(new BeanClass1(), new BeanClass4());
    assertThat(actualBean)
        .usingRecursiveComparison()
        .isEqualTo(expectedBean)
        .isInstanceOf(BeanClass2.class);
  }


  @Test
  @DisplayName("Throw NoSuchBeanDefinitionException when bean has dependency without bean definition")
  @Order(6)
  void given_BeanWithDependencyWithoutBeanDefinition_When_GetBeanByName_Then_ShouldTrowNoSuchBeanDefinitionException() {
    //given
    var beanDependency = new BeanDependency(TEST_BEAN_NAME_1, BeanClass2.class);

    var beanDefinition = new AnnotatedGenericBeanDefinition(BeanClass2.class);
    beanDefinition.setName(TEST_BEAN_NAME_2);
    beanDefinition.setDependencies(List.of(beanDependency));

    given(beanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME_2)).willReturn(beanDefinition);

    //when
    //then
    assertThatThrownBy(() -> objectFactory.getBean(TEST_BEAN_NAME_2))
        .isInstanceOf(NoSuchBeanDefinitionException.class)
        .hasMessage(
            "BeanDefinition for bean with name %s is not found! Check configuration and register this bean",
            TEST_BEAN_NAME_1);
  }

  @Test
  @DisplayName("Throw InstanceCreationException when bean instance could not be created")
  @Order(7)
  void given_BeanAbstractClass_When_GetBeanByName_Then_ShouldThrowInstanceCreationException() {
    // data
    String inputBeanName = TEST_BEAN_NAME_1;

    // given
    var beanDefinition = new AnnotatedGenericBeanDefinition(AbstractBeanClass.class);
    beanDefinition.setName(TEST_BEAN_NAME_1);
    beanDefinition.setInitConstructor(AbstractBeanClass.class.getDeclaredConstructors()[0]);

    
    given(beanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME_1)).willReturn(beanDefinition);

    // when
    // then
    assertThatThrownBy(() -> objectFactory.getBean(inputBeanName))
        .isInstanceOf(InstanceCreationException.class)
        .hasMessage("Could not create an instance of \"%s\" class!".formatted(inputBeanName));
  }

  @Test
  @DisplayName("Throw UnsupportedOperationException when take bean without bean definition by class")
  @Order(8)
  void given_BeanClass_When_BeanIsRegistered_Then_ThrowUnsupportedOperationException() {
    // when
    assertThatThrownBy(() -> objectFactory.getBean(Object.class))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  static class BeanClass1 {

  }

  @Test
  @DisplayName("Create prototype bean by bean name")
  @Order(9)
  void given_BeanName_When_BeanScopeIsPrototype_Then_BeanShouldNotBeenStored() {
    // data
    String inputBeanName = TEST_BEAN_NAME_5;
    // given
    BeanClass5 expectedBean = new BeanClass5();
    var beanDefinition = new AnnotatedGenericBeanDefinition(BeanClass5.class);
    beanDefinition.setName(inputBeanName);
    beanDefinition.setScope(BeanDefinition.PROTOTYPE_SCOPE);
    beanDefinition.setInitConstructor(ReflectionUtils.getDefaultConstructor(BeanClass5.class));

    given(beanDefinitionRegistry.getBeanDefinition(inputBeanName)).willReturn(beanDefinition);
    // when
    Object actualBean = objectFactory.getBean(inputBeanName);

    // then
    assertAll(
        () -> assertThat(objectFactory.getAllBeans()).isEmpty(),
        () -> assertThat(actualBean).isInstanceOf(BeanClass5.class),
        () -> assertThat(actualBean).usingRecursiveComparison().isEqualTo(expectedBean)
    );
  }

  @RequiredArgsConstructor
  static class BeanClass2 {
    private final BeanClass1 beanClass;
    private final BeanClass4 beanClass4;
  }

  @RequiredArgsConstructor
  static class BeanClass3 {
    private final BeanClass2 beanWithDependencies;
    private final BeanClass1 beanWithoutDependencies;
  }

  static class BeanClass4 {

  }

  static abstract class AbstractBeanClass {

  }

  @Scope(BeanDefinition.PROTOTYPE_SCOPE)
  static class BeanClass5 {

  }
}
