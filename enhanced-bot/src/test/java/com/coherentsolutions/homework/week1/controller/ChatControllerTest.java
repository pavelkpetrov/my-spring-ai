package com.coherentsolutions.homework.week1.controller;

import com.coherentsolutions.homework.week1.dto.ChatRequest;
import com.coherentsolutions.homework.week1.service.impl.ChatServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TextGeneratorController.
 *
 * This test class demonstrates proper controller testing patterns using Spring Boot Test
 * and MockMvc. It focuses on testing the HTTP layer behavior, request/response handling,
 * and integration with the service layer.
 *
 * Testing Strategy:
 * - @WebMvcTest for focused controller testing
 * - MockMvc for HTTP request simulation
 * - Mocked service layer to isolate controller logic
 * - Comprehensive scenario coverage (success, validation errors, service errors)
 *
 * Educational Value:
 * - Demonstrates modern Spring Boot testing patterns
 * - Shows proper use of mocks for dependency isolation
 * - Illustrates JSON request/response testing
 * - Teaches validation testing techniques
 * - Shows error handling testing
 **
 */
/**
 * Unit tests for the ChatController.
 * This test class uses @WebMvcTest to focus only on the web layer,
 * mocking the service dependency (ChatServiceImpl) to isolate the controller.
 *
 * We exclude Ollama and ChromaDB autoconfiguration to prevent connection attempts during tests.
 */
@WebMvcTest(
    value = ChatController.class,
    excludeAutoConfiguration = {
        OllamaAutoConfiguration.class,
        ChromaVectorStoreAutoConfiguration.class
    }
)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc; // Injects the tool to simulate HTTP requests

    @MockBean
    private ChatServiceImpl chatService; // Creates a mock of the service dependency

    @Autowired
    private ObjectMapper objectMapper; // Helper to convert Java objects to JSON and back

    @Test
    @DisplayName("POST /api/chat - Success")
    void whenChat_withValidRequest_shouldReturnSuccessResponse() throws Exception {
        // --- Arrange (Given) ---
        String sessionId = "session-123";
        String userMessage = "Hello, AI!";
        String aiResponse = "Hello, user! How can I help you today?";

        ChatRequest request = ChatRequest.builder()
                .sessionId(sessionId)
                .message(userMessage)
                .build();

        // Define the mock service behavior
        when(chatService.chat(sessionId, userMessage)).thenReturn(aiResponse);

        // --- Act (When) ---
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Send the request as JSON

                // --- Assert (Then) ---
                .andExpect(status().isOk()) // Check for HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Check response type
                .andExpect(jsonPath("$.response").value(aiResponse)) // Check response message
                .andExpect(jsonPath("$.sessionId").value(sessionId)); // Check session ID

        // --- Verify ---
        // Ensure the service method was called exactly once with the correct arguments
        verify(chatService, times(1)).chat(sessionId, userMessage);
    }

    @Test
    @DisplayName("DELETE /api/chat/{sessionId} - Success")
    void whenClearChat_withValidSessionId_shouldReturnOk() throws Exception {
        // --- Arrange (Given) ---
        String sessionId = "session-to-clear-456";

        // For void methods, use doNothing()
        doNothing().when(chatService).clearUserChatHistory(sessionId, false);

        // --- Act (When) ---
        mockMvc.perform(delete("/api/chat/{sessionId}", sessionId) // Use path variable
                        .contentType(MediaType.APPLICATION_JSON))

                // --- Assert (Then) ---
                .andExpect(status().isOk()); // Check for HTTP 200 OK

        // --- Verify ---
        // Ensure the service method was called exactly once
        verify(chatService, times(1)).clearUserChatHistory(sessionId, false);
    }

    @Test
    @DisplayName("DELETE /api/chat/{sessionId}?isNew=true - Success")
    void whenClearChat_withValidSessionId_andNew_shouldReturnOk() throws Exception {
        // --- Arrange (Given) ---
        String sessionId = "session-to-clear-456";

        // For void methods, use doNothing()
        doNothing().when(chatService).clearUserChatHistory(sessionId, true);

        // --- Act (When) ---
        mockMvc.perform(delete("/api/chat/{sessionId}?isNew=true", sessionId) // Use path variable
                        .contentType(MediaType.APPLICATION_JSON))

                // --- Assert (Then) ---
                .andExpect(status().isOk()); // Check for HTTP 200 OK

        // --- Verify ---
        // Ensure the service method was called exactly once
        verify(chatService, times(1)).clearUserChatHistory(sessionId, true);
    }

    /*
     * -------------------------------------------------------------------------
     * NOTE: VALIDATION & ERROR HANDLING TESTS
     *
     * The following tests demonstrate how to test for validation and service errors.
     * Your current controller doesn't have @Valid, but if you add it,
     * Test 1 (testChat_withInvalidRequest) will work as written.
     *
     * To make it work, you would need:
     * 1. Add validation annotations (like @NotBlank) to your ChatRequest DTO fields.
     * 2. Add the @Valid annotation in your controller:
     * public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) { ... }
     * -------------------------------------------------------------------------
     */

    @Test
    @DisplayName("POST /api/chat - Bad Request (Validation)")
    void whenChat_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        // --- Arrange (Given) ---
        // Create an invalid request (e.g., null message)
        ChatRequest invalidRequest = new ChatRequest("session-789", null);

        // Note: We don't need to mock the service, as the request should
        // fail validation *before* it even reaches the service.

        // --- Act (When) ---
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                // --- Assert (Then) ---
                .andExpect(status().isBadRequest()); // Check for HTTP 400 Bad Request

        // --- Verify ---
        // Verify the service was *never* called
        verify(chatService, never()).chat(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/chat - Service Layer Error")
    void whenChat_andServiceThrowsException_shouldReturnInternalServerError() throws Exception {
        // --- Arrange (Given) ---
        String sessionId = "session-err";
        String userMessage = "This will fail";
        ChatRequest request = new ChatRequest(sessionId, userMessage);

        // Mock the service to throw an exception
        when(chatService.chat(sessionId, userMessage))
                .thenThrow(new RuntimeException("Database connection failed"));

        // --- Act (When) ---
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // --- Assert (Then) ---
                // Without a @ControllerAdvice, a generic service exception
                // will result in an HTTP 500 Internal Server Error.
                .andExpect(status().isInternalServerError());

        // --- Verify ---
        // Verify the service *was* called, even though it failed
        verify(chatService, times(1)).chat(sessionId, userMessage);
    }
}