# MCP Server

## Overview
This is a standalone Spring Boot application that provides Model Context Protocol (MCP) tools for distributed AI capabilities.

## Technical Stack
- **Java Version**: Java 21 (LTS)
- **Framework**: Spring Boot 3.5.3 with Spring AI
- **Build Tool**: Maven
- **Transport**: Reactive (Spring WebFlux with SSE)
- **Authentication**: API key-based

## Project Structure
```
mcp-server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── mcpserver/
│   │   │           ├── MCPServerApplication.java
│   │   │           ├── controller/      # MCP and Health controllers
│   │   │           ├── service/         # Business logic services
│   │   │           ├── tools/           # MCP tool implementations
│   │   │           ├── model/           # Data models
│   │   │           └── security/        # Authentication filters
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
│           └── com/mcpserver/
│               └── integration/         # Integration tests
└── pom.xml
```

## Configuration
The server runs on port 8081 by default. Configuration can be customized in `application.yml`:

```yaml
server:
  port: 8081

mcp:
  auth:
    api-key: ${MCP_SERVER_KEY}
  tools:
    enabled: true
```

## Running the Server
```bash
# Set environment variable for API key
export MCP_SERVER_KEY=your-api-key-here

# Run the application
mvn spring-boot:run
```

## Next Steps
- Implement MCP tools (Data Retrieval, Processing, Action)
- Add authentication filter
- Create health check endpoint
- Add integration tests