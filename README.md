# Bring ![Bobocode Breskul Team](https://img.shields.io/badge/Bobocode%20Breskul%20Team-8A2BE2) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

## What is Bring?
Bring is a custom web framework that combines HTTP request handling with a Dependency Injection container.
It uses IoC (Inversion of Control) container, which ....

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Introduction

Bring Framework is designed to simplify the development of web applications in Java. It follows the Model-View-Controller (MVC) architecture and integrates with the Inversion of Control (IoC) container for dependency injection.

## Features

- **Feature One:** .....
- **Feature Two:** ......
- **Feature Three:** .....
- **Feature four:** .....

## Getting Started

Follow these steps to integrate Bring into your project:

1. Add the following Maven dependency:

```xml
<dependency>
  <groupId>io.github.bobocode-breskul</groupId>
  <artifactId>bring</artifactId>
  <version>1.0</version> //TODO What the final version will be
</dependency>
```

2. Configure your application context..

```java
import io.github.bobocodebreskul.context.annotations.BringConfiguration;

@BringConfiguration
public class Config {
  // Controller code here...
}
```

## Configuration

To customize the behavior of the Spring Web Framework, you can configure various properties in your application...

## Usage

### Example 1: Creating a Simple Controller

```java
@Annotation
@RequestMapping("/api")
public class MyController {
  // Controller code here...
}
```
### Example 2: ...
```java

```
### Example 3: ...
```java

```

## Contributing
We welcome contributions! 
If you'd like to contribute to Bring, please contact with the team Breskul!


## License
This project is licensed under the [MIT License](https://opensource.org/license/mit/).



## Quick Start
The reference [documentation](https://github.com/bobocode-breskul/bring/wiki) includes detailed installation instructions as well as a comprehensive getting started guide.

Here is a quick teaser of a complete Bring application in Java:

Add BringContainer.run("org.example") to your main method, where "org.example" is your package name.

```java
package org.example;

public class Main {
  public static void main(String[] args) {
    BringContainer.run(Main.class);
  }
}
```

Then create a new Controller with following code

```java
package org.example;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.server.annotations.RestController;
import io.github.bobocodebreskul.server.annotations.Get;

@RestController("/hello")
@BringComponent
public class MyController {

  @Get("/world")
  public String getHello() {
    return "Hello, world!";
  }
}
```

Now run the application and open http://localhost:8080/hello/world in your browser.

## Features

### HTTP Server

- Bring provides an API for handling HTTP requests and creates a servlet-based HTTP server.
- It simplifies the creation of endpoints, access to headers, request body processing, response construction, and error resolution.
- The framework efficiently serves multiple users concurrently.

### Dependency Injection Container

- Bring includes an API for configuration to instruct the container on object creation and field injection.
- Configuration can be stored in various formats such as XML, JSON, property text files, Java, annotations, and more.
- All objects created by the container are stored in a context or register of objects.

### Error Handling

Bring gracefully handles errors with appropriate exceptions and clear error messages. Users receive instructions on how to resolve issues. Detailed logging helps diagnose problems.