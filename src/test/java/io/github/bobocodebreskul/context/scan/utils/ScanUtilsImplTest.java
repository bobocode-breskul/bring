package io.github.bobocodebreskul.context.scan.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.flat.FlatClass1;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.flat.FlatClass2;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.tree.TreeClass1;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.tree.TreeClass2;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.tree.l2.TreeClass1L2;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.tree.l2.TreeClass2L2;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.type.TypeAbstractClass;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.type.TypeAnnotation;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.type.TypeClass;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.type.TypeInterface;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.TestAnnotationWithComponent;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.TestComponentAnnotation;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.cyclic.CyclicCandidate2;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.multi.MultiCandidate1;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.parent.ParentCandidate;
import io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.single.SingleCandidate;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScanUtilsImplTest {

  private final ScanUtils scanUtils = new ScanUtilsImpl();

  @Test
  @DisplayName("Find all classes located in flat package tree")
  @Order(1)
  void given_Flat_When_SearchAllClasses_ThenFoundAllClasses() {
    // data
    String inputPackage = "io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.flat";
    // given
    List<Class<?>> expectedResult = List.of(FlatClass1.class, FlatClass2.class);
    // when
    var actualResult = scanUtils.searchAllClasses(inputPackage);
    // verify
    assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expectedResult);
  }

  @Test
  @DisplayName("Find all classes located in multilevel package tree")
  @Order(2)
  void given_TreePackage_When_SearchAllClasses_Then_FoundAllClasses() {
    // data
    String inputPackage = "io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.tree";
    // given
    List<Class<?>> expectedResult = List.of(
        TreeClass1.class, TreeClass2.class,
        TreeClass1L2.class, TreeClass2L2.class);
    // when
    Set<Class<?>> actualResult = scanUtils.searchAllClasses(inputPackage);
    // verify
    assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expectedResult);
  }

  @Test
  @DisplayName("Find all types of classes located in package with different class types")
  @Order(3)
  void given_Type_When_SearchAllClasses_Then_FoundAllClassTypes() {
    // data
    String inputPackage = "io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.type";
    // given
    List<Class<?>> expectedResult = List.of(
        TypeAbstractClass.class, TypeAnnotation.class,
        TypeClass.class, TypeInterface.class
    );
    // when
    var actualResult = scanUtils.searchAllClasses(inputPackage);
    // verify
    assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expectedResult);
  }

  @Test
  @DisplayName("Find single class by given annotation when candidate class has single annotation")
  @Order(4)
  void given_SingleAnnotationOnCandidate_When_SearchClassesByAnnotationRecursively_Then_FoundSingleClass() {
    // data
    String inputPackage = "io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.single";
    Class<? extends Annotation> inputFilterAnnotation = TestComponentAnnotation.class;
    // given
    List<Class<?>> expectedResult = List.of(SingleCandidate.class);
    // when
    Set<Class<?>> actualResult = scanUtils.searchClassesByAnnotationRecursively(inputPackage,
        inputFilterAnnotation);
    // verify
    assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expectedResult);
  }

  @Test
  @DisplayName("Find single class by given annotation when candidate class has multiple annotations")
  @Order(5)
  void given_MultiAnnotationsOnCandidate_When_SearchClassesByAnnotationRecursively_Then_FoundSingleClass() {
    // data
    String inputPackage = "io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.multi";
    Class<? extends Annotation> inputFilterAnnotation = TestComponentAnnotation.class;
    // given
    List<Class<?>> expectedResult = List.of(MultiCandidate1.class);
    // when
    Set<Class<?>> actualResult = scanUtils.searchClassesByAnnotationRecursively(inputPackage,
        inputFilterAnnotation);
    // verify
    assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expectedResult);
  }

  @Test
  @DisplayName("Find nothing when package does not contain classes with given annotation")
  @Order(6)
  public void given_NoneComponentAnnotation_When_SearchByAnnotation_Than_FoundNothing() {
    // data
    String inputPackage = "io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.none";
    // when
    var actualResult = scanUtils.searchClassesByAnnotationRecursively(inputPackage,
        TestComponentAnnotation.class);
    // verify
    assertThat(actualResult).isEmpty();
  }

  @Test
  @DisplayName("Find class with annotation which has target annotation as parent")
  @Order(7)
  void given_ComponentAnnotationAsParent_When_FindByAnnotationRecursively_Than_FindCorrectClass() {
    // data
    String inputPackage = "io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.parent";
    Class<? extends Annotation> inputFilterAnnotation = TestAnnotationWithComponent.class;
    // given
    List<Class<?>> expectedResult = List.of(ParentCandidate.class);
    // when
    Set<Class<?>> actualResult = scanUtils.searchClassesByAnnotationRecursively(inputPackage,
        inputFilterAnnotation);
    // verify
    assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expectedResult);
  }

  @Test
  @DisplayName("Resolve cyclic annotation dependencies without block and find single single class by given annotation")
  @Order(8)
  void given_CyclicAnnotationsOnCandidate_When_SearchClassesByAnnotationRecursively_Then_CyclicDependencyResolvedWithoutBlock() {
    // data
    String inputPackage = "io.github.bobocodebreskul.context.scan.utils.scantestsclasses.annotations.cyclic";
    Class<? extends Annotation> inputFilterAnnotation = TestComponentAnnotation.class;
    // given
    List<Class<?>> expectedResult = List.of(CyclicCandidate2.class);
    // when
    Set<Class<?>> actualResult = scanUtils.searchClassesByAnnotationRecursively(inputPackage,
        inputFilterAnnotation);
    // verify
    assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expectedResult);
  }

  @Test
  @DisplayName("Find classes located in multilevel package tree by given filter")
  @Order(9)
  void given_TreePackage_When_searchClassesByFilter_ThenFoundFilteredClass() {
    // data
    String inputPackage = "io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.tree";
    Predicate<Class<?>> filter = clazz -> clazz.getName().endsWith("TreeClass1L2");
    // given
    List<Class<?>> expectedResult = List.of(TreeClass1L2.class);
    // when
    var actualResult = scanUtils.searchClassesByFilter(inputPackage, filter);
    // verify
    assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expectedResult);
  }

  @Order(10)
  @ParameterizedTest(name = "Throws exception when null or empty package name was provided")
  @NullAndEmptySource
  @DisplayName("Throws exception when null or empty package name was provided")
  void given_ThrowsException_When_Packages_NullOrEmpty(String packageName) {
    assertThatThrownBy(() -> scanUtils.validatePackagesToScan(packageName))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Argument [packagesToScan] must not contain null or empty element");
  }

  @Order(11)
  @Test
  @DisplayName("Throws exception when no package was provided")
  void given_ThrowsException_When_NoPackages() {
    assertThatThrownBy(scanUtils::validatePackagesToScan)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Argument [packagesToScan] must contain at least one not null and not empty element");
  }

  @Order(12)
  @ParameterizedTest(name = "Throws exception when package names contain [{0}]")
  @ValueSource(strings = {"^", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "?", "~", "+", "-",
      "<", ">", "/", ","})
  @DisplayName("Throws exception when package names contain")
  void given_ThrowsException_When_InvalidPackageName(String packageName) {
    assertThatThrownBy(() -> scanUtils.validatePackagesToScan(packageName))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Package name must contain only letters, numbers and symbol [.]");
  }

  @Test
  @Order(13)
  @DisplayName("Nothing throw when package name is valid")
  void given_Nothing_When_PackageNameIsValid() {
    // data
    String inputPackage = "io.github.bobocodebreskul.context.scan.utils.scantestsclasses.all.tree";
    // verify
    assertDoesNotThrow(() -> scanUtils.validatePackagesToScan(inputPackage));
  }

  @Test
  @Order(14)
  @DisplayName("Nothing throw when valid packages with multiple elements")
  void given_NoExceptions_When_ValidPackagesWithMultipleElements() {
    String firstInputPackage = "com.example";
    String secondInputPackage = "org.sample";
    String thirdInputPackage = "io.github.bobocodebreskul";
    assertDoesNotThrow(() -> scanUtils.validatePackagesToScan(firstInputPackage, secondInputPackage,
        thirdInputPackage));
  }

  @Test
  @Order(15)
  @DisplayName("Nothing throw when valid packages with multiple elements")
  void given_NoExceptions_When_ValidPackageWithNumbers() {
    String inputPackage = "io.github.bobocodebreskul1";
    assertDoesNotThrow(() -> scanUtils.validatePackagesToScan(inputPackage));
  }
}