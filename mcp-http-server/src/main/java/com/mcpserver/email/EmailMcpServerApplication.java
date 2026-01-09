package com.mcpserver.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Email Greeting MCP HTTP Server.
 *
 * This server provides a Model Context Protocol (MCP) HTTP endpoint
 * for sending greeting emails. It uses Spring AI's MCP server support
 * with HTTP stateless transport protocol.
 *
 * Features:
 * - Single synchronous tool: send_greeting_email
 * - HTTP transport (stateless)
 * - Spring Mail integration for email sending
 */
@SpringBootApplication
public class EmailMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailMcpServerApplication.class, args);
    }
}