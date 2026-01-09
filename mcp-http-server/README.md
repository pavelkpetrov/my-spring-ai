# MCP HTTP Server - Email Greeting Service

A Spring AI MCP (Model Context Protocol) HTTP server that provides email greeting functionality.

## Overview

This server implements a stateless HTTP MCP server using Spring Boot and Spring AI. It exposes a single synchronous tool for sending greeting emails to recipients.

### Features

- **Protocol**: HTTP (Stateless Transport)
- **Tool Type**: Synchronous (SYNC)
- **Function**: Send greeting emails
- **Framework**: Spring Boot 3.5.3 + Spring AI 1.1.2

## Tool

### send_greeting_email

Send a personalized greeting email to a specified recipient.

**Parameters:**
- `toEmail` (String, required) - Destination email address
- `greetingText` (String, required) - The greeting message to send

**Returns:**
```json
{
  "success": true,
  "message": "Greeting email sent successfully",
  "toEmail": "recipient@example.com"
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Failed to send greeting email: <error details>",
  "error": "ExceptionType"
}
```

## Configuration

### Required Environment Variables

```bash
# Email Server Configuration
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# Optional: Sender Information
export EMAIL_FROM_ADDRESS=${MAIL_USERNAME}
export EMAIL_FROM_NAME="Greeting Service"
```

### Gmail Setup

If using Gmail:

1. Enable 2-factor authentication on your Google account
2. Generate an App Password:
   - Go to Google Account → Security → 2-Step Verification → App passwords
   - Generate a new app password for "Mail"
   - Use this password as `MAIL_PASSWORD`

### application.yml

```yaml
server:
  port: 8082

spring:
  ai:
    mcp:
      server:
        protocol: HTTP
        enabled: true
        name: email-greeting-server
        type: SYNC
        capabilities:
          tool: true

  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

## Build and Run

### Build

```bash
cd mcp-http-server
mvn clean package
```

### Run

```bash
# Set environment variables first
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# Run the server
mvn spring-boot:run
```

The server will start on port **8082**.

## API Endpoints

### MCP Tool Endpoint

```
POST http://localhost:8082/mcp
```

The exact endpoint path depends on Spring AI MCP server configuration. Check the server logs on startup for the actual endpoint.

### Health Check

Spring Boot Actuator endpoints (if enabled):

```
GET http://localhost:8082/actuator/health
```

## Testing

### Using curl

```bash
# Test the MCP tool (adjust endpoint as needed)
curl -X POST http://localhost:8082/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "tool": "send_greeting_email",
    "arguments": {
      "toEmail": "recipient@example.com",
      "greetingText": "Hello! This is a test greeting from the MCP HTTP server."
    }
  }'
```

## Integration with MCP Client

### Enhanced Bot Configuration

Add to `enhanced-bot/src/main/resources/application-mcp.yml`:

```yaml
spring:
  ai:
    mcp:
      client:
        http:
          connections:
            email-greeting-server:
              url: http://localhost:8082
```

## Project Structure

```
mcp-http-server/
├── src/
│   ├── main/
│   │   ├── java/com/mcpserver/email/
│   │   │   ├── EmailMcpServerApplication.java    # Main application
│   │   │   ├── config/
│   │   │   │   └── EmailProperties.java          # Email configuration properties
│   │   │   ├── service/
│   │   │   │   └── EmailService.java             # Email sending service
│   │   │   └── tool/
│   │   │       └── EmailGreetingTool.java        # MCP tool implementation
│   │   └── resources/
│   │       └── application.yml                    # Configuration
│   └── test/
│       └── java/com/mcpserver/email/
└── pom.xml
```

## Dependencies

Key dependencies:
- `spring-boot-starter-web` - Web/REST support
- `spring-ai-starter-mcp-server-webmvc` - MCP HTTP server support
- `spring-boot-starter-mail` - Email functionality
- `lombok` - Code generation

## Troubleshooting

### Email Not Sending

1. **Check credentials**: Verify `MAIL_USERNAME` and `MAIL_PASSWORD` are correct
2. **Gmail App Password**: Ensure you're using an App Password, not your regular password
3. **SMTP Settings**: Verify `MAIL_HOST` and `MAIL_PORT` are correct for your provider
4. **Firewall**: Ensure port 587 (or 465 for SSL) is not blocked
5. **Check logs**: Enable DEBUG logging for `org.springframework.mail`

### Server Not Starting

1. **Port conflict**: Check if port 8082 is already in use
2. **Check logs**: Look for startup errors in console output
3. **Dependencies**: Run `mvn clean install` to ensure all dependencies are downloaded

## References

- [Spring Email Guide](https://www.baeldung.com/spring-email)
- [Spring AI MCP Documentation](https://docs.spring.io/spring-ai/reference/api/mcp.html)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)

## License

Educational project for Spring AI MCP integration demonstration.