package com.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for MCP Server
 *
 * This server provides Model Context Protocol (MCP) tools for distributed AI capabilities
 */
@SpringBootApplication
public class MCPServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MCPServerApplication.class, args);
    }
}