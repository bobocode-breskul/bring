package io.github.bobocodebreskul.context.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.exception.AliasDuplicateException;
import io.github.bobocodebreskul.context.exception.BeanDefinitionDuplicateException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SimpleBeanDefinitionRegistryTest {

  private static final String CANNOT_REGISTER_DUPLICATE_ALIAS_MESSAGE =
      "Cannot registered an alias with name %s because an alias with that name is already registered%n";
  private static final String CANNOT_REGISTER_DUPLICATE_BEAN_DEFINITION_MESSAGE =
      "Cannot registered a beanDefinition with name %s because a beanDefinition with that name is already registered%n";
  private static final String ALIAS_SHOULD_NOT_BE_NULL = "alias should not be null";
  private static final String BEAN_NAME_SHOULD_NOT_BE_NULL = "beanName should not be null";
  private static final String BEAN_DEFINITION_SHOULD_NOT_BE_NULL_MESSAGE = "beanDefinition should not be null";


  private SimpleBeanDefinitionRegistry simpleBeanDefinitionRegistry;

  @BeforeEach
  void init() {
    simpleBeanDefinitionRegistry = new SimpleBeanDefinitionRegistry();
  }

  @Test
  @DisplayName("Register bean alias")
  @Order(1)
  void given_BeanNameAndAlias_When_AliasNotRegister_Then_RegisterAlias() {
    String testBeanName = "testBeanName";
    String testAlias = "testAlias";
    assertFalse(simpleBeanDefinitionRegistry.getAliases(testBeanName).contains(testAlias));
    simpleBeanDefinitionRegistry.registerAlias(testBeanName, testAlias);
    assertTrue(simpleBeanDefinitionRegistry.getAliases(testBeanName).contains(testAlias));
  }

  @Test
  @DisplayName("Register duplicate alias")
  @Order(2)
  void given_BeanNameAndAlias_When_AliasRegisterDuplicate_Then_ThrowAliasDuplicateException() {
    String testAlias = "testAlias";
    String testBeanName1 = "testBeanName1";
    String testBeanName2 = "testBeanName2";
    simpleBeanDefinitionRegistry.registerAlias(testBeanName1, testAlias);
    Exception exception = assertThrows(
        AliasDuplicateException.class,
        () -> simpleBeanDefinitionRegistry.registerAlias(testBeanName2, testAlias));

    Exception actualException = catchException(() -> simpleBeanDefinitionRegistry.registerAlias(
        TEST_BEAN_NAME_2, SimpleBeanDefinitionRegistryTest.TEST_ALIAS));

    String expectedMessage = CANNOT_REGISTER_DUPLICATE_ALIAS_MESSAGE.formatted(
        SimpleBeanDefinitionRegistryTest.TEST_ALIAS);
    assertThat(actualException)
        .isInstanceOf(AliasDuplicateException.class)
        .hasMessage(expectedMessage);

  }

  @Test
  @DisplayName("Register alias with null name")
  @Order(3)
  void given_BeanNameAndAlias_When_RegisterAliasWithNullName_Then_ThrowNoSuchBeanDefinitionException() {
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> simpleBeanDefinitionRegistry.registerAlias(null, "testBeanName"));

    String actualMessage = exception.getMessage();

    assertEquals(ALIAS_SHOULD_NOT_BE_NULL, actualMessage);
  }

  @Test
  @DisplayName("Register alias when bean name is null")
  @Order(4)
  void given_BeanNameAndAlias_When_RegisterNullBeanName_Then_ThrowNoSuchBeanDefinitionException() {
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> simpleBeanDefinitionRegistry.registerAlias("testName", null));

    String actualMessage = exception.getMessage();

    assertEquals(ALIAS_SHOULD_NOT_BE_NULL, actualMessage);
  }

  @Test
  @DisplayName("Remove alias")
  @Order(5)
  void given_Alias_When_RemoveAlias_Then_RegistryNotContainAlias() {
    simpleBeanDefinitionRegistry.registerAlias("testBeanName1", "testAlias1");
    simpleBeanDefinitionRegistry.registerAlias("testBeanName2", "testAlias2");

    assertTrue(simpleBeanDefinitionRegistry.getAliases("testBeanName1").contains("testAlias1"));
    simpleBeanDefinitionRegistry.removeAlias("testAlias1");
    assertTrue(simpleBeanDefinitionRegistry.getAliases("testBeanName1").isEmpty());
    assertTrue(simpleBeanDefinitionRegistry.getAliases("testBeanName2").contains("testAlias2"));
  }

  @Test
  @DisplayName("Remove non-existing alias")
  @Order(6)
  void given_Alias_When_RemoveNonExistentAlias_Then_RegistryNotChange() {
    simpleBeanDefinitionRegistry.registerAlias("testBeanName1", "testAlias1");
    simpleBeanDefinitionRegistry.registerAlias("testBeanName2", "testAlias2");

    simpleBeanDefinitionRegistry.removeAlias("testAlias");
    assertTrue(simpleBeanDefinitionRegistry.getAliases("testBeanName1").contains("testAlias1"));
    assertTrue(simpleBeanDefinitionRegistry.getAliases("testBeanName2").contains("testAlias2"));
  }

  @Test
  @DisplayName("Remove alias with null name")
  @Order(7)
  void given_NullAlias_When_RemoveAlias_Then_ThrowNoSuchBeanDefinitionException() {
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> simpleBeanDefinitionRegistry.removeAlias(null));

    assertEquals(ALIAS_SHOULD_NOT_BE_NULL, exception.getMessage());
  }

  @Test
  @DisplayName("isAlias existing alias")
  @Order(8)
  void given_Alias_When_IsAliasExistingAlias_Then_CheckIsAliasReturnsTrue() {
    simpleBeanDefinitionRegistry.registerAlias("testBeanName1", "testAlias1");

    assertThat(simpleBeanDefinitionRegistry.isAlias(TEST_ALIAS_1))
        .as("Alias TEST_ALIAS_1 should exist")
        .isTrue();
  }

  @Test
  @DisplayName("isAlias non-existing alias")
  @Order(9)
  void given_Alias_When_IsAliasNonExistingAlias_Then_CheckIsAliasReturnsFalse() {
    simpleBeanDefinitionRegistry.registerAlias("testBeanName1", "testAlias1");

    assertFalse(simpleBeanDefinitionRegistry.isAlias("testAlias"));
  }

  @Test
  @DisplayName("isAlias alias with null name")
  @Order(10)
  void given_NullAlias_When_IsAliasNullAlias_Then_ThrowNoSuchBeanDefinitionException() {
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> simpleBeanDefinitionRegistry.isAlias(null));

    assertEquals(ALIAS_SHOULD_NOT_BE_NULL, exception.getMessage());
  }

  @Test
  @DisplayName("Get aliases for bean name")
  @Order(11)
  void given_BeanName_when_GetAliases_Then_GetAllAliasesForBeanName() {
    assertEquals(0, simpleBeanDefinitionRegistry.getAliases("testBeanName").size());

    simpleBeanDefinitionRegistry.registerAlias("testBeanName", "testAlias1");
    simpleBeanDefinitionRegistry.registerAlias("testBeanName", "testAlias2");
    Set<String> aliases = simpleBeanDefinitionRegistry.getAliases("testBeanName");

    assertThat(aliases).containsExactlyInAnyOrder("testAlias1", "testAlias2");
  }

  @Test
  @DisplayName("Get aliases for null bean name")
  @Order(12)
  void given_NullBeanName_when_GetAliases_Then_ThrowNoSuchBeanDefinitionException() {
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> simpleBeanDefinitionRegistry.getAliases(null));

    assertEquals(ALIAS_SHOULD_NOT_BE_NULL, exception.getMessage());
  }

  @Test
  @DisplayName("Register bean definition")
  @Order(13)
  void given_BeanNameAndBeanDefinition_When_BeanDefinitionNotRegister_Then_RegisterBeanDefinition() {
    BeanDefinition beanDefinitionMock = Mockito.mock(BeanDefinition.class);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName", beanDefinitionMock);
    assertSame(beanDefinitionMock,
        simpleBeanDefinitionRegistry.getBeanDefinition("testBeanName"));
  }

  @Test
  @DisplayName("Register duplicate bean definition")
  @Order(14)
  void given_BeanNameAndBeanDefinition_When_BeanDefinitionRegisterDuplicate_Then_ThrowBeanDefinitionDuplicateException() {
    BeanDefinition beanDefinitionMock1 = Mockito.mock(BeanDefinition.class);
    BeanDefinition beanDefinitionMock2 = Mockito.mock(BeanDefinition.class);
    String testBeanName = "testBeanName";
    simpleBeanDefinitionRegistry.registerBeanDefinition(testBeanName, beanDefinitionMock1);
    Exception exception = assertThrows(
        BeanDefinitionDuplicateException.class,
        () -> simpleBeanDefinitionRegistry.registerBeanDefinition(testBeanName,
            beanDefinitionMock2));

    String expectedMessage = CANNOT_REGISTER_DUPLICATE_BEAN_DEFINITION_MESSAGE.formatted(
        testBeanName);
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  @DisplayName("Register bean definition with null name")
  @Order(15)
  void given_BeanNameAndBeanDefinition_When_RegisterBeanDefinitionWithNullName_Then_ThrowNoSuchBeanDefinitionException() {
    BeanDefinition beanDefinitionMock = Mockito.mock(BeanDefinition.class);
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> simpleBeanDefinitionRegistry.registerBeanDefinition(null, beanDefinitionMock));

    String actualMessage = exception.getMessage();

    assertEquals(BEAN_NAME_SHOULD_NOT_BE_NULL, actualMessage);
  }

  @Test
  @DisplayName("Register bean definition when bean definition is null")
  @Order(16)
  void given_BeanNameAndBeanDefinition_When_RegisterNullBeanDefinition_Then_ThrowNoSuchBeanDefinitionException() {
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> simpleBeanDefinitionRegistry.registerBeanDefinition("testName", null));

    String actualMessage = exception.getMessage();

    assertEquals(BEAN_DEFINITION_SHOULD_NOT_BE_NULL_MESSAGE, actualMessage);
  }

  @Test
  @DisplayName("Remove bean definition")
  @Order(17)
  void given_BeanName_When_RemoveBeanDefinition_Then_RegistryNotContainBean() {
    BeanDefinition beanDefinitionMock1 = Mockito.mock(BeanDefinition.class);
    BeanDefinition beanDefinitionMock2 = Mockito.mock(BeanDefinition.class);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName1", beanDefinitionMock1);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName2", beanDefinitionMock2);

    assertNotNull(simpleBeanDefinitionRegistry.getBeanDefinition("testBeanName1"));
    simpleBeanDefinitionRegistry.removeBeanDefinition("testBeanName1");
    assertNull(simpleBeanDefinitionRegistry.getBeanDefinition("testBeanName1"));
    assertNotNull(simpleBeanDefinitionRegistry.getBeanDefinition("testBeanName2"));
  }

  @Test
  @DisplayName("Remove non-existing bean definition")
  @Order(18)
  void given_BeanName_When_RemoveNonExistentBeanDefinition_Then_RegistryNotChange() {
    BeanDefinition beanDefinitionMock1 = Mockito.mock(BeanDefinition.class);
    BeanDefinition beanDefinitionMock2 = Mockito.mock(BeanDefinition.class);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName1", beanDefinitionMock1);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName2", beanDefinitionMock2);

    simpleBeanDefinitionRegistry.removeBeanDefinition("testBeanName");
    assertNotNull(simpleBeanDefinitionRegistry.getBeanDefinition("testBeanName1"));
    assertNotNull(simpleBeanDefinitionRegistry.getBeanDefinition("testBeanName2"));
  }

  @Test
  @DisplayName("Remove bean definition with null name")
  @Order(19)
  void given_NullBeanName_When_RemoveBeanDefinition_Then_ThrowNoSuchBeanDefinitionException() {
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> simpleBeanDefinitionRegistry.removeBeanDefinition(null));

    assertEquals(BEAN_NAME_SHOULD_NOT_BE_NULL, exception.getMessage());
  }

  @Test
  @DisplayName("Get bean definition")
  @Order(20)
  void given_BeanName_When_GetBeanDefinition_Then_GetBeanDefinition() {
    BeanDefinition beanDefinitionMock1 = Mockito.mock(BeanDefinition.class);
    BeanDefinition beanDefinitionMock2 = Mockito.mock(BeanDefinition.class);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName1", beanDefinitionMock1);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName2", beanDefinitionMock2);

    assertNull(simpleBeanDefinitionRegistry.getBeanDefinition("testBeanName"));
    assertSame(beanDefinitionMock1,
        simpleBeanDefinitionRegistry.getBeanDefinition("testBeanName1"));
    assertSame(beanDefinitionMock2,
        simpleBeanDefinitionRegistry.getBeanDefinition("testBeanName2"));
  }

  @Test
  @DisplayName("Get bean definition with null name")
  @Order(21)
  void given_NullBeanName_When_GetBeanDefinition_Then_ThrowNoSuchBeanDefinitionException() {
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> simpleBeanDefinitionRegistry.getBeanDefinition(null));

    assertEquals(BEAN_NAME_SHOULD_NOT_BE_NULL, exception.getMessage());
  }

  @Test
  @DisplayName("Contains existing bean definition")
  @Order(22)
  void given_BeanName_When_ContainsExistingBeanDefinition_Then_CheckContainsBeanDefinitionReturnsTrue() {
    BeanDefinition beanDefinitionMock = Mockito.mock(BeanDefinition.class);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName1", beanDefinitionMock);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName2", beanDefinitionMock);

    assertTrue(simpleBeanDefinitionRegistry.containsBeanDefinition("testBeanName1"));
    assertTrue(simpleBeanDefinitionRegistry.containsBeanDefinition("testBeanName2"));
  }

  @Test
  @DisplayName("Contains non-existing bean definition")
  @Order(23)
  void given_BeanName_When_ContainsNonExistingBeanDefinition_Then_CheckContainsBeanDefinitionReturnsFalse() {
    BeanDefinition beanDefinitionMock = Mockito.mock(BeanDefinition.class);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName1", beanDefinitionMock);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName2", beanDefinitionMock);

    assertFalse(simpleBeanDefinitionRegistry.containsBeanDefinition("testBeanName"));
  }

  @Test
  @DisplayName("Contains bean definition with null name")
  @Order(24)
  void given_NullBeanName_When_ContainsBeanDefinition_Then_ThrowNoSuchBeanDefinitionException() {
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> simpleBeanDefinitionRegistry.containsBeanDefinition(null));

    assertEquals(BEAN_NAME_SHOULD_NOT_BE_NULL, exception.getMessage());
  }

  @Test
  @DisplayName("Get registered bean definition names")
  @Order(25)
  void when_RegisterBeanDefinitions_Then_GetRegisteredBeanDefinitionNames() {
    assertEquals(0, simpleBeanDefinitionRegistry.getBeanDefinitionNames().size());

    BeanDefinition beanDefinitionMock = Mockito.mock(BeanDefinition.class);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName1", beanDefinitionMock);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName2", beanDefinitionMock);
    Set<String> beanDefinitionNames = simpleBeanDefinitionRegistry.getBeanDefinitionNames();

    assertThat(beanDefinitionNames).containsExactlyInAnyOrder("testBeanName1", "testBeanName2");
  }

  @Test
  @DisplayName("Get registered bean definition count after register bean definition")
  @Order(26)
  void when_RegisterBeanDefinitions_Then_GetRegisteredBeanDefinitionCount() {
    assertEquals(0, simpleBeanDefinitionRegistry.getBeanDefinitionCount());

    BeanDefinition beanDefinitionMock = Mockito.mock(BeanDefinition.class);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName", beanDefinitionMock);

    assertEquals(1, simpleBeanDefinitionRegistry.getBeanDefinitionCount());
  }

  @Test
  @DisplayName("Get registered bean definition count after remove bean definition")
  @Order(27)
  void when_RemoveBeanDefinitions_Then_GetRegisteredBeanDefinitionCount() {
    assertEquals(0, simpleBeanDefinitionRegistry.getBeanDefinitionCount());

    BeanDefinition beanDefinitionMock = Mockito.mock(BeanDefinition.class);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName1", beanDefinitionMock);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName2", beanDefinitionMock);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanName3", beanDefinitionMock);
    simpleBeanDefinitionRegistry.removeBeanDefinition("testBeanName2");

    assertEquals(2, simpleBeanDefinitionRegistry.getBeanDefinitionCount());
  }

  @Test
  @DisplayName("isBeanNameInUse with used bean name")
  @Order(28)
  void given_BeanName_When_UsedBeanName_Then_IsBeanNameInUseReturnsTrue() {
    simpleBeanDefinitionRegistry.registerAlias("testBeanNameForAlias", "testAlias");
    assertTrue(simpleBeanDefinitionRegistry.isBeanNameInUse("testAlias"));
    BeanDefinition beanDefinitionMock = Mockito.mock(BeanDefinition.class);
    simpleBeanDefinitionRegistry.registerBeanDefinition("testBeanNameForBeanDefinition",
        beanDefinitionMock);
    assertTrue(simpleBeanDefinitionRegistry.isBeanNameInUse("testBeanNameForBeanDefinition"));
  }

  @Test
  @DisplayName("isBeanNameInUse with non-used bean name")
  @Order(29)
  void given_BeanName_When_NonUsedBeanName_Then_IsBeanNameInUseReturnsFalse() {
    simpleBeanDefinitionRegistry.registerAlias("testBeanNameForAlias", "testAlias");
    assertFalse(simpleBeanDefinitionRegistry.isBeanNameInUse("testBeanNameForAlias"));
    assertFalse(simpleBeanDefinitionRegistry.isBeanNameInUse("testBeanName"));
  }

  @Test
  @DisplayName("isBeanNameInUse with null bean name")
  @Order(30)
  void given_NullBeanName_When_IsBeanNameInUseNullBeanName_Then_ThrowNoSuchBeanDefinitionException() {
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> simpleBeanDefinitionRegistry.isBeanNameInUse(null));

    assertEquals(BEAN_NAME_SHOULD_NOT_BE_NULL, exception.getMessage());
  }
}