package io.github.bobocodebreskul.context.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReflectionUtilsTest {

  @Test
  @DisplayName("Return true when the annotation is present for class")
  @Order(1)
  void given_ClassWithAnnotation_When_isAnnotationPresentForClass_Then_ReturnTrue() {
    //given
    var clazz = MyClass.class;
    var annotation = MyAnnotation.class;

    //when
    boolean result = ReflectionUtils.isAnnotationPresentForClass(annotation, clazz);

    //then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Return false when the annotation is absent for class")
  @Order(2)
  void given_ClassWithoutAnnotation_When_isAnnotationPresentForClass_Then_ReturnFalse() {
    //given
    var clazz = NoAnnotationClass.class;
    var annotation = MyAnnotation.class;

    //when
    boolean result = ReflectionUtils.isAnnotationPresentForClass(annotation,
        clazz);

    //then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("Throw exception when the annotation is null")
  @Order(3)
  void given_NullableAnnotation_When_isAnnotationPresentForClass_Then_ThrowException() {
    //given
    //when
    //then
    assertThatThrownBy(
        () -> ReflectionUtils.isAnnotationPresentForClass(null, MyClass.class))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("The annotation parameter cannot be null!");
  }

  @Test
  @DisplayName("Throw exception when the class is null")
  @Order(4)
  void given_NullableClass_When_isAnnotationPresentForClass_Then_ThrowException() {
    //given
    //when
    //then
    assertThatThrownBy(
        () -> ReflectionUtils.isAnnotationPresentForClass(MyAnnotation.class, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("The class parameter cannot be null!");
  }

  @Test
  @DisplayName("Return true when the annotation is present for constructor")
  @Order(5)
  void given_ConstructorWithAnnotation_When_isAnnotationPresentForConstructor_Then_ReturnTrue() {
    //given
    var constructor = MyClass.class.getDeclaredConstructors()[0];
    var annotation = MyAnnotation.class;

    //when
    boolean result = ReflectionUtils.isAnnotationPresentForConstructor(annotation, constructor);

    //then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Return false when the annotation is absent for constructor")
  @Order(6)
  void given_ConstructorWithoutAnnotation_When_isAnnotationPresentForConstructor_Then_ReturnFalse() {
    //given
    var constructor = NoAnnotationClass.class.getDeclaredConstructors()[0];
    var annotation = MyAnnotation.class;

    //when
    boolean result = ReflectionUtils.isAnnotationPresentForConstructor(annotation, constructor);

    //then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("Throw exception when the annotation is null")
  @Order(7)
  void given_NullableAnnotation_When_isAnnotationPresentForConstructor_Then_ThrowException() {
    //given
    //when
    //then
    assertThatThrownBy(
        () -> ReflectionUtils.isAnnotationPresentForConstructor(null,
            MyClass.class.getDeclaredConstructors()[0]))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("The annotation parameter cannot be null!");
  }

  @Test
  @DisplayName("Throw exception when the constructor is null")
  @Order(8)
  void given_NullableClass_When_isAnnotationPresentForConstructor_Then_ThrowException() {
    //given
    //when
    //then
    assertThatThrownBy(
        () -> ReflectionUtils.isAnnotationPresentForConstructor(MyAnnotation.class, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("The constructor parameter cannot be null!");
  }

  @Test
  @DisplayName("Return 2 constructors when multiple constructors and only 2 contains annotation")
  @Order(9)
  void given_ClassWithMultipleConstructorsAnd2AnnotatedConstructors_When_getConstructorsAnnotatedWith_ThenReturn2Constructor() {
    //given
    var constructors = MultipleConstructorMultipleAnnotationClass.class.getDeclaredConstructors();
    var annotation = MyAnnotation.class;

    //when
    var result = ReflectionUtils.getConstructorsAnnotatedWith(annotation, constructors);

    //then
    var expectedResult = Arrays.stream(constructors)
        .filter(constructor -> constructor.isAnnotationPresent(MyAnnotation.class))
        .toArray(Constructor[]::new);

    assertThat(result.size()).isNotEqualTo(constructors.length);
    assertThat(result)
        .hasSize(2)
        .containsExactlyInAnyOrder(expectedResult);
  }

  @Test
  @DisplayName("Return empty result when multiple constructors but none with annotation")
  @Order(10)
  void given_ClassWithMultipleConstructorsAndNoAnnotation_When_getConstructorsAnnotatedWith_Then_ReturnEmptyList() {
    //given
    var constructors = NoConstructorAnnotationClass.class.getDeclaredConstructors();
    var annotation = MyAnnotation.class;

    //when
    var result = ReflectionUtils.getConstructorsAnnotatedWith(annotation, constructors);

    //then
    assertThat(result.size())
        .isZero()
        .isNotEqualTo(constructors.length);
  }

  @Test
  @DisplayName("Return single default/no parameter constructor when class without declared constructors")
  @Order(11)
  void given_ClassWithoutDeclaredConstructors_When_GetDefaultConstructor_Then_ReturnSingleConstructor() {
    // given
    var availableConstructors = NoAnnotationClass.class.getDeclaredConstructors();
    // when
    var actualConstructor = ReflectionUtils.getDefaultConstructor(NoAnnotationClass.class);
    // then
    assertThat(availableConstructors).hasSize(1);
    assertThat(actualConstructor).isEqualTo(availableConstructors[0]);
  }

  @Test
  @DisplayName("Return single default/no parameter constructor when class with multiple declared constructors including one without parameters")
  @Order(12)
  @SneakyThrows
  void given_ClassWithMultiConstructorIncludingNoParamOne_When_GetDefaultConstructor_Then_ReturnSingleConstructor() {
    // given
    var availableConstructors = MultipleConstructorWithDefaultClass.class.getDeclaredConstructors();
    var expectedConstructor = MultipleConstructorWithDefaultClass.class.getDeclaredConstructor();
    // when
    var actualConstructor = ReflectionUtils.getDefaultConstructor(MultipleConstructorWithDefaultClass.class);
    // verify
    assertThat(availableConstructors).hasSizeGreaterThan(1);
    assertThat(actualConstructor).isEqualTo(expectedConstructor);
  }


  @Test
  @DisplayName("Throw exception when class with multiple constructors and no default constructor")
  @Order(13)
  void given_ClassWithMultipleConstructorsAndNoDefaultConstructor_When_getDefaultConstructor_Then_ThrowException(){
    //given
    var clazz = MultipleConstructorClass.class;

    //when
    //then
   assertThatThrownBy(() -> ReflectionUtils.getDefaultConstructor(clazz))
       .isInstanceOf(IllegalStateException.class)
       .hasMessage("Not found a default constructor for class [%s]".formatted(clazz.getName()));
  }

  @Test
  @DisplayName("Throw exception when nullable class")
  @Order(14)
  void given_NullableClass_When_getDefaultConstructor_Then_ThrowException() {
    //given
    //when
    //then
    assertThatThrownBy(
        () -> ReflectionUtils.getDefaultConstructor(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("The class parameter cannot be null!");
  }

  @Test
  @DisplayName("Return true when class without declared constructors")
  @Order(15)
  void given_ClassWithoutDeclaredConstructors_When_hasDefaultConstructor_Then_ReturnTrue() {
    //given
    var clazz = NoAnnotationClass.class;

    //when
    boolean result = ReflectionUtils.hasDefaultConstructor(clazz);

    //then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Return true when class with multiple constructors and explicitly declared default constructor")
  @Order(16)
  void given_ClassWithMultipleConstructorsAndDefaultConstructor_When_hasDefaultConstructor_Then_ReturnTrue() {
    //given
    var clazz = MultipleConstructorMultipleAnnotationClass.class;

    //when
    boolean result = ReflectionUtils.hasDefaultConstructor(clazz);

    //then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Return false when class with multiple constructors and no default constructor")
  @Order(17)
  void given_ClassWithMultipleConstructorsAndNoDefaultConstructor_When_hasDefaultConstructor_Then_ReturnFalse() {
    //given
    var clazz = MultipleConstructorClass.class;

    //when
    boolean result = ReflectionUtils.hasDefaultConstructor(clazz);

    //then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("Throw exception when nullable class")
  @Order(18)
  void given_NullableClass_When_hasDefaultConstructor_Then_ThrowException() {
    //given
    //when
    //then
    assertThatThrownBy(
        () -> ReflectionUtils.hasDefaultConstructor(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("The class parameter cannot be null!");
  }


  @MyAnnotation
  static class MyClass {

    @MyAnnotation
    public MyClass() {
    }
  }

  static class NoAnnotationClass {

  }

  static class MultipleConstructorClass {

    private NoAnnotationClass noAnnotationClass;
    private MyClass myClass;


    public MultipleConstructorClass(NoAnnotationClass noAnnotationClass) {
      this.noAnnotationClass = noAnnotationClass;
    }

    @MyAnnotation
    public MultipleConstructorClass(MyClass myClass) {
      this.myClass = myClass;
    }

    public MultipleConstructorClass(NoAnnotationClass noAnnotationClass, MyClass myClass) {
      this.noAnnotationClass = noAnnotationClass;
      this.myClass = myClass;
    }

  }

  static class NoConstructorAnnotationClass {

    private NoAnnotationClass noAnnotationClass;
    private MyClass myClass;

    public NoConstructorAnnotationClass(NoAnnotationClass noAnnotationClass) {
      this.noAnnotationClass = noAnnotationClass;
    }

    public NoConstructorAnnotationClass(MyClass myClass) {
      this.myClass = myClass;
    }

    public NoConstructorAnnotationClass(NoAnnotationClass noAnnotationClass, MyClass myClass) {
      this.noAnnotationClass = noAnnotationClass;
      this.myClass = myClass;
    }

  }

  static class MultipleConstructorMultipleAnnotationClass {

    private NoAnnotationClass noAnnotationClass;
    private MyClass myClass;

    public MultipleConstructorMultipleAnnotationClass() {
    }

    public MultipleConstructorMultipleAnnotationClass(NoAnnotationClass noAnnotationClass) {
      this.noAnnotationClass = noAnnotationClass;
    }

    @MyAnnotation
    public MultipleConstructorMultipleAnnotationClass(MyClass myClass) {
      this.myClass = myClass;
    }

    @MyAnnotation
    public MultipleConstructorMultipleAnnotationClass(NoAnnotationClass noAnnotationClass,
        MyClass myClass) {
      this.noAnnotationClass = noAnnotationClass;
      this.myClass = myClass;
    }

  }

  static class MultipleConstructorWithDefaultClass {

    private NoAnnotationClass noAnnotationClass;
    private MyClass myClass;

    public MultipleConstructorWithDefaultClass() {}

    public MultipleConstructorWithDefaultClass(NoAnnotationClass noAnnotationClass) {
      this.noAnnotationClass = noAnnotationClass;
    }

    public MultipleConstructorWithDefaultClass(NoAnnotationClass noAnnotationClass, MyClass myClass) {
      this.noAnnotationClass = noAnnotationClass;
      this.myClass = myClass;
    }
  }

  @Target({ElementType.TYPE, ElementType.CONSTRUCTOR})
  @Retention(RetentionPolicy.RUNTIME)
  @interface MyAnnotation {

  }
}
