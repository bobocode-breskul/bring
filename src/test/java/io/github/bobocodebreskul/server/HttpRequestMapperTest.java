package io.github.bobocodebreskul.server;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
class HttpRequestMapperTest {
  // todo test BringResponse with all fields then we set correct values to HttpServletResponse
  @Test
  @DisplayName("")
  @Order(1)
  void given_MethodName_When_getByName_Then_ShouldReturnRequestMethod_1(){

  }
  // todo test HttpServletResponse throw IOException during body writing then throw RequestsMappingException
  @Test
  @DisplayName("")
  @Order(2)
  void given_MethodName_When_getByName_Then_ShouldReturnRequestMethod_2(){
    //data
    //given
    //when
    //verify
  }
  // todo test when ObjectMapper#writeValueAsString(body) throw JsonProcessingException then throw RequestsMappingException
  @Test
  @DisplayName("")
  @Order(3)
  void given_MethodName_When_getByName_Then_ShouldReturnRequestMethod_3(){
    //data
    //given
    //when
    //verify
  }
  // todo test when we fill all BringRequest fields from HttpServletRequest when body exists
  @Test
  @DisplayName("")
  @Order(4)
  void given_MethodName_When_getByName_Then_ShouldReturnRequestMethod_4(){
    //data
    //given
    //when
    //verify
  }
  // todo test when we fill all BringRequest fields from HttpServletRequest when body not exists
  @Test
  @DisplayName("")
  @Order(5)
  void given_MethodName_When_getByName_Then_ShouldReturnRequestMethod_5(){
    //data
    //given
    //when
    //verify
  }
  // todo test when objectMapper.readValue(stringBody, bodyType) then throws IOException
  @Test
  @DisplayName("")
  @Order(6)
  void given_MethodName_When_getByName_Then_ShouldReturnRequestMethod_6(){
    //data
    //given
    //when
    //verify
  }
}