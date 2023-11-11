package io.github.bobocodebreskul.context.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import io.github.bobocodebreskul.context.config.BeanDefinition;
import io.github.bobocodebreskul.context.exception.BeanDefinitionDuplicateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SimpleBeanDefinitionRegistryTest {

  private BeanDefinitionRegistry beanDefinitionRegistry;

  @BeforeEach
  void init() {
    beanDefinitionRegistry = new SimpleBeanDefinitionRegistry();
  }

  @Test
  void givenBeanNameAndBeanDefinitionWhenBeanDefinitionNotRegisterThenRegisterBeanDefinition() {
    BeanDefinition beanDefinitionMock1 = Mockito.mock(BeanDefinition.class);
    beanDefinitionRegistry.registerBeanDefinition("testBeanName1", beanDefinitionMock1);
    assertSame(beanDefinitionMock1, beanDefinitionRegistry.getBeanDefinition("testBeanName1"));
  }

  @Test()
  void givenBeanNameAndBeanDefinitionWhenBeanDefinitionRegisterDuplicateThenThrow() {
    BeanDefinition beanDefinitionMock1 = Mockito.mock(BeanDefinition.class);
    BeanDefinition beanDefinitionMock2 = Mockito.mock(BeanDefinition.class);
    String testBeanName = "testBeanName";
    beanDefinitionRegistry.registerBeanDefinition(testBeanName, beanDefinitionMock1);
    Exception exception = assertThrows(
        BeanDefinitionDuplicateException.class,
        () -> beanDefinitionRegistry.registerBeanDefinition(testBeanName, beanDefinitionMock2));

    String expectedMessage =
        "Cannot registered a beanDefinition with name %s because a beanDefinition with that name is already registered%n".formatted(
            testBeanName);
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void givenBeanNameThenRemoveBeanDefinition() {
    BeanDefinition beanDefinitionMock1 = Mockito.mock(BeanDefinition.class);
    BeanDefinition beanDefinitionMock2 = Mockito.mock(BeanDefinition.class);
    beanDefinitionRegistry.registerBeanDefinition("testBeanName1", beanDefinitionMock1);
    beanDefinitionRegistry.registerBeanDefinition("testBeanName2", beanDefinitionMock2);

    assertNotNull(beanDefinitionRegistry.getBeanDefinition("testBeanName1"));
    beanDefinitionRegistry.removeBeanDefinition("testBeanName");
    beanDefinitionRegistry.removeBeanDefinition("testBeanName1");
    assertNull(beanDefinitionRegistry.getBeanDefinition("testBeanName1"));
    assertNotNull(beanDefinitionRegistry.getBeanDefinition("testBeanName2"));
  }

  @Test
  void givenBeanNameThenGetBeanDefinition() {
    BeanDefinition beanDefinitionMock1 = Mockito.mock(BeanDefinition.class);
    BeanDefinition beanDefinitionMock2 = Mockito.mock(BeanDefinition.class);
    beanDefinitionRegistry.registerBeanDefinition("testBeanName1", beanDefinitionMock1);
    beanDefinitionRegistry.registerBeanDefinition("testBeanName2", beanDefinitionMock2);

    assertNull(beanDefinitionRegistry.getBeanDefinition("testBeanName"));
    assertSame(beanDefinitionMock1, beanDefinitionRegistry.getBeanDefinition("testBeanName1"));
    assertSame(beanDefinitionMock2, beanDefinitionRegistry.getBeanDefinition("testBeanName2"));
  }

  @Test
  void givenBeanNameThenContainsBeanDefinition() {
    BeanDefinition beanDefinitionMock = Mockito.mock(BeanDefinition.class);
    beanDefinitionRegistry.registerBeanDefinition("testBeanName1", beanDefinitionMock);
    beanDefinitionRegistry.registerBeanDefinition("testBeanName2", beanDefinitionMock);

    assertFalse(beanDefinitionRegistry.containsBeanDefinition("testBeanName"));
    assertTrue(beanDefinitionRegistry.containsBeanDefinition("testBeanName1"));
    assertTrue(beanDefinitionRegistry.containsBeanDefinition("testBeanName2"));
  }

  @Test
  void whenRegisterBeanDefinitionsThenGetRegisteredBeanDefinitionNames() {
    assertEquals(0, beanDefinitionRegistry.getBeanDefinitionNames().length);

    BeanDefinition beanDefinitionMock = Mockito.mock(BeanDefinition.class);
    beanDefinitionRegistry.registerBeanDefinition("testBeanName1", beanDefinitionMock);
    beanDefinitionRegistry.registerBeanDefinition("testBeanName2", beanDefinitionMock);
    String[] beanDefinitionNames = beanDefinitionRegistry.getBeanDefinitionNames();

    assertEquals(2, beanDefinitionNames.length);
    assertThat(beanDefinitionNames).contains("testBeanName1", "testBeanName2");
  }

  @Test
  void whenRegisterBeanDefinitionsThenGetRegisteredBeanDefinitionCount() {
    assertEquals(0, beanDefinitionRegistry.getBeanDefinitionCount());

    BeanDefinition beanDefinitionMock = Mockito.mock(BeanDefinition.class);
    beanDefinitionRegistry.registerBeanDefinition("testBeanName", beanDefinitionMock);

    assertEquals(1, beanDefinitionRegistry.getBeanDefinitionCount());
  }
}