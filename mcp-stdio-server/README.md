# Spring AI MCP Sampling Server

This sample project demonstrates how to create an MCP server using the Spring AI MCP Server Boot Starter with WebMVC transport. It implements a weather service that exposes tools for retrieving weather information using the Open-Meteo API and showcases MCP Sampling capabilities.

For more information, see the [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html) reference documentation.

## Overview

The sample showcases:
- Integration with `spring-ai-mcp-server-webmvc-spring-boot-starter`
- Support for STDIO transports
- Automatic tool registration using Spring AI's `@Tool` annotation
- MCP STDIO implementation that demonstrates LLM provider routing
- Weather tool that retrieves temperature data and generates creative responses using multiple LLMs

## Dependencies

The project requires the Spring AI MCP Server WebMVC Boot Starter:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

This starter provides:
- STDIO transport
- Included `spring-boot-starter-web` and `mcp-spring-webmvc` dependencies

## Building the Project

Build the project using Maven:
```bash
./mvnw clean package -DskipTests
```

## Running the Server

The server supports STDIO transport mode:

### STDIO Mode
To enable STDIO transport, set the appropriate properties:
```bash
java -Dspring.ai.mcp.server.stdio=true -Dspring.main.web-application-type=none -jar target/mcp-stdio-server-0.0.1-SNAPSHOT.jar
```

## Configuration

Configure the server through `application.yml`:

```properties
# Server identification
spring.ai.mcp.server.name=mcp-stdio-server
spring.ai.mcp.server.version=0.0.1
spring.main.banner-mode=off

#STDIO transport
spring.ai.mcp.server.stdio=true
spring.main.web-application-type=none
logging.pattern.console=
```

## Available Tools

### Weather Temperature Tool
- Name: `getTemperature`
- Description: Get the temperature (in celsius) for a specific location
- Parameters:
  - `latitude`: double - The location latitude
  - `longitude`: double - The location longitude
  - `toolContext`: ToolContext - Automatically provided by Spring AI

This tool not only retrieves the current temperature from the Open-Meteo API but also uses MCP Sampling to generate creative poems about the weather from both OpenAI and Anthropic models.

## MCP Clients 

You can connect to the weather server using either STDIO transport:

### Manual Clients

#### STDIO Client

For servers using STDIO transport:

```java
var stdioParams = ServerParameters.builder("java")
    .args("-Dspring.ai.mcp.server.stdio=true",
          "-Dspring.main.web-application-type=none",
          "-Dspring.main.banner-mode=off",
          "-Dlogging.pattern.console=",
          "-jar",
          "target/mcp-stdio-server-0.0.1-SNAPSHOT.jar")
    .build();

var transport = new StdioClientTransport(stdioParams);
var client = McpClient.sync(transport).build();
```
