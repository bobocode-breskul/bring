package io.github.bobocodebreskul.context.registry;


import io.github.bobocodebreskul.context.config.AnnotatedGenericBeanDefinition;
import io.github.bobocodebreskul.context.exception.NoSuchBeanDefinitionException;
import jdk.jfr.Description;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BringContainerTest {

  private static final String TEST_BEAN_NAME_1 = "TEST_BEAN_NAME_1";

  @InjectMocks
  private BringContainer objectFactory;

  @Mock
  private BeanDefinitionRegistry beanDefinitionRegistry;

  @Test
  @Description("Create single bean by bean name")
  @Order(1)
  public void givenBeanName_WhenBeanIsRegistered_ThenReturnBean() {
    // data
    String inputBeanName = TEST_BEAN_NAME_1;
    // given
    BeanClass1 expectedBean = new BeanClass1();
    AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(BeanClass1.class);
    beanDefinition.setName(inputBeanName);

    given(beanDefinitionRegistry.getBeanDefinition(inputBeanName)).willReturn(beanDefinition);
    // when
    Object actualBean = objectFactory.getBean(inputBeanName);
    // then
    assertThat(actualBean).isInstanceOf(BeanClass1.class);
    assertThat(actualBean)
            .usingRecursiveComparison()
            .isEqualTo(expectedBean);
  }

  @Test
  @Description("Create and return same bean when called twice by same bean name")
  @Order(2)
  public void givenBeanName_WhenBeanIsRegistered_ThenOnSecondTimeReturnSameBeanAgain() {
    // data
    String inputBeanName = TEST_BEAN_NAME_1;
    // given
    AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(BeanClass1.class);
    beanDefinition.setName(inputBeanName);

    given(beanDefinitionRegistry.getBeanDefinition(inputBeanName)).willReturn(beanDefinition);
    // when
    Object actualBean1 = objectFactory.getBean(inputBeanName);
    Object actualBean2 = objectFactory.getBean(inputBeanName);
    // then
    assertThat(actualBean1).isSameAs(actualBean2);
  }

  @Test
  @Description("Throw NoSuchBeanDefinitionException when take bean without bean definition by name")
  @Order(3)
  public void given_BeanName_When_BeanNameNotRegistered_Then_ThrowNoSuchBeanDefinitionException() {
    assertThatThrownBy(() -> objectFactory.getBean(TEST_BEAN_NAME_1))
            .isInstanceOf(NoSuchBeanDefinitionException.class);
  }

  @Test
  @Description("Throw UnsupportedOperationException when take bean without bean definition by class")
  @Order(4)
  public void givenBeanClass_WhenBeanIsRegistered_ThenThrowUnsupportedOperationException() {
    // when
    assertThatThrownBy(() -> objectFactory.getBean(Object.class))
            .isInstanceOf(UnsupportedOperationException.class);
  }

  public static class BeanClass1 {
  }
}
