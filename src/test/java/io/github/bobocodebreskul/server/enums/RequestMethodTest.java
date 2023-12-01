package io.github.bobocodebreskul.server.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

class RequestMethodTest {

  @ParameterizedTest
  @ValueSource(strings = {"Get", "heaD", "pOst", "DELETE", "put"})
  void given_MethodName_When_getByName_Then_ShouldReturnCorrectRequestMethod(String methodName) {
    String result = RequestMethod.getByName(methodName).name();
    assertThat(result).isEqualToIgnoringCase(methodName);
  }
}
