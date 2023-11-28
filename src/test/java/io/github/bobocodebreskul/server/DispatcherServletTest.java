package io.github.bobocodebreskul.server;

import java.util.Map;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class DispatcherServletTest {

  @InjectMocks
  private DispatcherServlet dispatcherServlet;
  @Mock
  private Map<String, Map<String, ControllerMethod>> pathToControllerMethod;

  // TODO: write test logic
  // TODO: test doGet
  // TODO: test doGet (unhappy path)
  //  Case 1: when isNull(controllerMethodMap) (2)
  //  Case 2: when isNull(controllerMethod) (3)
  //  Case 3: when isNull(method) (4)
  //  Case 4: when IOException | IllegalAccessException ex (5)
  //  Case 5: when InvocationTargetException ex (6)
  // TODO: test doPost
  // TODO: test doPost (unhappy path)
  // TODO: test doPut
  // TODO: test doPut (unhappy path)
  // TODO: test doDelete
  // TODO: test doDelete (unhappy path)
  // TODO: test doHead
  // TODO: test doHead (unhappy path)
  // TODO: test service

  @Order(1)
  @Test
  public void given_GetRequest_When_DoGet_Then_ShouldSuccessHandleRequest() {
    // TODO: implement test logic
  }

  @Order(2)
  @Test
  public void given_GetRequest_when_controllerMethodMapIsNull_thenReturn404Response() {
    // TODO: implement test logic
  }

  @Order(3)
  @Test
  public void given_GetRequest_when_controllerMethodIsNull_thenReturn404Response() {
    // TODO: implement test logic
  }

  @Order(4)
  @Test
  public void given_GetRequest_when_methodIsNull_thenReturn404Response() {
    // TODO: implement test logic
  }

  @Order(5)
  @Test
  public void given_GetRequest_when_IOExceptionOrIllegalAccessException_thenThrowDispatcherServletException() {
    // TODO: implement test logic
  }

  @Order(6)
  @Test
  public void given_GetRequest_when_InvocationTargetException_thenThrowMethodInvocationException() {
    // TODO: implement test logic
  }

  @Order(7)
  @Test
  public void given_PostRequest_When_DoPost_Then_ShouldSuccessHandleRequest() {
    // TODO: implement test logic
  }
}
