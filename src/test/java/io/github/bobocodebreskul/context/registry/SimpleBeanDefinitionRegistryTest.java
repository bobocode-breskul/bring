package io.github.bobocodebreskul.context.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.exception.AliasDuplicateException;
import io.github.bobocodebreskul.context.exception.BeanDefinitionDuplicateException;
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
  private static final String TEST_BEAN_NAME = "testBeanName";
  private static final String TEST_BEAN_NAME_1 = "testBeanName1";
  private static final String TEST_BEAN_NAME_2 = "testBeanName2";
  private static final String TEST_ALIAS = "testAlias";
  private static final String TEST_ALIAS_1 = "testAlias1";
  private static final String TEST_ALIAS_2 = "testAlias2";
  private static final String TEST_BEAN_NAME_FOR_ALIAS = "testBeanNameForAlias";

  private static final BeanDefinition BEAN_DEFINITION_MOCK = Mockito.mock(BeanDefinition.class);
  private static final BeanDefinition BEAN_DEFINITION_MOCK_2 = Mockito.mock(BeanDefinition.class);

  private SimpleBeanDefinitionRegistry simpleBeanDefinitionRegistry;

  @BeforeEach
  void init() {
    simpleBeanDefinitionRegistry = new SimpleBeanDefinitionRegistry();
  }

  @Test
  @DisplayName("Register bean alias")
  @Order(1)
  void given_BeanNameAndAlias_When_AliasNotRegister_Then_RegisterAlias() {
    assertThat(simpleBeanDefinitionRegistry.getAliases(TEST_BEAN_NAME))
        .doesNotContain(TEST_ALIAS);
    simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME, TEST_ALIAS);
    assertThat(simpleBeanDefinitionRegistry.getAliases(TEST_BEAN_NAME))
        .contains(TEST_ALIAS);
  }

  @Test
  @DisplayName("Register duplicate alias")
  @Order(2)
  void given_BeanNameAndAlias_When_AliasRegisterDuplicate_Then_ThrowAliasDuplicateException() {
    simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME_1,
        SimpleBeanDefinitionRegistryTest.TEST_ALIAS);

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
    Exception actualException = catchException(
        () -> simpleBeanDefinitionRegistry.registerAlias(null, TEST_BEAN_NAME));

    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ALIAS_SHOULD_NOT_BE_NULL);
  }

  @Test
  @DisplayName("Register alias when bean name is null")
  @Order(4)
  void given_BeanNameAndAlias_When_RegisterNullBeanName_Then_ThrowNoSuchBeanDefinitionException() {
    Exception actualException = catchException(
        () -> simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME, null));

    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ALIAS_SHOULD_NOT_BE_NULL);
  }

  @Test
  @DisplayName("Remove alias")
  @Order(5)
  void given_Alias_When_RemoveAlias_Then_RegistryNotContainAlias() {
    simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME_1, TEST_ALIAS_1);
    simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME_2, TEST_ALIAS_2);

    assertThat(simpleBeanDefinitionRegistry.getAliases(TEST_BEAN_NAME_1)).as("")
        .contains(TEST_ALIAS_1);
    simpleBeanDefinitionRegistry.removeAlias(TEST_ALIAS_1);
    assertThat(simpleBeanDefinitionRegistry.getAliases(TEST_BEAN_NAME_1)).isEmpty();
    assertThat(simpleBeanDefinitionRegistry.getAliases(TEST_BEAN_NAME_2)).as("")
        .contains(TEST_ALIAS_2);
  }

  @Test
  @DisplayName("Remove non-existing alias")
  @Order(6)
  void given_Alias_When_RemoveNonExistentAlias_Then_RegistryNotChange() {
    simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME_1, TEST_ALIAS_1);
    simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME_2, TEST_ALIAS_2);

    simpleBeanDefinitionRegistry.removeAlias(TEST_ALIAS);
    assertThat(simpleBeanDefinitionRegistry.getAliases(TEST_BEAN_NAME_1)).contains(TEST_ALIAS_1);
    assertThat(simpleBeanDefinitionRegistry.getAliases(TEST_BEAN_NAME_2)).contains(TEST_ALIAS_2);
  }

  @Test
  @DisplayName("Remove alias with null name")
  @Order(7)
  void given_NullAlias_When_RemoveAlias_Then_ThrowNoSuchBeanDefinitionException() {
    Exception actualException = catchException(
        () -> simpleBeanDefinitionRegistry.removeAlias(null));

    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ALIAS_SHOULD_NOT_BE_NULL);
  }

  @Test
  @DisplayName("isAlias existing alias")
  @Order(8)
  void given_Alias_When_IsAliasExistingAlias_Then_CheckIsAliasReturnsTrue() {
    simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME_1, TEST_ALIAS_1);

    assertThat(simpleBeanDefinitionRegistry.isAlias(TEST_ALIAS_1))
        .as("Alias TEST_ALIAS_1 should exist")
        .isTrue();
  }

  @Test
  @DisplayName("isAlias non-existing alias")
  @Order(9)
  void given_Alias_When_IsAliasNonExistingAlias_Then_CheckIsAliasReturnsFalse() {
    simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME_1, TEST_ALIAS_1);

    assertThat(simpleBeanDefinitionRegistry.isAlias(TEST_ALIAS))
        .as("Alias TEST_ALIAS should not exist")
        .isFalse();
  }

  @Test
  @DisplayName("isAlias alias with null name")
  @Order(10)
  void given_NullAlias_When_IsAliasNullAlias_Then_ThrowNoSuchBeanDefinitionException() {
    Exception actualException = catchException(() -> simpleBeanDefinitionRegistry.isAlias(null));

    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ALIAS_SHOULD_NOT_BE_NULL);
  }

  @Test
  @DisplayName("Get aliases for bean name")
  @Order(11)
  void given_BeanName_when_GetAliases_Then_GetAllAliasesForBeanName() {
    assertThat(simpleBeanDefinitionRegistry.getAliases(TEST_BEAN_NAME)).isEmpty();

    simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME, TEST_ALIAS_1);
    simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME, TEST_ALIAS_2);

    assertThat(simpleBeanDefinitionRegistry.getAliases(TEST_BEAN_NAME)).containsExactlyInAnyOrder(
        TEST_ALIAS_1, TEST_ALIAS_2);
  }

  @Test
  @DisplayName("Get aliases for null bean name")
  @Order(12)
  void given_NullBeanName_when_GetAliases_Then_ThrowNoSuchBeanDefinitionException() {
    Exception actualException = catchException(() -> simpleBeanDefinitionRegistry.getAliases(null));

    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ALIAS_SHOULD_NOT_BE_NULL);
  }

  @Test
  @DisplayName("Register bean definition")
  @Order(13)
  void given_BeanNameAndBeanDefinition_When_BeanDefinitionNotRegister_Then_RegisterBeanDefinition() {
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME, BEAN_DEFINITION_MOCK);
    assertThat(simpleBeanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME))
        .isEqualTo(BEAN_DEFINITION_MOCK);
  }

  @Test
  @DisplayName("Register duplicate bean definition")
  @Order(14)
  void given_BeanNameAndBeanDefinition_When_BeanDefinitionRegisterDuplicate_Then_ThrowBeanDefinitionDuplicateException() {
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME, BEAN_DEFINITION_MOCK);

    Exception actualException = catchException(
        () -> simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME,
            BEAN_DEFINITION_MOCK_2));

    assertThat(actualException)
        .isInstanceOf(BeanDefinitionDuplicateException.class)
        .hasMessage(CANNOT_REGISTER_DUPLICATE_BEAN_DEFINITION_MESSAGE.formatted(TEST_BEAN_NAME));
  }

  @Test
  @DisplayName("Register bean definition with null name")
  @Order(15)
  void given_BeanNameAndBeanDefinition_When_RegisterBeanDefinitionWithNullName_Then_ThrowNoSuchBeanDefinitionException() {
    Exception actualException = catchException(
        () -> simpleBeanDefinitionRegistry.registerBeanDefinition(null, BEAN_DEFINITION_MOCK));

    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(BEAN_NAME_SHOULD_NOT_BE_NULL);
  }

  @Test
  @DisplayName("Register bean definition when bean definition is null")
  @Order(16)
  void given_BeanNameAndBeanDefinition_When_RegisterNullBeanDefinition_Then_ThrowNoSuchBeanDefinitionException() {
    Exception actualException = catchException(
        () -> simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME, null));

    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(BEAN_DEFINITION_SHOULD_NOT_BE_NULL_MESSAGE);
  }

  @Test
  @DisplayName("Remove bean definition")
  @Order(17)
  void given_BeanName_When_RemoveBeanDefinition_Then_RegistryNotContainBean() {
//    BeanDefinition beanDefinitionMock1 = Mockito.mock(BeanDefinition.class);
//    BeanDefinition beanDefinitionMock2 = Mockito.mock(BeanDefinition.class);
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_1, BEAN_DEFINITION_MOCK);
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_2, BEAN_DEFINITION_MOCK_2);

    assertThat(simpleBeanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME_1))
        .as("Bean definition with name TEST_BEAN_NAME_1 should not be null")
        .isNotNull();
    simpleBeanDefinitionRegistry.removeBeanDefinition(TEST_BEAN_NAME_1);
    assertThat(simpleBeanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME_1))
        .as("Bean definition with name TEST_BEAN_NAME_1 should be null after removing")
        .isNull();
    assertThat(simpleBeanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME_2))
        .as("Bean definition with name TEST_BEAN_NAME_2 should not be null")
        .isNotNull();
  }

  @Test
  @DisplayName("Remove non-existing bean definition")
  @Order(18)
  void given_BeanName_When_RemoveNonExistentBeanDefinition_Then_RegistryNotChange() {
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_1, BEAN_DEFINITION_MOCK);
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_2, BEAN_DEFINITION_MOCK_2);

    simpleBeanDefinitionRegistry.removeBeanDefinition(TEST_BEAN_NAME);
    assertThat(simpleBeanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME_1))
        .as("Bean definition with name TEST_BEAN_NAME_1 should not be null")
        .isNotNull();
    assertThat(simpleBeanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME_2))
        .as("Bean definition with name TEST_BEAN_NAME_2 should not be null")
        .isNotNull();
  }

  @Test
  @DisplayName("Remove bean definition with null name")
  @Order(19)
  void given_NullBeanName_When_RemoveBeanDefinition_Then_ThrowNoSuchBeanDefinitionException() {
    Exception actualException = catchException(
        () -> simpleBeanDefinitionRegistry.removeBeanDefinition(null));

    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(BEAN_NAME_SHOULD_NOT_BE_NULL);
  }

  @Test
  @DisplayName("Get bean definition")
  @Order(20)
  void given_BeanName_When_GetBeanDefinition_Then_GetBeanDefinition() {
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_1, BEAN_DEFINITION_MOCK);
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_2, BEAN_DEFINITION_MOCK_2);

    assertThat(simpleBeanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME))
        .as("Bean definition with name TEST_BEAN_NAME should be null")
        .isNull();
    assertThat(simpleBeanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME_1))
        .isEqualTo(BEAN_DEFINITION_MOCK);
    assertThat(simpleBeanDefinitionRegistry.getBeanDefinition(TEST_BEAN_NAME_2))
        .isEqualTo(BEAN_DEFINITION_MOCK_2);
  }

  @Test
  @DisplayName("Get bean definition with null name")
  @Order(21)
  void given_NullBeanName_When_GetBeanDefinition_Then_ThrowNoSuchBeanDefinitionException() {
    Exception actualException = catchException(
        () -> simpleBeanDefinitionRegistry.getBeanDefinition(null));

    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(BEAN_NAME_SHOULD_NOT_BE_NULL);
  }

  @Test
  @DisplayName("Contains existing bean definition")
  @Order(22)
  void given_BeanName_When_ContainsExistingBeanDefinition_Then_CheckContainsBeanDefinitionReturnsTrue() {
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_1, BEAN_DEFINITION_MOCK);
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_2, BEAN_DEFINITION_MOCK);

    assertThat(simpleBeanDefinitionRegistry.containsBeanDefinition(TEST_BEAN_NAME_1))
        .as("BeanDefinitionRegistry should contains bean with name TEST_BEAN_NAME_1")
        .isTrue();
    assertThat(simpleBeanDefinitionRegistry.containsBeanDefinition(TEST_BEAN_NAME_2))
        .as("BeanDefinitionRegistry should contains bean with name TEST_BEAN_NAME_2")
        .isTrue();
  }

  @Test
  @DisplayName("Contains non-existing bean definition")
  @Order(23)
  void given_BeanName_When_ContainsNonExistingBeanDefinition_Then_CheckContainsBeanDefinitionReturnsFalse() {
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_1, BEAN_DEFINITION_MOCK);
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_2, BEAN_DEFINITION_MOCK);

    assertThat(simpleBeanDefinitionRegistry.containsBeanDefinition(TEST_BEAN_NAME))
        .as("BeanDefinitionRegistry should not contains bean with name TEST_BEAN_NAME")
        .isFalse();
  }

  @Test
  @DisplayName("Contains bean definition with null name")
  @Order(24)
  void given_NullBeanName_When_ContainsBeanDefinition_Then_ThrowNoSuchBeanDefinitionException() {
    Exception actualException = catchException(
        () -> simpleBeanDefinitionRegistry.containsBeanDefinition(null));

    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(BEAN_NAME_SHOULD_NOT_BE_NULL);
  }

  @Test
  @DisplayName("Get registered bean definition names")
  @Order(25)
  void when_RegisterBeanDefinitions_Then_GetRegisteredBeanDefinitionNames() {
    assertThat(simpleBeanDefinitionRegistry.getBeanDefinitionNames()).isEmpty();

    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_1, BEAN_DEFINITION_MOCK);
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_2, BEAN_DEFINITION_MOCK);

    assertThat(simpleBeanDefinitionRegistry.getBeanDefinitionNames())
        .containsExactlyInAnyOrder(TEST_BEAN_NAME_1, TEST_BEAN_NAME_2);
  }

  @Test
  @DisplayName("Get registered bean definition count after register bean definition")
  @Order(26)
  void when_RegisterBeanDefinitions_Then_GetRegisteredBeanDefinitionCount() {
    assertThat(simpleBeanDefinitionRegistry.getBeanDefinitionCount()).isZero();

    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME, BEAN_DEFINITION_MOCK);

    assertThat(simpleBeanDefinitionRegistry.getBeanDefinitionCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("Get registered bean definition count after remove bean definition")
  @Order(27)
  void when_RemoveBeanDefinitions_Then_GetRegisteredBeanDefinitionCount() {
    assertThat(simpleBeanDefinitionRegistry.getBeanDefinitionCount()).isZero();

    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME, BEAN_DEFINITION_MOCK);
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_1, BEAN_DEFINITION_MOCK);
    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME_2, BEAN_DEFINITION_MOCK);
    simpleBeanDefinitionRegistry.removeBeanDefinition(TEST_BEAN_NAME);

    assertThat(simpleBeanDefinitionRegistry.getBeanDefinitionCount()).isEqualTo(2);
  }

  @Test
  @DisplayName("isBeanNameInUse with used bean name")
  @Order(28)
  void given_BeanName_When_UsedBeanName_Then_IsBeanNameInUseReturnsTrue() {
    simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME_FOR_ALIAS, TEST_ALIAS);

    assertThat(simpleBeanDefinitionRegistry.isBeanNameInUse(TEST_ALIAS))
        .as("Alias TEST_ALIAS should be used as bean name")
        .isTrue();

    simpleBeanDefinitionRegistry.registerBeanDefinition(TEST_BEAN_NAME,
        BEAN_DEFINITION_MOCK);

    assertThat(simpleBeanDefinitionRegistry.isBeanNameInUse(TEST_BEAN_NAME))
        .as("Bean TEST_BEAN_NAME should be used as bean name")
        .isTrue();
  }

  @Test
  @DisplayName("isBeanNameInUse with non-used bean name")
  @Order(29)
  void given_BeanName_When_NonUsedBeanName_Then_IsBeanNameInUseReturnsFalse() {
    simpleBeanDefinitionRegistry.registerAlias(TEST_BEAN_NAME_FOR_ALIAS, TEST_ALIAS);

    assertThat(simpleBeanDefinitionRegistry.isBeanNameInUse(TEST_BEAN_NAME_FOR_ALIAS))
        .as("Bean TEST_BEAN_NAME_FOR_ALIAS should not be used as bean name")
        .isFalse();

    assertThat(simpleBeanDefinitionRegistry.isBeanNameInUse(TEST_BEAN_NAME))
        .as("Bean TEST_BEAN_NAME should not be used as bean name")
        .isFalse();
  }

  @Test
  @DisplayName("isBeanNameInUse with null bean name")
  @Order(30)
  void given_NullBeanName_When_IsBeanNameInUseNullBeanName_Then_ThrowNoSuchBeanDefinitionException() {
    Exception actualException = catchException(
        () -> simpleBeanDefinitionRegistry.isBeanNameInUse(null));

    assertThat(actualException)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(BEAN_NAME_SHOULD_NOT_BE_NULL);
  }
}