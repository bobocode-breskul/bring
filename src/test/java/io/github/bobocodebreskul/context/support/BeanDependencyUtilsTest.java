package io.github.bobocodebreskul.context.support;

import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.config.BeanDependency;
import io.github.bobocodebreskul.context.exception.DependencyNotResolvedException;
import io.github.bobocodebreskul.context.registry.BeanDefinitionRegistry;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BeanDependencyUtilsTest {

  private static final String TEST_QUALIFIER_1 = "TEST_QUALIFIER_1";
  private static final String TEST_QUALIFIER_2 = "TEST_QUALIFIER_2";
  private static final String TEST_BEAN_NAME_1 = "TEST_BEAN_NAME_1";

  @InjectMocks
  private BeanDependencyUtils beanDependencyUtils;

  @Mock
  private BeanDefinitionRegistry beanDefinitionRegistry;

  @Mock
  private BeanDefinition inputBeanDefinition;

  @Test
  @DisplayName("Return valid bean definition for single dependency by qualifier")
  @Order(1)
  void given_BeanWithQualifierDependency_When_PrepareDependencies_Then_ReturnValidDefinition() {
    // data
    String inputQualifierName = TEST_QUALIFIER_1;
    BeanDependency inputBeanDependency = new BeanDependency(null, inputQualifierName,
        TestClass1.class);
    // given
    BeanDefinition expectedBeanDefinition = new AnnotatedGenericBeanDefinition(TestClass1.class);
    given(inputBeanDefinition.getDependencies()).willReturn(List.of(inputBeanDependency));
    given(beanDefinitionRegistry.containsBeanDefinition(inputQualifierName)).willReturn(TRUE);
    given(beanDefinitionRegistry.getBeanDefinition(inputQualifierName)).willReturn(expectedBeanDefinition);
    // when
    List<BeanDefinition> actualBeanDefinitions = beanDependencyUtils.prepareDependencies(
        inputBeanDefinition, beanDefinitionRegistry);
    // then
    assertThat(actualBeanDefinitions)
        .usingRecursiveFieldByFieldElementComparator
            ().containsExactly(expectedBeanDefinition);
  }

  @Test
  @DisplayName("Throw exception when bean definition not found by qualifier name")
  @Order(2)
  void given_BeanDependency_When_BeanDefinitionalNullByQualifierNameThen_ThrowException() {
    // data
    String dummyQualifier = TEST_QUALIFIER_1;
    BeanDependency dependency = new BeanDependency(null, dummyQualifier, TestClass1.class);
    // given
    given(inputBeanDefinition.getDependencies()).willReturn(List.of(dependency));
    given(beanDefinitionRegistry.containsBeanDefinition(dummyQualifier)).willReturn(false);
    // when
    // then
    assertThatThrownBy(
        () -> beanDependencyUtils.prepareDependencies(inputBeanDefinition, beanDefinitionRegistry))
        .isInstanceOf(DependencyNotResolvedException.class)
        .hasMessage("No suitable dependency found for qualifier " + dummyQualifier);
  }

  @Test
  @DisplayName("Throw exception when valid bean definition by qualifier which have different type with dependency type")
  @Order(3)
  void given_DependencyWithQualifiedDependency_When_QualifiedDependencyMismatchByTypeWithBeanDependency_Then_ThrowException() {
    // data
    String dummyQualifier = TEST_QUALIFIER_1;
    BeanDependency dependency = new BeanDependency(null, dummyQualifier, TestClass1.class);
    BeanDefinition dependencyBeanDefinition =  new AnnotatedGenericBeanDefinition(TestClass2.class);
    // given
    given(inputBeanDefinition.getDependencies()).willReturn(List.of(dependency));
    given(beanDefinitionRegistry.containsBeanDefinition(dummyQualifier)).willReturn(true);
    given(beanDefinitionRegistry.getBeanDefinition(dummyQualifier)).willReturn(dependencyBeanDefinition);
    // when
    // then
    assertThatThrownBy(
        () -> beanDependencyUtils.prepareDependencies(inputBeanDefinition, beanDefinitionRegistry))
        .isInstanceOf(DependencyNotResolvedException.class)
        .hasMessage(String.format("Mismatched type for dependency %s. Expected: %s, Actual: %s",
            dependency, dependency.type(), dependencyBeanDefinition.getBeanClass()));
  }

  @Test
  @DisplayName("Return valid bean definition for single dependency by name")
  @Order(4)
  void given_DependencyWithOutQualifiedDependency_When_DependencyByNamePresent_Then_ReturnValidDefinition() {
    // data
    String beanName = TEST_BEAN_NAME_1;
    BeanDependency dependency = new BeanDependency(beanName, null, TestClass1.class);
    BeanDefinition foundBeanDefinition = new AnnotatedGenericBeanDefinition(TestClass1.class);
    // given
    given(inputBeanDefinition.getDependencies()).willReturn(List.of(dependency));
    given(beanDefinitionRegistry.containsBeanDefinition(beanName)).willReturn(true);
    given(beanDefinitionRegistry.getBeanDefinition(beanName)).willReturn(foundBeanDefinition);
    // when
    List<BeanDefinition> actualBeanDefinitions = beanDependencyUtils.prepareDependencies(
        inputBeanDefinition, beanDefinitionRegistry);
    // then
    assertThat(actualBeanDefinitions)
        .usingRecursiveFieldByFieldElementComparator
            ().containsExactly(foundBeanDefinition);

  }

  @ParameterizedTest
  @MethodSource("getChildAndParentClassArguments")
  @DisplayName("Return valid bean definition for single dependency by interface/abstract class type")
  @Order(5)
  void given_BeanWithAbstractClassTypeDependency_When_PrepareDependencies_Then_Return(Class<?> inputClass, Class<?> expectedClass) {
    // data
    BeanDependency inputBeanDependency = new BeanDependency(TEST_BEAN_NAME_1, null, inputClass);
    // given
    BeanDefinition expectedBeanDefinition = new AnnotatedGenericBeanDefinition(expectedClass);
    given(inputBeanDefinition.getDependencies()).willReturn(List.of(inputBeanDependency));
    given(beanDefinitionRegistry.getBeanDefinitionByType(inputClass)).willReturn(List.of(expectedBeanDefinition));
    // when
    List<BeanDefinition> actualBeanDefinitions = beanDependencyUtils
        .prepareDependencies(inputBeanDefinition, beanDefinitionRegistry);
    // then
    assertThat(actualBeanDefinitions)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(expectedBeanDefinition);
  }

  @ParameterizedTest
  @MethodSource("getChildAndMultiParentClassArguments")
  @DisplayName("Return valid bean definition for multi dependency with single primary marker")
  @Order(6)
  void given_BeanWithMultiTypeDependencyAndSinglePrimary_When_PrepareDependencies_Then_ReturnValidDefinition(
      Class<?> inputClass, Class<?> expectedClassNoPrimary, Class<?> expectedClassWithPrimary) {
    // data
    BeanDependency inputBeanDependency = new BeanDependency(TEST_BEAN_NAME_1, null, inputClass);
    // given
    BeanDefinition expectedBeanDefinitionNoPrimary = new AnnotatedGenericBeanDefinition(expectedClassNoPrimary);
    BeanDefinition expectedBeanDefinitionWithPrimary = new AnnotatedGenericBeanDefinition(expectedClassWithPrimary);
    expectedBeanDefinitionWithPrimary.setPrimary(true);
    given(inputBeanDefinition.getDependencies()).willReturn(List.of(inputBeanDependency));
    given(beanDefinitionRegistry.getBeanDefinitionByType(inputClass))
        .willReturn(List.of(expectedBeanDefinitionNoPrimary, expectedBeanDefinitionWithPrimary));
    // when
    List<BeanDefinition> actualBeanDefinitions = beanDependencyUtils
        .prepareDependencies(inputBeanDefinition, beanDefinitionRegistry);
    // then
    assertThat(actualBeanDefinitions)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(expectedBeanDefinitionWithPrimary);
  }


  @Test
  @DisplayName("Throw exception when bean not found by any of the conditions")
  @Order(7)
  void given_DependencyForSpecificClassWithOutQualifiedDependency_When_DependencyByNameAbsentPresent_Then_ThrowException() {
    // data
    String beanName = TEST_BEAN_NAME_1;
    BeanDependency dependency = new BeanDependency(beanName, null, TestClass1.class);
    // given
    given(inputBeanDefinition.getDependencies()).willReturn(List.of(dependency));
    given(beanDefinitionRegistry.containsBeanDefinition(beanName)).willReturn(false);
    // when
    // then
    assertThatThrownBy(
        () -> beanDependencyUtils.prepareDependencies(inputBeanDefinition, beanDefinitionRegistry))
        .isInstanceOf(DependencyNotResolvedException.class)
        .hasMessage("No suitable dependency found for " + dependency);

  }

  @ParameterizedTest
  @MethodSource("getParentClassArguments")
  @DisplayName("Throw DependencyNotResolvedException for bean with single dependency by class and no candidates")
  @Order(8)
  void given_BeanWithSingleDependencyAndNoCandidates_When_PrepareDependencies_Then_ThrowDependencyNotResolvedException(
      Class<?> inputClass) {
    // data
    BeanDependency inputBeanDependency = new BeanDependency(TEST_BEAN_NAME_1, null, inputClass);
    // given
    given(inputBeanDefinition.getDependencies()).willReturn(List.of(inputBeanDependency));
    given(beanDefinitionRegistry.getBeanDefinitionByType(inputClass)).willReturn(List.of());
    // when
    // then
    assertThatThrownBy(() -> beanDependencyUtils.prepareDependencies(inputBeanDefinition, beanDefinitionRegistry))
        .isInstanceOf(DependencyNotResolvedException.class)
        .hasMessage("No suitable dependency found for " + inputBeanDependency);
  }

  @ParameterizedTest
  @MethodSource("getChildAndMultiParentClassArguments")
  @DisplayName("Throw DependencyNotResolvedException for bean with multi dependency and multi primary marker")
  @Order(9)
  void given_BeanWithMultiTypeDependencyAndMultiPrimary_When_PrepareDependencies_Then_ThrowDependencyNotResolvedException(
      Class<?> inputClass, Class<?> expectedClassWithPrimary1, Class<?> expectedClassWithPrimary2) {
    // data
    BeanDependency inputBeanDependency = new BeanDependency(TEST_BEAN_NAME_1, null, inputClass);
    // given
    BeanDefinition expectedBeanDefinitionWithPrimary1 = new AnnotatedGenericBeanDefinition(expectedClassWithPrimary1);
    BeanDefinition expectedBeanDefinitionWithPrimary2 = new AnnotatedGenericBeanDefinition(expectedClassWithPrimary2);
    expectedBeanDefinitionWithPrimary1.setPrimary(true);
    expectedBeanDefinitionWithPrimary2.setPrimary(true);
    given(inputBeanDefinition.getDependencies()).willReturn(List.of(inputBeanDependency));
    given(beanDefinitionRegistry.getBeanDefinitionByType(inputClass))
        .willReturn(List.of(expectedBeanDefinitionWithPrimary1, expectedBeanDefinitionWithPrimary2));
    // when
    // then
    assertThatThrownBy(
        () -> beanDependencyUtils.prepareDependencies(inputBeanDefinition, beanDefinitionRegistry))
        .isInstanceOf(DependencyNotResolvedException.class)
        .hasMessage("No suitable dependency found for " + inputBeanDependency);
  }

  @Test
  @DisplayName("Throw exception when more than one bean definition is found for TYPE and none marked as @Primary")
  @Order(10)
  public void given_BeanWithMultiTypeDependency_When_PrepareDependenciesWithoutPrimaryAnnotations_Then_ThrowDependencyNotResolvedException() {
    // data
    BeanDependency inputBeanDependency = new BeanDependency(TEST_BEAN_NAME_1, null, TestInterface1.class);
    // given
    BeanDefinition beanDefinition1 = new AnnotatedGenericBeanDefinition(TestInterfaceImpl1.class);
    BeanDefinition beanDefinition2 = new AnnotatedGenericBeanDefinition(TestInterfaceImpl2.class);
    given(inputBeanDefinition.getDependencies()).willReturn(List.of(inputBeanDependency));
    given(beanDefinitionRegistry.getBeanDefinitionByType(inputBeanDependency.type())).willReturn(List.of(beanDefinition1, beanDefinition2));
    // when
    // then
    assertThatThrownBy(
        () -> beanDependencyUtils.prepareDependencies(inputBeanDefinition, beanDefinitionRegistry))
        .isInstanceOf(DependencyNotResolvedException.class)
        .hasMessage("No suitable dependency found for " + inputBeanDependency);
  }

  @Test
  @DisplayName("Return valid bean definition for two dependencies of same interface by qualifier")
  @Order(11)
  public void given_BeanDefinitionWithTwoDependenciesOfSameType_When_BothWithQualifierAndWithSameInterface_Then_ReturnValidDefinition() {
    // data
    String qualifier1 = TEST_QUALIFIER_1;
    String qualifier2 = TEST_QUALIFIER_2;
    BeanDependency beanDependency1 = new BeanDependency(null, qualifier1, TestInterface1.class);
    BeanDependency beanDependency2 = new BeanDependency(null, qualifier2, TestInterface1.class);
    // given
    BeanDefinition beanDefinition1 = new AnnotatedGenericBeanDefinition(TestInterfaceImpl1.class);
    BeanDefinition beanDefinition2 = new AnnotatedGenericBeanDefinition(TestInterfaceImpl2.class);
    given(inputBeanDefinition.getDependencies()).willReturn(List.of(beanDependency1, beanDependency2));
    given(beanDefinitionRegistry.containsBeanDefinition(qualifier1)).willReturn(true);
    given(beanDefinitionRegistry.containsBeanDefinition(qualifier2)).willReturn(true);
    given(beanDefinitionRegistry.getBeanDefinition(qualifier1)).willReturn(beanDefinition1);
    given(beanDefinitionRegistry.getBeanDefinition(qualifier2)).willReturn(beanDefinition2);
    // when
    List<BeanDefinition> actualBeanDefinitions = beanDependencyUtils
        .prepareDependencies(inputBeanDefinition, beanDefinitionRegistry);
    // then
    assertThat(actualBeanDefinitions)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(beanDefinition1, beanDefinition2);
  }

  @Test
  @DisplayName("Return valid bean definition for two dependencies of same interface one with qualifier and other with primary")
  @Order(12)
  public void given_BeanDefinitionWithTwoDependenciesOfSameType_When_OneWithQualifierAndOneWithPrimary_Then_ReturnValidDefinition() {
    // data
    String qualifier1 = TEST_QUALIFIER_1;
    BeanDependency beanDependency1 = new BeanDependency(null, qualifier1, TestInterface1.class);
    BeanDependency beanDependency2 = new BeanDependency(null, null, TestInterface1.class);
    // given
    BeanDefinition beanDefinition1 = new AnnotatedGenericBeanDefinition(TestInterfaceImpl1.class);
    BeanDefinition beanDefinition2 = new AnnotatedGenericBeanDefinition(TestInterfaceImpl2.class);
    beanDefinition2.setPrimary(true);
    given(inputBeanDefinition.getDependencies()).willReturn(List.of(beanDependency1, beanDependency2));
    given(beanDefinitionRegistry.containsBeanDefinition(qualifier1)).willReturn(true);
    given(beanDefinitionRegistry.getBeanDefinition(qualifier1)).willReturn(beanDefinition1);
    given(beanDefinitionRegistry.getBeanDefinitionByType(beanDependency2.type())).willReturn(List.of(beanDefinition1, beanDefinition2));
    // when
    List<BeanDefinition> actualBeanDefinitions = beanDependencyUtils
        .prepareDependencies(inputBeanDefinition, beanDefinitionRegistry);
    // then
    assertThat(actualBeanDefinitions)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(beanDefinition1, beanDefinition2);
  }

  private static Stream<Arguments> getParentClassArguments() {
    return Stream.of(
        Arguments.of(TestInterface1.class),
        Arguments.of(TestAbstractClass1.class)
    );
  }

  private static Stream<Arguments> getChildAndParentClassArguments() {
    return Stream.of(
        Arguments.of(TestInterface1.class, TestInterfaceImpl1.class),
        Arguments.of(TestAbstractClass1.class, TestAbstractClassImpl1.class)
    );
  }

  private static Stream<Arguments> getChildAndMultiParentClassArguments() {
    return Stream.of(
        Arguments.of(TestInterface1.class, TestInterfaceImpl1.class, TestInterfaceImpl2.class),
        Arguments.of(TestAbstractClass1.class, TestAbstractClassImpl1.class, TestAbstractClassImpl2.class)
    );
  }


  private static class TestClass1 {

  }

  private static class TestClass2 {

  }

  private interface TestInterface1 {}

  private static class TestInterfaceImpl1 implements TestInterface1 {}
  private static class TestInterfaceImpl2 implements TestInterface1 {}

  private static abstract class TestAbstractClass1 {}

  private static class TestAbstractClassImpl1 extends TestAbstractClass1 {}
  private static class TestAbstractClassImpl2 extends TestAbstractClass1 {}
}