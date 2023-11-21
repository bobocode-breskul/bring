package io.github.bobocodebreskul.context.support;

import static io.github.bobocodebreskul.context.support.ReflectionUtils.ANNOTATION_VALUE_ERROR_MSG_PREFIX;
import static io.github.bobocodebreskul.context.support.ReflectionUtilsTest.TestEnum.SECOND_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReflectionUtilsTest {

  @Test
  @DisplayName("Check if annotation found in class")
  @Order(1)
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
  @Order(2)
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
  @Order(3)
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
  @Order(4)
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
  @Order(5)
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
  @Order(6)
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
  @Order(7)
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
  @Order(8)
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
  @Order(9)
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
  @Order(10)
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
  @Order(10)
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