# My Spring AI Projects

This repository contains multiple Spring Boot applications demonstrating Spring AI and Model Context Protocol (MCP) implementations.

## Sub-Projects

### 1. enhanced-bot
**Spring AI Bot with Multiple LLM Providers**

A Spring Boot application demonstrating Spring AI integration with multiple LLM providers:
- OpenAI and Ollama (default profile)
- AWS Bedrock (Amazon Titan models)
- Chroma vector store for embeddings
- MCP client integration for tool calling

**Technologies**: Spring AI 1.0.0-M6, Spring Boot 3.5.3, Java 17

---

### 2. mcp-http-server
**MCP HTTP Server - Email Service**

An MCP server implementation using HTTP transport (stateless) to send greeting emails.
- Exposes MCP tools via HTTP endpoints
- Email functionality using Spring Boot Mail
- WebFlux-based reactive implementation

**Technologies**: Spring AI 1.0.3, Spring Boot 3.5.3, Java 21

---

### 3. mcp-sse-server
**MCP SSE Server - Splunk Integration**

An MCP server implementation using Server-Sent Events (SSE) transport for real-time communication.
- Extracts and processes Splunk log messages
- Splunk SDK integration
- Security layer for MCP server
- WebFlux reactive stack

**Technologies**: Spring AI 1.1.2, Spring Boot 3.5.3, Java 21, Splunk SDK 1.7.0

---

### 4. mcp-stdio-server
**MCP STDIO Server - Sample Implementation**

A sample Spring Boot application demonstrating MCP client and server usage with STDIO transport.
- Basic MCP server implementation
- WebMVC-based approach
- Reference implementation for learning MCP concepts

**Technologies**: Spring AI 1.0.0-M7, Spring Boot 4.0.0

---

## What is MCP?

Model Context Protocol (MCP) is an open protocol that enables seamless integration between LLM applications and external data sources and tools. Spring AI provides MCP server implementations in three transport modes:

- **HTTP** - Stateless request/response (mcp-http-server)
- **SSE** - Server-Sent Events for real-time streaming (mcp-sse-server)
- **STDIO** - Standard input/output for local processes (mcp-stdio-server)

## Getting Started

Each sub-project contains its own configuration and can be run independently. Refer to each project's specific documentation for setup instructions.

## Common Requirements

- Java 17+ (enhanced-bot) or Java 21+ (MCP servers)
- Maven 3.6+
- Docker (optional, for running dependencies like Chroma or Splunk)
