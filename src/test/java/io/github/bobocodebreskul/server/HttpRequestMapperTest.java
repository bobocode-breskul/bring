package io.github.bobocodebreskul.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bobocodebreskul.context.exception.RequestsMappingException;
import io.github.bobocodebreskul.server.enums.RequestMethod;
import io.github.bobocodebreskul.server.enums.ResponseStatus;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@TestMethodOrder(OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class HttpRequestMapperTest {

  @Mock
  private ObjectMapper mockedMapper;
  @Mock
  private HttpServletResponse mockedHttpServletResponse;
  @Mock
  private HttpServletRequest mockedHttpServletRequest;
  @Mock
  private ServletInputStream mockedServletInputStream;
  @InjectMocks
  private HttpRequestMapper requestMapper;

  @Test
  @DisplayName("When BringResponse with all correct fields then set correct values to HttpServletResponse")
  @Order(1)
  void given_BringResponseWithAllCorrectFields_When_WriteBringResponseIntoHttpServletResponse_Then_SetValuesToHttpServletResponse()
      throws IOException {
    Map<String, String> headers = Map.of("HeaderKey", "HeaderValue");
    BringResponse<String> bringResponse =
        new BringResponse<>("Hello", headers, ResponseStatus.BAD_REQUEST);
    ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);
    given(mockedHttpServletResponse.getOutputStream()).willReturn(outputStream);

    requestMapper.writeBringResponseIntoHttpServletResponse(mockedHttpServletResponse,
        bringResponse);

    verify(mockedHttpServletResponse, times(1)).setHeader("HeaderKey".toLowerCase(), "HeaderValue");
    verify(mockedHttpServletResponse, times(1)).getOutputStream();
    verify(outputStream, times(1)).print(any());
    verify(mockedHttpServletResponse, times(1)).setStatus(400);
  }

  @Test
  @DisplayName("When BringResponse with byte[] body then set correct body to HttpServletResponse")
  @Order(2)
  void given_BringResponseWithByteArrayBody_When_WriteBringResponseIntoHttpServletResponse_Then_SetCorrectBodyToHttpServletResponse()
      throws IOException {
    BringResponse<byte[]> bringResponse =
        new BringResponse<>(new byte[0], null, ResponseStatus.BAD_REQUEST);
    ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);
    given(mockedHttpServletResponse.getOutputStream()).willReturn(outputStream);

    requestMapper.writeBringResponseIntoHttpServletResponse(mockedHttpServletResponse,
        bringResponse);

    verify(mockedHttpServletResponse, times(1)).getOutputStream();
    verify(outputStream, times(1)).write(any());
  }

  @Test
  @DisplayName("HttpServletResponse throw IOException during body writing then throw RequestsMappingException")
  @Order(3)
  void given_OutputStreamThorow_When_writeBringResponseIntoHttpServletResponse_Then_ThrownRequestsMappingException() throws IOException {
    //data

    String body = "response body";
    String mappedBody = "maped body";
    BringResponse<String> bringResponse = BringResponse.ok(body);
    ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);

    //given
    given(mockedHttpServletResponse.getOutputStream()).willReturn(outputStream);
    doThrow(new IOException()).when(outputStream).print(mappedBody);
    given(mockedMapper.writeValueAsString(body)).willReturn(mappedBody);

    //when
    Exception actualException = catchException(
        () -> requestMapper.writeBringResponseIntoHttpServletResponse(mockedHttpServletResponse,
            bringResponse));

    //verify
    assertThat(actualException)
        .isInstanceOf(RequestsMappingException.class)
        .hasMessage("Failed to write response entity to httpServletResponse");
  }

  @Test
  @DisplayName("When ObjectMapper#writeValueAsString(body) throw JsonProcessingException then throw RequestsMappingException")
  @Order(4)
  @SneakyThrows
  void given_ObjectMapperThrowsException_When_writeBringResponseIntoHttpServletResponse_Then_ShouldThrowRequestsMappingException() {
    when(mockedMapper.writeValueAsString(any()))
        .thenThrow(new JsonProcessingException("Json Error") {
        });

    Exception actualException = catchException(
        () -> requestMapper.writeBringResponseIntoHttpServletResponse(mockedHttpServletResponse,
            new BringResponse("500")));

    assertThat(actualException)
        .isInstanceOf(RequestsMappingException.class)
        .hasMessage("Failed to write body as String");
  }


  @Test
  @DisplayName("when we fill all BringRequest fields from HttpServletRequest when body exists")
  @Order(5)
  @SneakyThrows
  void given_HttpServletRequestHasAllFields_When_mapHttpServletRequestOnBringRequestEntity_Then_returnFilledBringRequest() {
    //data
    String body = "response body";
    String method = "POST";
    String headerName1 = "headername1";
    String headerName2 = "headername2";
    String headerContentType = "Content-Type";
    String headerValue1 = "headerValue1";
    String headerValue2 = "headerValue2";
    String headerValueContentType = "text/plain";
    String stringUrl = "100.90.90.99";
    var expectedURI = URI.create(stringUrl);
    var headerNames = Collections.enumeration(List.of(headerName1, headerName2, headerContentType));

    //given
    given(mockedHttpServletRequest.getMethod()).willReturn(method);
    given(mockedHttpServletRequest.getRequestURI()).willReturn(stringUrl);
    given(mockedHttpServletRequest.getHeaderNames()).willReturn(headerNames);
    given(mockedHttpServletRequest.getHeader(headerName1)).willReturn(headerValue1);
    given(mockedHttpServletRequest.getHeader(headerName2)).willReturn(headerValue2);
    given(mockedHttpServletRequest.getHeader(headerContentType)).willReturn(headerValueContentType);
    given(mockedHttpServletRequest.getInputStream()).willReturn(mockedServletInputStream);
    given(mockedMapper.readValue(anyString(), any(Class.class))).willReturn(body);

    //when
    var actual = requestMapper.mapHttpServletRequestOnBringRequestEntity(mockedHttpServletRequest,
        String.class);

    //verify
    assertThat(actual.getRequestMethod()).isEqualTo(RequestMethod.POST);
    assertThat(actual.getUrl()).isEqualTo(expectedURI);
    assertThat(actual.getHeadersNames()).containsExactlyInAnyOrder(headerName1, headerName2);
    assertThat(actual.getHeader(headerName1)).isEqualTo(headerValue1);
    assertThat(actual.getHeader(headerName2)).isEqualTo(headerValue2);
    assertThat(actual.getHeader(headerContentType)).isEqualTo(headerValueContentType);
    assertThat(actual.getBody()).isEqualTo(body);
  }

  @Test
  @DisplayName("When we fill all BringResponse fields from HttpServletRequest when body not exists")
  @Order(6)
  @SneakyThrows
  void given_BringResponseHasAllFieldsExceptBody_When_writeBringResponseIntoHttpServletResponse_Then_ShouldNotSetBody() {
    Map<String, String> headers = Map.of("HeaderKey", "HeaderValue");
    BringResponse<String> bringResponse =
        new BringResponse<>(null, headers, ResponseStatus.BAD_REQUEST);

    requestMapper.writeBringResponseIntoHttpServletResponse(mockedHttpServletResponse,
        bringResponse);

    verify(mockedHttpServletResponse, times(1)).setHeader("headerkey", "HeaderValue");
    verify(mockedHttpServletResponse, times(1)).setStatus(400);
    verify(mockedHttpServletResponse, times(0)).getOutputStream();
  }

  @Test
  @DisplayName("When objectMapper.readValue(stringBody, bodyType) throws IOException then should throw RequestsMappingException")
  @Order(7)
  @SneakyThrows
  void given_ObjectMapperThrowsIOExceptionOnReadValue_When_mapHttpServletRequestOnBringRequestEntity_Then_ShouldThrowRequestsMappingException() {
    when(mockedMapper.readValue(anyString(), any(Class.class)))
        .thenThrow(new JsonProcessingException("Json Error") {
        });

    given(mockedHttpServletRequest.getMethod()).willReturn("GET");
    given(mockedHttpServletRequest.getRequestURI()).willReturn("/test");

    var headerNames = Collections.enumeration(List.of("headerName", "Content-Type"));

    given(mockedHttpServletRequest.getHeaderNames()).willReturn(headerNames);
    given(mockedHttpServletRequest.getHeader("headerName")).willReturn("headerValue");
    given(mockedHttpServletRequest.getHeader("Content-Type")).willReturn("text/plain");
    given(mockedHttpServletRequest.getInputStream()).willReturn(mockedServletInputStream);

    Exception actualException = catchException(
        () -> requestMapper.mapHttpServletRequestOnBringRequestEntity(
            mockedHttpServletRequest,
            String.class));

    assertThat(actualException)
        .isInstanceOf(RequestsMappingException.class)
        .hasMessage("Failed to map HttpServletRequest body into object.");
  }
}