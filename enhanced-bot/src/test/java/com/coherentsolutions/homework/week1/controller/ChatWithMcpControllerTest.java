package com.coherentsolutions.homework.week1.controller;

import com.coherentsolutions.homework.week1.dto.ChatRequest;
import com.coherentsolutions.homework.week1.service.McpChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ChatWithMcpController.
 *
 * This test class demonstrates proper controller testing patterns using Spring Boot Test
 * and MockMvc. It focuses on testing the HTTP layer behavior, request/response handling,
 * and integration with the MCP chat service layer.
 *
 * Testing Strategy:
 * - @WebMvcTest for focused controller testing
 * - MockMvc for HTTP request simulation
 * - Mocked service layer to isolate controller logic
 * - Comprehensive scenario coverage (success and failure cases for each endpoint)
 *
 * Endpoints tested:
 * 1. POST /api/mcp/chat - Chat with MCP-enabled AI
 * 2. DELETE /api/mcp/chat/{sessionId} - Clear chat history
 * 3. GET /api/mcp/chat/tools - List available MCP tools
 */
@WebMvcTest(
    value = ChatWithMcpController.class,
    excludeAutoConfiguration = {
        OllamaAutoConfiguration.class,
        ChromaVectorStoreAutoConfiguration.class
    }
)
class ChatWithMcpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private McpChatService mcpChatService;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================================
    // POST /api/mcp/chat - Chat endpoint tests
    // =========================================================================

    @Test
    @DisplayName("POST /api/mcp/chat - Success: Valid request returns AI response")
    void whenChat_withValidRequest_shouldReturnSuccessResponse() throws Exception {
        // --- Arrange ---
        String sessionId = "mcp-session-123";
        String userMessage = "What tools do you have access to?";
        String aiResponse = "I have access to filesystem, weather, and email tools.";

        ChatRequest request = ChatRequest.builder()
                .sessionId(sessionId)
                .message(userMessage)
                .build();

        // Mock service behavior
        when(mcpChatService.chat(sessionId, userMessage)).thenReturn(aiResponse);

        // --- Act & Assert ---
        mockMvc.perform(post("/api/mcp/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value(aiResponse))
                .andExpect(jsonPath("$.sessionId").value(sessionId));

        // --- Verify ---
        verify(mcpChatService, times(1)).chat(sessionId, userMessage);
    }

    @Test
    @DisplayName("POST /api/mcp/chat - Failure: Invalid request with null message")
    void whenChat_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        // --- Arrange ---
        // Create an invalid request with null message (violates @NotNull validation)
        ChatRequest invalidRequest = ChatRequest.builder()
                .sessionId("session-456")
                .message(null)  // Invalid: message cannot be null
                .build();

        // --- Act & Assert ---
        mockMvc.perform(post("/api/mcp/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // --- Verify ---
        // Service should never be called due to validation failure
        verify(mcpChatService, never()).chat(anyString(), anyString());
    }

    // =========================================================================
    // DELETE /api/mcp/chat/{sessionId} - Clear chat history endpoint tests
    // =========================================================================

    @Test
    @DisplayName("DELETE /api/mcp/chat/{sessionId} - Success: Clears chat history")
    void whenClearChat_withValidSessionId_shouldReturnOk() throws Exception {
        // --- Arrange ---
        String sessionId = "session-to-clear-789";

        // Mock void method behavior
        doNothing().when(mcpChatService).clearUserChatHistory(sessionId, false);

        // --- Act & Assert ---
        mockMvc.perform(delete("/api/mcp/chat/{sessionId}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // --- Verify ---
        verify(mcpChatService, times(1)).clearUserChatHistory(sessionId, false);
    }

    @Test
    @DisplayName("DELETE /api/mcp/chat/{sessionId} - Failure: Service throws exception")
    void whenClearChat_andServiceThrowsException_shouldReturnInternalServerError() throws Exception {
        // --- Arrange ---
        String sessionId = "session-error";

        // Mock service to throw exception
        doThrow(new RuntimeException("Failed to clear chat history"))
                .when(mcpChatService).clearUserChatHistory(sessionId, false);

        // --- Act & Assert ---
        mockMvc.perform(delete("/api/mcp/chat/{sessionId}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        // --- Verify ---
        verify(mcpChatService, times(1)).clearUserChatHistory(sessionId, false);
    }

    // =========================================================================
    // GET /api/mcp/chat/tools - List tools endpoint tests
    // =========================================================================

    @Test
    @DisplayName("GET /api/mcp/chat/tools - Success: Returns list of available tools")
    void whenListTools_shouldReturnToolsList() throws Exception {
        // --- Arrange ---
        // Mock service to return a non-empty list of tools
        // Note: Using empty list for simplicity since mocking internal ToolCallback structure is complex
        List<ToolCallback> tools = Collections.emptyList();  // Simplified mock

        // Mock service behavior
        when(mcpChatService.listAvailableMcpTools()).thenReturn(tools);

        // --- Act & Assert ---
        mockMvc.perform(get("/api/mcp/chat/tools")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        // --- Verify ---
        verify(mcpChatService, times(1)).listAvailableMcpTools();
    }

    @Test
    @DisplayName("GET /api/mcp/chat/tools - Failure: Service throws exception")
    void whenListTools_andServiceThrowsException_shouldReturnInternalServerError() throws Exception {
        // --- Arrange ---
        // Mock service to throw exception
        when(mcpChatService.listAvailableMcpTools())
                .thenThrow(new RuntimeException("Failed to retrieve MCP tools"));

        // --- Act & Assert ---
        mockMvc.perform(get("/api/mcp/chat/tools")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        // --- Verify ---
        verify(mcpChatService, times(1)).listAvailableMcpTools();
    }

    // =========================================================================
    // Additional edge case tests
    // =========================================================================

    @Test
    @DisplayName("DELETE /api/mcp/chat/{sessionId}?isNew=true - Success: Clears with isNew flag")
    void whenClearChat_withIsNewFlagTrue_shouldReturnOk() throws Exception {
        // --- Arrange ---
        String sessionId = "new-session-123";

        // Mock void method behavior with isNew=true
        doNothing().when(mcpChatService).clearUserChatHistory(sessionId, true);

        // --- Act & Assert ---
        mockMvc.perform(delete("/api/mcp/chat/{sessionId}", sessionId)
                        .param("isNew", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // --- Verify ---
        verify(mcpChatService, times(1)).clearUserChatHistory(sessionId, true);
    }

    @Test
    @DisplayName("POST /api/mcp/chat - Failure: Service throws exception")
    void whenChat_andServiceThrowsException_shouldReturnInternalServerError() throws Exception {
        // --- Arrange ---
        String sessionId = "session-error";
        String userMessage = "This will fail";
        ChatRequest request = ChatRequest.builder()
                .sessionId(sessionId)
                .message(userMessage)
                .build();

        // Mock service to throw exception
        when(mcpChatService.chat(sessionId, userMessage))
                .thenThrow(new RuntimeException("MCP server connection failed"));

        // --- Act & Assert ---
        mockMvc.perform(post("/api/mcp/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        // --- Verify ---
        verify(mcpChatService, times(1)).chat(sessionId, userMessage);
    }

}