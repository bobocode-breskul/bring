


# Bring Framework ![Bobocode Breskul Team](https://img.shields.io/badge/Bobocode%20Breskul%20Team-8A2BE2) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

## What is Bring Framework?
Bring Framework is a custom lightweight and flexible web framework for building Java-based web applications that combines HTTP request handling with a Dependency Injection container.
It uses IoC (Inversion of Control) container, which allows to create objects and it's dependencies in declarative way.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Introduction

Bring Framework is designed to simplify the development of web applications in Java. It follows the Model-View-Controller (MVC) architecture and integrates with the Inversion of Control (IoC) container for dependency injection.

## Features

- **Dependency injection container**:
    - fail fast: application won't run if badly configured - dependency bean definition not found, cycle bean dependency, invalid bean naming, multiple bean constructors, multiple bean @Autowired constructors, etc.
- **Bean declaration**:
    - by annotation
    - by java configuration (partially, singletone only)

- **Bean dependencies processing**:
    - injection by type:
        - direct class
        - interface usage
        - inject by name
        - inject by qualifier
        - inject by primary
        - inject by autowired constructor parameter
- **Injection strategy**: by constructor only
- **Bean scope options**:
    - singletone
    - prototype
- **Embedded Tomcat based HTTP server**
- **Dispatcher mapping**:
    - RestControllers as IoC beans
    - RequestMapping for class
    - CRUD mapping for methods (GET, POST, PUT, DELETE, HEAD)
- **Controller method parameters injection (combination)**:
    - HttpServletRequest/HttpServletResponse
    - RequestBody annotation as plain text or JSON
    - RequestParam annotation
    - user friendly request wrapper BringRequest object as plain text or JSON
- **Controller method response handling**:
    - any raw object as plain text or JSON
    - user friendly response wrapper BringResponse object as bytes, plain text or JSON
- **Customisation of exception handling**: using customizable exception handling advice
- **Properties application configuration**: control application configuration with application properties
- **Logging controller**: control logging level with application properties
- **Controlling banner**: enable/disable banner with application properties

## Getting Started

Follow these steps to integrate Bring Framework into your project:

1. Add the following Maven dependency:

```xml
<dependency>
  <groupId>io.github.bobocode-breskul</groupId>
  <artifactId>bring</artifactId>
  <version>2.0</version>
</dependency>
```

2. Configure your application context.
   Create application.properties file in the resources folder.
   Configuration has next options:

```properties
server.port=<SERVER PORT NUMBER>
banner=<BOOLEAN>
logging=<LOGGING LEVEL [INFO|DEBUG|WARN|ERROR]>
```

## Usage

### Example 1: Launch HTTP application
Read all beans in the package where launched class is located and launch HTTP application. Main application class should be in the package, non-package (default) is not supported.

