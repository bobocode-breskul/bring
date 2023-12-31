package io.github.bobocodebreskul.context.scan;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.registry.BeanDefinitionReader;
import io.github.bobocodebreskul.context.scan.utils.ScanUtils;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.ConfigTestClass;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.multi.MultiCandidate1;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.multi.MultiCandidate2;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.multi.MultiCandidate3;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
class RecursiveClassPathAnnotatedBeanScannerTest {

  private static final String TEST_PACKAGE_ONE = "test.package.one";
  private static final String TEST_PACKAGE_TWO = "test.package.two";

  @InjectMocks
  private RecursiveClassPathAnnotatedBeanScanner annotatedBeanScanner;

  @Mock
  private ScanUtils scanUtils;
  @Mock
  private BeanDefinitionReader beanDefinitionReader;

  @Test
  @DisplayName("Register two bean definitions when scan by single package")
  @Order(1)
  void given_SinglePackage_When_Scan_Then_RegisterTwoBeans() {
    // given
    Class<?> expectedClass1 = MultiCandidate1.class;
    Class<?> expectedClass2 = MultiCandidate2.class;

    given(scanUtils.searchClassesByAnnotationRecursively(TEST_PACKAGE_ONE, BringComponent.class))
        .willReturn(Set.of(expectedClass1, expectedClass2));
    given(scanUtils.readBasePackages(ConfigTestClass.class)).willReturn(
        new HashSet<>(List.of(TEST_PACKAGE_ONE)));
    // when
    annotatedBeanScanner.scan(ConfigTestClass.class);
    // verify
    then(beanDefinitionReader).should().registerBean(expectedClass1);
    then(beanDefinitionReader).should().registerBean(expectedClass2);
    then(beanDefinitionReader).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("Register three bean definitions when scan by multi package")
  @Order(2)
  void given_MultiPackage_When_Scan_Then_RegisterThreeBeans() {
    // given
    Class<?> expectedClass1 = MultiCandidate1.class;
    Class<?> expectedClass2 = MultiCandidate2.class;
    Class<?> expectedClass3 = MultiCandidate3.class;

    given(scanUtils.searchClassesByAnnotationRecursively(TEST_PACKAGE_ONE, BringComponent.class))
        .willReturn(Set.of(expectedClass1, expectedClass2));
    given(scanUtils.searchClassesByAnnotationRecursively(TEST_PACKAGE_TWO, BringComponent.class))
        .willReturn(Set.of(expectedClass3));
    given(scanUtils.readBasePackages(ConfigTestClass.class)).willReturn(
        new HashSet<>(Arrays.asList(TEST_PACKAGE_ONE, TEST_PACKAGE_TWO)));
    // when
    annotatedBeanScanner.scan(ConfigTestClass.class);
    // verify
    then(beanDefinitionReader).should().registerBean(expectedClass1);
    then(beanDefinitionReader).should().registerBean(expectedClass2);
    then(beanDefinitionReader).should().registerBean(expectedClass3);
    then(beanDefinitionReader).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("Register none bean definition when scan empty package")
  @Order(3)
  void given_EmptyPackage_When_Scan_Then_RegisterNothing() {
    // given
    given(scanUtils.searchClassesByAnnotationRecursively(TEST_PACKAGE_ONE, BringComponent.class))
        .willReturn(Collections.emptySet());
    given(scanUtils.readBasePackages(ConfigTestClass.class)).willReturn(
        new HashSet<>(List.of(TEST_PACKAGE_ONE)));
    // when
    annotatedBeanScanner.scan(ConfigTestClass.class);
    // verify
    then(beanDefinitionReader).shouldHaveNoInteractions();
  }
}