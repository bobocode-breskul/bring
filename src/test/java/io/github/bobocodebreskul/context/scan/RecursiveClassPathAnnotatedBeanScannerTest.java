package io.github.bobocodebreskul.context.scan;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.registry.AnnotatedBeanDefinitionReader;
import io.github.bobocodebreskul.context.scan.utils.ScanUtils;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.multi.MultiCandidate1;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.multi.MultiCandidate2;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.multi.MultiCandidate3;
import java.util.Collections;
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
  private AnnotatedBeanDefinitionReader beanDefinitionReader;

  @Test
  @DisplayName("Register two bean definitions when scan by single package")
  @Order(1)
  void given_SinglePackage_When_Scan_Then_RegisterTwoBeans() {
    // data
    String inputPackage = TEST_PACKAGE_ONE;
    // given
    Class<?> expectedClass1 = MultiCandidate1.class;
    Class<?> expectedClass2 = MultiCandidate2.class;

    given(scanUtils.searchClassesByAnnotationRecursively(inputPackage, BringComponent.class))
        .willReturn(Set.of(expectedClass1, expectedClass2));
    // when
    annotatedBeanScanner.scan(inputPackage);
    // verify
    then(beanDefinitionReader).should().registerBean(expectedClass1);
    then(beanDefinitionReader).should().registerBean(expectedClass2);
    then(beanDefinitionReader).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("Register three bean definitions when scan by multi package")
  @Order(2)
  void given_MultiPackage_When_Scan_Then_RegisterThreeBeans() {
    // data
    String inputPackage1 = TEST_PACKAGE_ONE;
    String inputPackage2 = TEST_PACKAGE_TWO;
    // given
    Class<?> expectedClass1 = MultiCandidate1.class;
    Class<?> expectedClass2 = MultiCandidate2.class;
    Class<?> expectedClass3 = MultiCandidate3.class;

    given(scanUtils.searchClassesByAnnotationRecursively(inputPackage1, BringComponent.class))
        .willReturn(Set.of(expectedClass1, expectedClass2));
    given(scanUtils.searchClassesByAnnotationRecursively(inputPackage2, BringComponent.class))
        .willReturn(Set.of(expectedClass3));
    // when
    annotatedBeanScanner.scan(inputPackage1, inputPackage2);
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
    // data
    String packageName = TEST_PACKAGE_ONE;
    // given
    given(scanUtils.searchClassesByAnnotationRecursively(packageName, BringComponent.class))
        .willReturn(Collections.emptySet());
    // when
    annotatedBeanScanner.scan(packageName);
    // verify
    then(beanDefinitionReader).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("Do nothing when provided empty package list")
  @Order(4)
  void given_EmptyInput_When_Scan_Then_RegisterDoNothing() {
    // when
    annotatedBeanScanner.scan();
    // verify
    then(scanUtils).shouldHaveNoInteractions();
    then(beanDefinitionReader).shouldHaveNoInteractions();
  }
}