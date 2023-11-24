# Bring ![Bobocode Breskul Team](https://img.shields.io/badge/Bobocode%20Breskul%20Team-8A2BE2) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
 Bring is a custom web framework that combines HTTP request handling with a Dependency Injection container.

## Installation

Add Maven dependency to your project:

```xml
<dependency>
    <groupId>io.github.bobocode-breskul</groupId>
    <artifactId>bring</artifactId>
    <version>1.3</version>
</dependency>
```

## How to run locally

Requirements: Java 17

In the terminal, navigate to the root directory of the project and execute the following command:

```shell
sh start.sh
```

## Getting Started
The reference [documentation](https://github.com/bobocode-breskul/bring/wiki) includes detailed installation instructions as well as a comprehensive getting started guide.

Here is a quick teaser of a complete Bring application in Java:

Add BringContainer.run("org.example") to your main method, where "org.example" is your package name.

```java
package org.example;

public class Main {
  public static void main(String[] args) {
    BringContainer.run("org.example");
  }
}
```

Then create a new Controller with following code

```java
package org.example;

import io.github.bobocodebreskul.context.annotations.BringComponent;
import io.github.bobocodebreskul.context.annotations.RestController;
import io.github.bobocodebreskul.context.annotations.Get;

@RestController("/hello")
@BringComponent
public class MyController {

  @Get
  public String getHello() {
    return "Hello, world!";
  }
}
```

Now run the application and open http://localhost:8080/hello in your browser.

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
