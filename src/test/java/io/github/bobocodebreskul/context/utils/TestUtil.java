package io.github.bobocodebreskul.context.utils;

import io.github.bobocodebreskul.context.exception.AmbiguousHttpAnnotationException;
import io.github.bobocodebreskul.context.exception.DuplicatePathException;
import io.github.bobocodebreskul.server.annotations.Delete;
import io.github.bobocodebreskul.server.annotations.ErrorHandlerController;
import io.github.bobocodebreskul.server.annotations.ExceptionHandler;
import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.Head;
import io.github.bobocodebreskul.server.annotations.Post;
import io.github.bobocodebreskul.server.annotations.Put;
import io.github.bobocodebreskul.server.annotations.RequestMapping;
import io.github.bobocodebreskul.server.annotations.RestController;
import io.github.bobocodebreskul.server.enums.RequestMethod;
import jakarta.servlet.http.HttpServletRequest;

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

  public static Object getErrorHandlerControllerWithNotAnnotatedMethods() {
    return new ErrorHandlerControllerWithNotAnnotatedMethods();
  }

  public static Object getErrorHandlerControllerWithAnnotatedPrivateMethods() {
    return new ErrorHandlerControllerWithAnnotatedPrivateMethods();
  }

  public static Object getErrorHandlerControllerWithAnnotatedPrivateAndPublicMethods() {
    return new ErrorHandlerControllerWithAnnotatedPrivateAndPublicMethods();
  }

  public static Object getErrorHandlerControllerWithAnnotatedMethodParamHasNoArguments() {
    return new ErrorHandlerControllerWithAnnotatedMethodParamHasNoArguments();
  }

  public static Object getErrorHandlerControllerWithAnnotatedMethodParamHasMoreThen2Arguments() {
    return new ErrorHandlerControllerWithAnnotatedMethodParamHasMoreThen2Arguments();
  }

  public static Object getErrorHandlerControllerWithAnnotatedMethodParamHas2ExceptionTypeArguments() {
    return new ErrorHandlerControllerWithAnnotatedMethodParamHas2ExceptionTypeArguments();
  }

  public static Object getErrorHandlerControllerWithAnnotatedMethodParamHas2HttpServletRequestArguments() {
    return new ErrorHandlerControllerWithAnnotatedMethodParamWithoutHttpServletRequestArguments();
  }

  public static Object getCorrectErrorHandlerController() {
    return new CorrectErrorHandlerController();
  }

  public static Object getErrorHandlerControllerWithAnnotatedMethodParamWithDuplicateErrorHandlers() {
    return new ErrorHandlerControllerWithAnnotatedMethodParamWithDuplicateErrorHandlers();
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

  @ErrorHandlerController
  static class ErrorHandlerControllerWithNotAnnotatedMethods {
    public String hello(){
      return "Hello";
    }
  }

  @ErrorHandlerController
  static class ErrorHandlerControllerWithAnnotatedPrivateMethods {
    @ExceptionHandler
    private String hello(){
      return "Hello";
    }
  }

  @ErrorHandlerController
  static class ErrorHandlerControllerWithAnnotatedPrivateAndPublicMethods {
    @ExceptionHandler
    private String hello(AmbiguousHttpAnnotationException ex){
      return "Hello";
    }

    @ExceptionHandler
    public String hi(RuntimeException ex){
      return "hi";
    }
  }

  @ErrorHandlerController
  static class ErrorHandlerControllerWithAnnotatedMethodParamHasNoArguments {
    @ExceptionHandler
    public String hello(){
      return "Hello";
    }
  }

  @ErrorHandlerController
  static class ErrorHandlerControllerWithAnnotatedMethodParamHasMoreThen2Arguments {
    @ExceptionHandler
    public String hello(RuntimeException ex, RuntimeException ex2, RuntimeException ex3){
      return "Hello";
    }
  }

  @ErrorHandlerController
  static class ErrorHandlerControllerWithAnnotatedMethodParamHas2ExceptionTypeArguments {
    @ExceptionHandler
    public String hello(RuntimeException ex, RuntimeException ex2){
      return "Hello";
    }
  }

  @ErrorHandlerController
  static class ErrorHandlerControllerWithAnnotatedMethodParamWithoutHttpServletRequestArguments {
    @ExceptionHandler
    public String hello(RuntimeException ex, String str){
      return "Hello";
    }
  }

  @ErrorHandlerController
  static class CorrectErrorHandlerController {
    @ExceptionHandler
    public String hello(AmbiguousHttpAnnotationException ex, HttpServletRequest req) {
      return "Hello";
    }

    @ExceptionHandler
    public String golang(HttpServletRequest req, DuplicatePathException ex) {
      return "golang";
    }

    @ExceptionHandler
    public String hi(RuntimeException ex){
      return "hi";
    }
  }

  @ErrorHandlerController
  static class ErrorHandlerControllerWithAnnotatedMethodParamWithDuplicateErrorHandlers {
    @ExceptionHandler
    public String hello(RuntimeException ex){
      return "Hello";
    }

    @ExceptionHandler
    public String hi(RuntimeException ex){
      return "Hi";
    }
  }
}
