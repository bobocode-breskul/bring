package io.github.bobocodebreskul.context.registry;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BeanDefinitionValidatorTest {


  @Mock
  private BeanDefinitionRegistry definitionRegistry;


  @Test
  void test(){
   String test =  """
        The dependencies of some of the beans form a cycle:
        ┌─────┐
        |  %s defined in file [%s]
        ↑     ↓
        |  %s defined in file [%s]
        ↑     ↓
        |  %s defined in file [%s]
        └─────┘
        """.formatted(A.class.getName(), getFileLocation(A.class));
  }

  private String getFileLocation(Class<?> beanClass) {
    return Path.of(beanClass.getName()).toAbsolutePath().toString()
        .replace(".", "/")
        .concat(".java");
  }

  static class  A {
    private final B b;

    public A(B b) {
      this.b = b;
    }
  }
  static class B{
    private final C c;

    public B(C c) {
      this.c = c;
    }
  }
  static class C{
    private final A a;

    public C(A a) {
      this.a = a;
    }
  }
}
