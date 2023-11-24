package io.github.bobocodebreskul.context.support;

import static io.github.bobocodebreskul.context.support.ReflectionUtils.ANNOTATION_VALUE_ERROR_MSG_PREFIX;
import static io.github.bobocodebreskul.context.support.ReflectionUtilsTest.TestEnum.SECOND_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.TestComponentAnnotation;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.none.NoneCandidate1;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.parent.ParentCandidate;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

  @Test
  @DisplayName("Check if annotation found in class")
  @Order(19)
  void given_ClassWithTargetAnnotation_When_checkIfClassHasAnnotationRecursively_ThenReturnTrue() {
    // data
    Class<?> classForCheck = ParentCandidate.class;
    Class<TestComponentAnnotation> searchedAnnotation = TestComponentAnnotation.class;
    // when
    var actualResult = ReflectionUtils.checkIfClassHasAnnotationRecursively(
      classForCheck, searchedAnnotation
    );
    // verify
    assertThat(actualResult).isTrue();
  }

  @Test
  @DisplayName("Check if annotation found in class recursively")
  @Order(20)
  void given_ClassWithTargetAnnotationAsParent_When_checkIfClassHasAnnotationRecursively_ThenReturnTrue() {
    // data
    Class<?> classForCheck = AnnotatedClass2.class;
    Class<AnnotationWithComponent> searchedAnnotation = AnnotationWithComponent.class;
    // when
    var actualResult = ReflectionUtils.checkIfClassHasAnnotationRecursively(
      classForCheck, searchedAnnotation
    );
    // verify
    assertThat(actualResult).isTrue();
  }

  @Test
  @DisplayName("Find nothing if class don't have searched annotation")
  @Order(21)
  void given_ClassWithoutTargetAnnotation_When_checkIfClassHasAnnotationRecursively_ThenReturnFalse() {
    // data
    Class<?> classForCheck = NoneCandidate1.class;
    Class<AnnotationWithComponent> searchedAnnotation = AnnotationWithComponent.class;
    // when
    var actualResult = ReflectionUtils.checkIfClassHasAnnotationRecursively(
      classForCheck, searchedAnnotation
    );
    // verify
    assertThat(actualResult).isFalse();
  }

  @ParameterizedTest
  @MethodSource("testAnnotationFieldArgs")
  @DisplayName("Successfully retrieve field values from class annotation")
  @Order(22)
  void given_ClassWithAnnotationAndFieldValue_When_GetClassAnnotationValue_Then_ReturnCorrectValue(
    String fieldName, Object expectedValue, Class<?> fieldType
  ) {
    // data
    Class<?> testedClass = AnnotatedClass.class;
    Class<TestAnnotation> annotationClass = TestAnnotation.class;

    // when
    var actualResult = ReflectionUtils.getClassAnnotationValue(testedClass, annotationClass,
      fieldName, fieldType);

    // then
    assertThat(actualResult).isEqualTo(expectedValue);
  }

  @Test
  @DisplayName("Successfully retrieve enum field value from class annotation")
  @Order(23)
  void given_AnnotationFieldTypeEnum_When_GetClassAnnotationValue_Then_ReturnCorrectValue() {
    // data
    Class<?> testedClass = AnnotatedClass.class;
    Class<TestAnnotation> annotationClass = TestAnnotation.class;

    // when
    var actualResult = ReflectionUtils.getClassAnnotationValue(testedClass, annotationClass,
      "enumValue", TestEnum.class);

    // then
    assertThat(actualResult).isEqualTo(SECOND_VALUE);
  }

  @Test
  @DisplayName("Try to get value by providing wrong field value type than throw an Exception.")
  @Order(24)
  void given_WrongFieldType_When_GetClassAnnotationValue_Then_ThrowException() {
    // data
    Class<?> testedClass = AnnotatedClass.class;
    Class<TestAnnotation> annotationClass = TestAnnotation.class;
    String fieldName = "value";

    // when
    Exception actualException = catchException(() ->
      ReflectionUtils.getClassAnnotationValue(testedClass, annotationClass, fieldName,
        Integer.class));

    // then
    assertThat(actualException).
      isInstanceOf(IllegalArgumentException.class)
      .hasMessage(ANNOTATION_VALUE_ERROR_MSG_PREFIX.formatted(annotationClass.getName(), fieldName,
        testedClass.getName()) + "Got unexpected value type.");
  }

  @Test
  @DisplayName("Try to get value of non existed field than throw an Exception.")
  @Order(25)
  void given_NonExistingField_When_GetClassAnnotationValue_Then_ThrowException() {
    // data
    Class<?> testedClass = AnnotatedClass.class;
    Class<TestAnnotation> annotationClass = TestAnnotation.class;
    String wrongField = "notExistedField";

    // when
    Exception actualException = catchException(() ->
      ReflectionUtils.getClassAnnotationValue(testedClass, annotationClass, wrongField,
        Object.class));

    // then
    assertThat(actualException).
      isInstanceOf(IllegalStateException.class)
      .hasMessage(ANNOTATION_VALUE_ERROR_MSG_PREFIX.formatted(annotationClass.getName(), wrongField,
        testedClass.getName()));
  }

  @Test
  @DisplayName("Try to get annotation value from class which don't have provided annotation than throw an Exception.")
  @Order(26)
  void given_ClassNotHaveAnnotation_When_GetClassAnnotationValue_Then_ThrowExceptions() {
    // data
    Class<?> testedClass = TestClassWithoutAnnotation.class;
    Class<TestAnnotation> annotationClass = TestAnnotation.class;
    String wrongField = "value";

    // when
    Exception actualException = catchException(() ->
      ReflectionUtils.getClassAnnotationValue(testedClass, annotationClass, wrongField,
        Object.class));

    // then
    assertThat(actualException).
      isInstanceOf(IllegalStateException.class)
      .hasMessage(ANNOTATION_VALUE_ERROR_MSG_PREFIX.formatted(annotationClass.getName(), wrongField,
        testedClass.getName()) + "Provided class don't have such annotation.");
  }

  @Test
  @DisplayName("Check if @BringComponent is component annotation then return true")
  @Order(27)
  void given_BringComponentAnnotation_When_IsComponentAnnotation_Then_True() {
    // data
    Annotation annotation = AnnotationWithComponent.class.getAnnotation(BringComponent.class);

    // when
    boolean actualResult = ReflectionUtils.isComponentAnnotation(annotation);

    // then
    assertThat(actualResult).isTrue();
  }

  @Test
  @DisplayName("Check if @TestAnnotationWithComponent is component annotation then return true")
  @Order(28)
  void given_AnnotationWithBringComponentInside_When_IsComponentAnnotation_Then_True() {
    // data
    Annotation annotation = AnnotatedClass2.class.getAnnotation(AnnotationWithComponent.class);

    // when
    boolean actualResult = ReflectionUtils.isComponentAnnotation(annotation);

    // then
    assertThat(actualResult).isTrue();
  }

  @Test
  @DisplayName("Check if @TestAnnotation is component annotation then return false")
  @Order(29)
  void given_AnnotationWithoutBringComponentInside_When_IsComponentAnnotation_Then_False() {
    // data
    Annotation annotation = AnnotatedClass.class.getAnnotation(TestAnnotation.class);

    // when
    boolean actualResult = ReflectionUtils.isComponentAnnotation(annotation);

    // then
    assertThat(actualResult).isFalse();
  }

  private static Stream<Arguments> testAnnotationFieldArgs() {
    return Stream.of(
      arguments("value", "stringValue", String.class),
      arguments("intValue", 4, Integer.class),
      arguments("boolValue", true, Boolean.class)
    );
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

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @interface TestAnnotation {

    String value() default "";

    int intValue();

    boolean boolValue() default false;

    TestEnum enumValue() default TestEnum.FIRST_VALUE;
  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @BringComponent
  @interface AnnotationWithComponent {

  }

  @TestAnnotation(value = "stringValue", intValue = 4, boolValue = true, enumValue = SECOND_VALUE)
  static class AnnotatedClass {

  }

  @AnnotationWithComponent
  static class AnnotatedClass2 {

  }

  static class TestClassWithoutAnnotation {

  }

  enum TestEnum {
    FIRST_VALUE,
    SECOND_VALUE
  }
}