```java
@BringComponentScan  
public class App {  
    public static void main(String[] args) {
        BringContainer.run(App.class);
  }
}
```
### Example 2: Bean declaration
Annotation based bean declaration:
```java
// add component by annotation
@BringComponent
public class NestedBean {

}
```
Java configuration based bean declaration:
```java
// add component by Java configuration class
@BringConfiguration
public class Config {

  @BringBean
  public ObjectMapper getObjectMapper() {
    return new ObjectMapper();
  }

  @BringBean
  public ParentBean getParent(BeanSample beanSample, AnnotatedBean annotatedBean) {
    return new ParentBean(beanSample, annotatedBean);
  }

  @BringBean
  public BeanSample getBeanSample() {
    return new packag.BeanSample();
  }
}
```
### Example 3: Simple dependency injection
Dependencies could be injected only by constructors. If no dependencies defined then default constructor will be used for bean creation.
Dependency injection using simple direct classes:
```java
@BringComponent  
public class CallerService {  
    private final DependencyService dependencyService;  
  
    public CallerService(DependencyService dependencyService) {  
        this.dependencyService = dependencyService;  
    }  
  
    public String doCall(String input) {  
        return dependencyService.doWork(input);  
    }  
}

@BringComponent  
public class DependencyServiceImpl implements DependencyService {  
    public String doWork(String input) {  
        //... do logic  
        return "RESULT";  
    }  
}

public interface DependencyService {  
    String doWork(String input);  
}
```
### Example 4: Handling ambiguous beans creation
If more than one constructor is available in bean then @Autowired annotation should be used to mark the target constructor.
If more than one interface candidate is available, then the dependent bean could be marked as @Primary to indicate that this bean should be used by default. Also, a bean can specify its name in @BringComponent("NAME") and @Qualifier("NAME") could be used on the constructor parameter to indicate the selected bean.
```java
@BringComponent  
public class CallerService {  
    private final DependencyService dependencyServiceOne;  
    private final DependencyService dependencyServiceTwo;  
  
    public CallerService(DependencyService dependencyServiceOne) {  
        this.dependencyServiceOne = dependencyServiceOne;  
        this.dependencyServiceTwo = null;  
    }  
  
    // constructor is used for bean creation using @Autowired  
    @Autowired  
    public CallerService(DependencyService dependencyServiceOne, @Qualifier("ServiceB") DependencyService dependencyServiceTwo) {  
        // set using @Primary  
       this.dependencyServiceOne = dependencyServiceOne;  
       // set using @Qualifier  
       this.dependencyServiceTwo = dependencyServiceTwo;  
    }  
  
    public String doCall(String input) {  
        return dependencyServiceOne.doWork(input);  
    }  
}

@Primary  
@BringComponent("ServiceA")  
public class DependencyServiceA implements DependencyService {  
    public String doWork(String input) {  
        //... do logic  
        return "A " + input;  
    }  
}

@BringComponent("ServiceB")  
public class DependencyServiceB implements DependencyService {  
    @Override  
  public String doWork(String input) {  
      //... do logic  
      return "A " + input;  
  }  
}

public interface DependencyService {  
    String doWork(String input);  
}
```
### Example 5: Defining bean scope:
Define SINGLETON bean (if the value attribute is not set explicitly, the default assumption is that the scope is singleton):
```java
@BringComponent
@Scope
public class MyBean {
}
```
or
```java
@BringComponent
@Scope("singleton")
public class MyBean {
}
```	
Define bean scope prototype:

```java
@BringComponent
@Scope("prototype")
public class MyBean {
}
```
### Example 6: Dispatcher mapping
Let's create our simple CRUD controller.

Create new controller PictureController and add two annotations `@RestController` and `@RequestMapping("/pictures")`, where `/pictures`  web path.

```java 
@RestController  
@RequestMapping("/pictures")  
public class PictureController {}
```

Then let's add new methods that will handle `GET`, `POST`, `PUT`, `DELETE` requests.
```java 
@RestController
@RequestMapping("/pictures")
public class PictureController {
  private final ObjectMapper mapper = new ObjectMapper();
  private final Map<Integer, PictureDto> storage = new HashMap<>();

  @Get
  public BringResponse<JsonNode> getAllPictures() {
    JsonNode node = mapper.valueToTree(storage);
    return BringResponse.ok(node);
  }

  @Post
  public BringResponse<JsonNode> addPicture(@RequestBody PictureDto picture) {
    storage.put(picture.getId(), picture);
    JsonNode node = mapper.valueToTree(picture);

    return new BringResponse<>(null, null, ResponseStatus.CREATED);
  }

  @Put
  public BringResponse<JsonNode> updatePicture(@RequestBody PictureDto picture) {
    storage.put(picture.getId(), picture);
    JsonNode node = mapper.valueToTree(picture);

    return BringResponse.ok(node);
  }

  @Delete
  public BringResponse<Void> removeAllPictures() {
    storage.clear();

    return new BringResponse<>(null, null, ResponseStatus.NO_CONTENT);
  }

  @Head
  public BringResponse<Void> head() {
    return new BringResponse<>(null,
        Map.of("headerkey", "HeaderValue"),
        ResponseStatus.NO_CONTENT);
  }
}
```
### Example 7: Controller method input handling
Controller can handle input in different ways and combinations:
- HttpServletRequest/HttpServletResponse as parameter
- request parameter using @RequestParam annotation
- raw object using @RequestBody annotation (plain text or JSON as input)
- user-friendly request wrapper using BringRequest<> object (plain text or JSON as input), provides URL, HTTP request method, headers, and body
  All mentioned method parameters could be easily combined to achieve maximum results.
  HttpServletRequest and HttpServletResponse method parameter injection:
