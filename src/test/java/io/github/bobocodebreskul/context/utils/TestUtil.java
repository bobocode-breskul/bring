package io.github.bobocodebreskul.context.utils;

import io.github.bobocodebreskul.server.annotations.Delete;
import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.Head;
import io.github.bobocodebreskul.server.annotations.Post;
import io.github.bobocodebreskul.server.annotations.Put;
import io.github.bobocodebreskul.server.annotations.RequestMapping;
import io.github.bobocodebreskul.server.annotations.RestController;
import io.github.bobocodebreskul.server.enums.RequestMethod;

public class TestUtil {

  public static Object retrieveControllerWithoutHttpAnnotationsMethod() {
    return new ControllerWithNotAnnotatedMethods();
  }

  public static Object retrieveControllerWithBothHttpAnnotationsMethodAndNot() {
    return new ControllerWithBothHttpAnnotationsMethodAndNot();
  }

  public static Object retrieveControllerWithSeveralHttpAnnotationsMethod() {
    return new ControllerWithSeveralAnnotatedMethods();
  }

  public static Object retrieveControllerWithInvalidPathAnnotationsMethod() {
    return new ControllerWithInvalidPathAnnotatedMethods();
  }

  public static Object retrieveValidControllerWithValidMethods() {
    return new ValidControllerWithValidMethods();
  }

  public static Object retrieveValidControllerWithRequestMappingAnnotatedMethod() {
    return new ControllerWithRequestMappingAnnotatedMethod();
  }

  public static Object retrieveValidControllerWithRequestMappingAnnotatedMethodWithoutHTTPMethod() {
    return new ControllerWithRequestMappingAnnotatedMethodWithoutHTTPMethod();
  }

  public static Object retrieveControllerWithAnnotatedPrivateMethods() {
    return new ControllerWithAnnotatedPrivateMethods();
  }

  public static Object retrieveControllerWithMethodsWithSameHttpMethodAndPath() {
    return new ControllerWithMethodsWithSameHttpMethodAndPath();
  }

  public static Object retrieveControllerWithMethodsWithSameHttpMethodAndPathWhenUseRequestMapping() {
    return new ControllerWithMethodsWithSameHttpMethodAndPathWhenUseRequestMapping();
  }

  public static Object retrieveControllerMethodWithHttpAnnotationAndRequestMapping() {
    return new ControllerMethodWithHttpAnnotationAndRequestMapping();
  }

  @RestController
  static class ControllerWithNotAnnotatedMethods {
    public String hello(){
      return "Hello";
    }
  }

  @RestController
  static class ControllerWithSeveralAnnotatedMethods {
    @Get("/testget")
    @Post("/testpost")
    public String hello(){
      return "Hello";
    }
  }

  @RestController
  static class ControllerWithBothHttpAnnotationsMethodAndNot {
    @Post("/testpost")
    public String hello(){
      return "Hello";
    }

    public String goodBye() {
      return "Good bye";
    }
  }

  @RestController
  static class ControllerMethodWithHttpAnnotationAndRequestMapping {
    @RequestMapping("/testrequestmapping")
    @Get("/testget")
    public String hello(){
      return "Hello";
    }
  }

  @RequestMapping("test")
  @RestController
  static class ControllerWithInvalidPathAnnotatedMethods {
    @Get("/get")
    public String hello(){
      return "Hello";
    }
  }

  @RequestMapping("/test")
  @RestController
  static class ValidControllerWithValidMethods {
    @Get("/test")
    public String helloGet(){
      return "Hello";
    }

    @Post("/test")
    public String helloPost(){
      return "Hello";
    }

    @Delete("/test")
    public String helloDelete(){
      return "Hello";
    }

    @Head("/test")
    public String helloHead(){
      return "Hello";
    }

    @Put("/test")
    public String helloPut(){
      return "Hello";
    }
  }

  @RestController
  static class ControllerWithAnnotatedPrivateMethods {
    @Get("/get")
    public String hello(){
      return "Hello";
    }

    @Head("/get")
    private String helloHead(){
      return "Hello";
    }

    @Head("/get")
    protected String helloPost(){
      return "Hello";
    }

    @Put("/get")
    String helloPut(){
      return "Hello";
    }
  }

  @RequestMapping("/test")
  @RestController
  static class ControllerWithRequestMappingAnnotatedMethod {
    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public String hello(){
      return "Hello";
    }
  }

  @RequestMapping("/test")
  @RestController
  static class ControllerWithRequestMappingAnnotatedMethodWithoutHTTPMethod {
    @RequestMapping("/get")
    public String hello(){
      return "Hello";
    }
  }

  @RestController
  static class ControllerWithMethodsWithSameHttpMethodAndPath {
    @Get("/test")
    public String helloGet() {
      return "Hello";
    }

    @Get("/test")
    public String helloGet1() {
      return "Hello";
    }
  }

  @RestController
  static class ControllerWithMethodsWithSameHttpMethodAndPathWhenUseRequestMapping {
    @Post("/post")
    public String helloGet() {
      return "Hello";
    }

    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public String helloGet1() {
      return "Hello";
    }
  }
}
