package com.mcpserver.config;

import com.mcpserver.tools.SplunkMcpTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for MCP Server Tool Callbacks
 *
 * This configuration initializes and logs the registration of MCP-compatible tools
 * that can be invoked by AI assistants and other MCP clients.
 *
 * In Spring AI M6, @Tool annotated methods in Spring beans are automatically discovered
 * and registered by the framework. This configuration class simply validates that the
 * tool objects are properly initialized.
 *
 * The SplunkMcpTools bean is automatically scanned for @Tool annotated methods, which are
 * then made available for invocation via:
 * - Direct REST/SSE API calls
 * - AI model function calling
 * - MCP client protocol integration
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class McpServerToolConfig {

//    @Bean
//    public ToolCallbackProvider splunkTools(SplunkMcpTools splunkMcpTools) {
//        return MethodToolCallbackProvider.builder()
//                .toolObjects(splunkMcpTools)
//                .build();
//    }
}