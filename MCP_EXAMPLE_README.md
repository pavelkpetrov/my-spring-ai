# MCP Integration Example

A Spring AI application demonstrating Model Context Protocol (MCP) integration with multiple server types.

## Overview

This example showcases MCP client integration with three different MCP server implementations:

1. **STDIO MCP Server** - Weather service that retrieves weather information by latitude and longitude
2. **SSE MCP Server** - Splunk log extraction service for querying and analyzing logs
3. **HTTP MCP Server** - Email greeting service for sending personalized greeting emails

## Architecture

```
┌──────────────────────────────────────┐
│   Enhanced Bot (MCP Client)          │
│   Port: 8080                         │
└────────┬───────────────┬─────────────┬───────┘
         │               │             │
         │ STDIO         │ SSE         │ HTTP
         │               │             │
    ┌────▼────┐     ┌────▼─────────┐  │
    │ Weather │     │ Splunk MCP   │  │
    │ Server  │     │ Server       │  │
    │         │     │ Port: 8081   │  │
    └─────────┘     └──────────────┘  │
                                       │
                              ┌────────▼────────┐
                              │ Email MCP       │
                              │ Server          │
                              │ Port: 8082      │
                              └─────────────────┘
```

## Prerequisites

- Java 21
- Maven 3.x
- AWS Credentials (for Bedrock AI)

## Environment Variables

Set the following environment variables before running the application:

```bash
# AWS Credentials (Required for Bedrock AI)
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_SESSION_TOKEN=your_session_token

# MCP STDIO Server Path (Required)
export MPC_STDIO_SERVER_PATH=./mcp-stdio-server/target/mcp-stdio-server-0.0.1-SNAPSHOT.jar

# Optional: MCP Server API Key (if authentication enabled)
export MCP_SERVER_API_KEY=dev-key-1234567890abcdef
```

## Quick Start

### 1. Build the Projects

```bash
# Build STDIO server
cd mcp-stdio-server
mvn clean package
cd ..

# Build SSE server
cd mcp-sse-server
mvn clean package
cd ..

# Build HTTP server
cd mcp-http-server
mvn clean package
cd ..

# Build enhanced bot
cd enhanced-bot
mvn clean package
cd ..
```

### 2. Start the MCP Servers (Optional)

#### Start SSE Server (for Splunk integration)
```bash
cd mcp-sse-server
mvn spring-boot:run
```
The SSE server will start on port **8081**.

#### Start HTTP Server (for Email greeting)
```bash
cd mcp-http-server

# Set required email configuration
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

mvn spring-boot:run
```
The HTTP server will start on port **8082**.

**Note:** For Gmail, use an App Password (not your regular password). See [mcp-http-server/README.md](mcp-http-server/README.md) for details.

### 3. Start the Enhanced Bot

```bash
cd enhanced-bot
mvn spring-boot:run -Dspring-boot.run.profiles=mcp
```

**Important:** The active Spring profile must be set to `mcp` to enable MCP integration.

## MCP Servers

### Weather Server (STDIO)

- **Type:** STDIO (Standard Input/Output)
- **Protocol:** Process communication
- **Functionality:** Retrieves weather data by geographic coordinates
- **Auto-started:** Yes (launched by the client application)

**Tools:**
- `get_weather` - Get weather information for a specific location

### Splunk Server (SSE)

- **Type:** SSE (Server-Sent Events)
- **Protocol:** HTTP/SSE
- **Port:** 8081
- **Functionality:** Query and extract logs from Splunk
- **Authentication:** Optional API key

**Tools:**
- `search_logs` - Search Splunk logs with custom queries
- `extract_events` - Extract specific events from Splunk

### Email Server (HTTP)

- **Type:** HTTP (Stateless)
- **Protocol:** HTTP
- **Port:** 8082
- **Functionality:** Send personalized greeting emails
- **Requirements:** SMTP server credentials (e.g., Gmail App Password)

**Tools:**
- `send_greeting_email` - Send personalized greeting emails to recipients

## Configuration

### MCP Client Configuration (application-mcp.yml)

```yaml
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            splunk-mcp-server:
              url: http://localhost:8081
              api-key: ${MCP_SERVER_API_KEY}
        stdio:
          connections:
            weather-mcp-server:
              command: java
              args:
                - -jar
                - ${MPC_STDIO_SERVER_PATH}
```

## Example Usage

### Weather Query

```
User: "What's the weather at latitude 40.7128 and longitude -74.0060?"

Bot: [Calls weather MCP server via STDIO]
Response: "The current weather at that location (New York City) is..."
```

### Log Query

```
User: "Search for error logs from the last hour"

Bot: [Calls Splunk MCP server via SSE]
Response: "Found 15 error logs in the last hour..."
```

### Email Greeting

```
User: "Send a greeting email to john@example.com saying 'Happy Birthday! Hope you have a wonderful day!'"

Bot: [Calls email MCP server via HTTP]
Response: "I've sent a greeting email to john@example.com with your message."
```

## Project Structure

```
.
├── enhanced-bot/              # MCP client application
│   ├── src/main/resources/
│   │   └── application-mcp.yml
│   └── pom.xml
├── mcp-stdio-server/          # Weather STDIO server
│   ├── src/
│   └── target/
│       └── mcp-stdio-server-0.0.1-SNAPSHOT.jar
├── mcp-sse-server/            # Splunk SSE server
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── mcp-http-server/           # Email HTTP server
│   ├── src/
│   ├── http/                  # HTTP test files
│   ├── pom.xml
│   └── README.md
└── MCP_EXAMPLE_README.md      # This file
```

## Troubleshooting

### STDIO Server Not Starting

Check that the `MPC_STDIO_SERVER_PATH` environment variable points to a valid JAR file:

```bash
ls -la $MPC_STDIO_SERVER_PATH
```

### SSE Server Connection Issues

Ensure the SSE server is running:

```bash
curl http://localhost:8081/actuator/health
```

### AWS Credentials

Verify AWS credentials are set correctly:

```bash
echo $AWS_ACCESS_KEY_ID
aws sts get-caller-identity
```

## Resources

- [Spring AI MCP Documentation](https://docs.spring.io/spring-ai/reference/api/mcp.html)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [Project Assignment Details](./README.md)

## License

Educational project for Spring AI MCP integration demonstration.