```java
@Get("/get-servlet-request")  
public void postWithHttpServlet(HttpServletRequest request, HttpServletResponse response) {  
    // do work with request and response  
}
```
Request parameter method injection:
```java
@Get("/get-request-param")  
public void getRequestParam(@RequestParam("name") String name) {  
    // do work with request param  
}
```
Request body method injection:
```java
@Post("/post-request-body")  
public void postRawObject(@RequestBody PersonDto person) {  
    // do work with DTO  
}
```
Request body with HttpServletRequest combination method injection:
```java
@Post("/post-request-body-servlet")  
public void postRawObjectWithHttpServlet(@RequestBody PersonDto person, HttpServletRequest request) {  
    // do work with DTO  
}
```
User-friendly request wrapper using BringRequest<>:
```java
@Post("/post-http-entity")  
public void postEntity(BringRequest<PersonDto> personRequest) {  
    // do work with http entity  
}
```
BringRequest with @RequestMethod annotation combination:
```java
@Post("/post-http-entity-param")  
public void postEntityWithRequestParam(BringRequest<PersonDto> personRequest, @RequestParam("name") String name) {  
    // do work with http entity and param  
}
```
### Example 8: Controller method response handling:
Try out user friendly response wrapper BringResponse object as bytes, plain text or JSON
```java
@RestController
@RequestMapping
public class BaseController {
  private final ObjectMapper mapper = new ObjectMapper();

  @Get("/json")
  public BringResponse<JsonNode> doGetJson() {
    RequestDto dto = new RequestDto();
    dto.setString("Hello world");
    dto.setInteger(42);

    JsonNode node = mapper.valueToTree(dto);
    return BringResponse.ok(node);
  }

  @Get("/text")
  public BringResponse<String> doGetText() {
    RequestDto dto = new RequestDto();
    dto.setString("Hello world");
    dto.setInteger(42);

    return BringResponse.ok(dto.toString());
  }

  @Get("/byte")
  public BringResponse<byte[]> doGetByte() {
    RequestDto dto = new RequestDto();
    dto.setString("Hello world");
    dto.setInteger(42);

    return BringResponse.ok(dto.toString().getBytes());
  }
}
```
### Example 9: Customisation of exception handling
Create your custom exception handler using @ErrorHandlerController and @ExceptionHandler.
```java
@ErrorHandlerController
public class GlobalErrorHandler {

  @ExceptionHandler
  public BringResponse<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
    return new BringResponse<>(ex.getMessage(), Map.of("TestHeader", "TestValue"), 
        ResponseStatus.NOT_FOUND);
  }

  @ExceptionHandler
  public BringResponse<String> handleResourceNotFoundException(SystemEngineException ex, HttpServletRequest req) {
    String errorMessage = 
        "An error occurs when reaching [%s] due to [%s]".formatted(req.getPath, ex.getMessage());
    return new BringResponse<>(ex.getMessage(), Map.of("TestHeader", "TestValue"),
        ResponseStatus.INTERNAL_SERVER_ERROR);
  }
}
```
### Example 10: Properties application configuration
Configure your application context.
Create application.properties file in the resources folder.
Configuration has next options:

```properties
server.port=<SERVER PORT NUMBER>
banner=<BOOLEAN>
logging=<LOGGING LEVEL [INFO|DEBUG|WARN|ERROR]>
```

### Example 11: Logging controller
Control logging level with application properties in the resources folder
```properties
logging=<LOGGING LEVEL [INFO|DEBUG|WARN|ERROR]>
```

### Example 12: Controlling banner
Enable/disable banner using application.properties in the resources folder
```properties
banner=<BOOLEAN>
```

## Contributing
We welcome contributions!
If you'd like to contribute to Bring, please contact with the team Breskul.


## License
This project is licensed under the [MIT License](https://opensource.org/license/mit/).