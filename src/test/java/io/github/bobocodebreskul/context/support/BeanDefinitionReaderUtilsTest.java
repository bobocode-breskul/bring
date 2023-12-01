package io.github.bobocodebreskul.context.support;

import static io.github.bobocodebreskul.context.support.BeanDefinitionReaderUtils.UNCERTAIN_BEAN_NAME_EXCEPTION_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.github.bobocodebreskul.context.annotations.Autowired;
import io.github.bobocodebreskul.context.annotations.BringBean;
import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.Qualifier;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.BeanDefinitionCreationException;
import io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
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

@SuppressWarnings({"UnusedDeclaration"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class BeanDefinitionReaderUtilsTest {

  @Mock
  private BeanDefinitionRegistry registry;


  @Test
  @DisplayName("Throw NullPointerException when provided class type is null")
  @Order(1)
  void given_NullClassType_When_GetBeanName_Then_ThrowNullPointerException() {
    // when
    // then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.getBeanName(null))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("Bean class has not been specified");
  }


  @Test
  @DisplayName("Get bean name based on bean class when no bean name specified")
  @Order(2)
  void given_BeanClassWithoutBeanNameSpecified_When_GetBeanName_Then_ReturnBeanClassName() {
    //given
    var beanClass = MyComponent.class;
    var expectedBeanName = beanClass.getName();

    //when
    var actualBeanName = BeanDefinitionReaderUtils.getBeanName(beanClass);

    //then
    assertThat(actualBeanName).isEqualTo(expectedBeanName);
  }

  @Test
  @DisplayName("Get bean name from annotation when bean name specified")
  @Order(3)
  void given_BeanClassWithSpecifiedBeanName_When_GetBeanName_Then_ReturnSpecifiedBeanName(){
    //given
    var beanClass = NamedComponent.class;
    var expectedBeanName = "singleName";

    //when
    var result = BeanDefinitionReaderUtils.getBeanName(beanClass);

    //then
    assertThat(result).isEqualTo(expectedBeanName);
  }


  @Test
  @DisplayName("Class have two component annotations with similar names then return name")
  @Order(4)
  void given_BeanClassWithTwoSimilarBeanNameDeclaration_When_GetBeanName_Then_ReturnValidBeanName() {
    //given
    var beanClass = TwoComponentAnnotationBean.class;
    String expectedName = "similarName";

    //when
    var actualBeanName = BeanDefinitionReaderUtils.getBeanName(beanClass);

    //then
    assertThat(actualBeanName).isEqualTo(expectedName);
  }

  @Test
  @DisplayName("Class have two component annotations with different names then throw exception")
  @Order(5)
  void given_BeanClassWithTwoDifferentBeanNameDeclaration_When_GetBeanName_Then_ThrowException() {
    //given
    var beanClass = UncertainNameComponent.class;

    var expectedMessage = UNCERTAIN_BEAN_NAME_EXCEPTION_MSG
      .formatted(beanClass.getName(), "firstName, secondName");

    //then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.getBeanName(beanClass))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage(expectedMessage);
  }

  @Test
  @DisplayName("Get bean dependencies from the default constructor")
  @Order(6)
  void given_BeanClassWithDefaultConstructor_When_GetConstructorBeanDependencies_Then_ReturnEmptyList() {
    //given
    var beanDefaultConstructor = MyComponent.class.getDeclaredConstructors()[0];

    //when
    var dependencies =
        BeanDefinitionReaderUtils.getBeanMethodDependencies(beanDefaultConstructor);

    //then
    assertThat(dependencies).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("getConstructors")
  @DisplayName("Get bean dependencies for constructor with n parameters")
  @Order(7)
  void given_ConstructorsWithNArguments_When_GetConstructorBeanDependencies_Then_ReturnAllConstructorArgumentTypes(
      Constructor<?> constructor, int result) {
   // when
    var dependencies = BeanDefinitionReaderUtils.getBeanMethodDependencies(constructor);

    //then
    var expectedTypes = constructor.getParameterTypes();
    assertThat(dependencies.stream().map(BeanDependency::type).toArray())
        .hasSize(result)
        .containsExactlyInAnyOrder(expectedTypes);
  }

  @Test
  @DisplayName("Get bean dependencies for constructor parameters with qualifiers annotations")
  @Order(8)
  void given_BeanClassWithConstructorWithQualifiers_When_GetConstructorBeanDependencies_Then_ReturnDependenciesWithQualifiers() {
    //given
    String firstQualifierName = "first";
    String secondQualifierName = "second";
    var beanDefaultConstructor = ComponentWithQualifiedConstructor.class.getDeclaredConstructors()[0];

    //when
    var dependencies =
      BeanDefinitionReaderUtils.getBeanMethodDependencies(beanDefaultConstructor);

    //then
    assertThat(dependencies).hasSize(3);
    assertThat(dependencies.get(0).qualifier()).isEqualTo(firstQualifierName);
    assertThat(dependencies.get(2).qualifier()).isEqualTo(secondQualifierName);
  }


  @Test
  @DisplayName("Throw NullPointerException with meaningful description for nullable constructor")
  @Order(9)
  void given_NullableConstructor_When_GetConstructorBeanDependencies_Then_ThrowNullPointerException() {
    // when
    // then
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.getBeanMethodDependencies(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Bean method/constructor has not been specified");
  }

  @Test
  @DisplayName("Get bean constructor when bean class has only default constructor")
  @Order(11)
  void given_BeanClassWithSingleDefaultConstructor_When_FindBeanInitConstructor_Then_ReturnDefaultConstructor() {
    //given
    var beanClass = MyComponent.class;
    var beanName = BeanDefinitionReaderUtils.getBeanName(beanClass);

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
    var beanName = BeanDefinitionReaderUtils.getBeanName(beanClass);

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
    var beanName = BeanDefinitionReaderUtils.getBeanName(beanClass);

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
    var beanName = BeanDefinitionReaderUtils.getBeanName(beanClass);

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
    var beanName = BeanDefinitionReaderUtils.getBeanName(beanClass);
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
    var beanName = BeanDefinitionReaderUtils.getBeanName(beanClass);

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
  @DisplayName("Get List of methods which annotated with @BringBean from class")
  @Order(20)
  void given_BeanClassWithBeanMethods_When_TwoMethodsAnnotatedAsBringBean_Then_ReturnListOfMethods() {
    List<Method> beanMethods = BeanDefinitionReaderUtils.getBeanMethods(Config1.class);

    assertThat(beanMethods.size()).isEqualTo(2);
  }

  @Test
  @DisplayName("Get List of methods which annotated with @BringBean from class when some not annotated")
  @Order(21)
  void given_BeanClassWithBeanMethods_When_OneMethodsAnnotatedAsBringBeanAndOneNot_Then_ReturnListOfMethods() {
    List<Method> beanMethods = BeanDefinitionReaderUtils.getBeanMethods(Config2.class);

    assertThat(beanMethods.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Get List of methods which annotated with @BringBean but class null")
  @Order(22)
  void when_ClassNull_Then_ThrowException() {
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.getBeanMethods(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("Get Method name but method null")
  @Order(23)
  void given_Method_When_MethodNull_Then_ThrowException() {
    assertThatThrownBy(() -> BeanDefinitionReaderUtils.getMethodBeanName(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("Get Method name")
  @Order(23)
  void given_Method_When_MethodNotNull_Then_ReturnName() {
    Method method = Config1.class.getMethods()[0];

    String methodBeanName = BeanDefinitionReaderUtils.getMethodBeanName(method);

    assertThat(methodBeanName).isEqualTo(method.getName());
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
  static class ComponentWithQualifiedConstructor {
    QualifiedComponent component1;
    String string;

    public ComponentWithQualifiedConstructor(@Qualifier("first") QualifiedComponent component1,
      String string, @Qualifier("second") QualifiedComponent component2) {
      this.component1 = component1;
      this.string = string;
      this.component2 = component2;
    }

    QualifiedComponent component2;

  }

  static class QualifiedComponent {

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

  @BringComponent("singleName")
  static class NamedComponent {

  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @BringComponent
  public @interface AnnotationWithComponent {

    String value() default "";
  }
  @BringComponent("firstName")
  @AnnotationWithComponent("secondName")
  static class UncertainNameComponent {

  }

  @BringComponent("similarName")
  @AnnotationWithComponent("similarName")
  static class TwoComponentAnnotationBean {

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

  static class Config1 {

    @BringBean
    public void bean1() {

    }

    @BringBean
    public void bean2() {

    }
  }

  static class Config2 {

    @BringBean
    public void bean1() {

    }

    public void bean2() {

    }
  }
}
