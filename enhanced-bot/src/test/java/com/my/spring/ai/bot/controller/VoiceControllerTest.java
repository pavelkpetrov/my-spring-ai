package com.my.spring.ai.bot.controller;

import com.my.spring.ai.bot.dto.AudioRequest;
import com.my.spring.ai.bot.dto.AudioResponse;
import com.my.spring.ai.bot.service.VoicePipelineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for VoiceController.
 *
 * This test class demonstrates proper controller testing patterns for voice chat endpoints.
 * It covers successful voice query processing as well as validation error scenarios.
 *
 * Testing Strategy:
 * - @WebMvcTest for focused controller testing
 * - MockMvc for HTTP request simulation
 * - Mocked VoicePipelineService to isolate controller logic
 * - Scenario coverage (success and validation failures)
 *
 * We exclude Ollama and ChromaDB autoconfiguration to prevent connection attempts during tests.
 */
@WebMvcTest(
    controllers = VoiceController.class,
    excludeAutoConfiguration = {
        OllamaAutoConfiguration.class,
        ChromaVectorStoreAutoConfiguration.class
    }
)
@ActiveProfiles("voice")
class VoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoicePipelineService voicePipelineService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/voice/chat - Service exception should return Internal Server Error")
    void chat_whenServiceThrowsException_shouldReturnInternalServerError() throws Exception {
        String sessionId = "error-session";
        byte[] audioInput = new byte[]{1};

        AudioRequest request = AudioRequest.builder()
                .sessionId(sessionId)
                .voice(audioInput)
                .build();

        when(voicePipelineService.processVoiceQuery(anyString(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/voice/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(voicePipelineService, times(1)).processVoiceQuery(eq(sessionId), eq(audioInput));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void chat_withInvalidSessionId_shouldReturnBadRequest(String sessionId) throws Exception {
        AudioRequest request = AudioRequest.builder()
                .sessionId(sessionId)
                .voice(new byte[]{1,2})
                .build();

        mockMvc.perform(post("/api/voice/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(voicePipelineService, never()).processVoiceQuery(any(), any());
    }

    @Test
    @DisplayName("POST /api/voice/chat - Bad Request: Malformed JSON should return Bad Request")
    void chat_withMalformedJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/voice/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());

        verify(voicePipelineService, never()).processVoiceQuery(anyString(), any());
    }

    @Test
    @DisplayName("DELETE /api/voice/chat/{sessionId} - Service fails, returns Internal Server Error")
    void clearChat_whenServiceThrowsException_shouldReturnInternalServerError() throws Exception {
        String sessionId = "missing-session";
        doThrow(new RuntimeException("Not found")).when(voicePipelineService).clearUserChatHistory(sessionId, false);

        mockMvc.perform(delete("/api/voice/chat/{sessionId}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(voicePipelineService).clearUserChatHistory(sessionId, false);
    }

    @Test
    @DisplayName("GET /api/voice/chat - Method Not Allowed")
    void chat_withGetMethod_shouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/api/voice/chat")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("POST /api/voice/chat - Success: Valid audio request should return audio response")
    void chat_withValidRequest_shouldReturnSuccessResponse() throws Exception {
        // Given
        String sessionId = "test-session-123";
        byte[] audioInput = new byte[]{1, 2, 3, 4, 5};
        byte[] audioOutput = new byte[]{6, 7, 8, 9, 10};
        String requestText = "Hello, how are you?";
        String responseText = "I'm doing well, thank you!";

        AudioRequest request = AudioRequest.builder()
                .sessionId(sessionId)
                .voice(audioInput)
                .build();

        AudioResponse mockResponse = new AudioResponse(
                audioOutput,
                responseText,
                requestText,
                sessionId
        );

        // Mock the service behavior
        when(voicePipelineService.processVoiceQuery(sessionId, audioInput))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/voice/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.request").value(requestText))
                .andExpect(jsonPath("$.response").value(responseText))
                .andExpect(jsonPath("$.voice").isString()); // byte[] is serialized as Base64 string

        // Verify the service was called exactly once with the correct arguments
        verify(voicePipelineService, times(1)).processVoiceQuery(sessionId, audioInput);
    }

    @Test
    @DisplayName("POST /api/voice/chat - Bad Request: Null sessionId should return bad request")
    void chat_withNullSessionId_shouldReturnBadRequest() throws Exception {
        // Given
        byte[] audioInput = new byte[]{1, 2, 3};

        AudioRequest invalidRequest = AudioRequest.builder()
                .sessionId(null)  // Null sessionId violates @NotNull constraint
                .voice(audioInput)
                .build();

        // When & Then
        mockMvc.perform(post("/api/voice/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Verify the service was never called
        verify(voicePipelineService, never()).processVoiceQuery(anyString(), any(byte[].class));
    }

    @Test
    @DisplayName("POST /api/voice/chat - Bad Request: Null voice should return bad request")
    void chat_withNullVoice_shouldReturnBadRequest() throws Exception {
        // Given
        String sessionId = "test-session-789";

        AudioRequest invalidRequest = AudioRequest.builder()
                .sessionId(sessionId)
                .voice(null)  // Null voice violates @NotNull constraint
                .build();

        // When & Then
        mockMvc.perform(post("/api/voice/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Verify the service was never called
        verify(voicePipelineService, never()).processVoiceQuery(anyString(), any(byte[].class));
    }

    @Test
    @DisplayName("DELETE /api/voice/chat/{sessionId} - Success: Valid sessionId should clear history")
    void clearChat_withValidSessionId_shouldReturnOk() throws Exception {
        // Given
        String sessionId = "session-to-clear-123";

        // Mock the void method
        doNothing().when(voicePipelineService).clearUserChatHistory(sessionId, false);

        // When & Then
        mockMvc.perform(delete("/api/voice/chat/{sessionId}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify the service was called exactly once
        verify(voicePipelineService, times(1)).clearUserChatHistory(sessionId, false);
    }

    @Test
    @DisplayName("DELETE /api/voice/chat/{sessionId}?isNew=true - Success: Clear with isNew parameter")
    void clearChat_withIsNewParameter_shouldReturnOk() throws Exception {
        // Given
        String sessionId = "session-to-clear-456";

        // Mock the void method
        doNothing().when(voicePipelineService).clearUserChatHistory(sessionId, true);

        // When & Then
        mockMvc.perform(delete("/api/voice/chat/{sessionId}", sessionId)
                        .param("isNew", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify the service was called exactly once with isNew=true
        verify(voicePipelineService, times(1)).clearUserChatHistory(sessionId, true);
    }

    @Test
    @DisplayName("DELETE /api/voice/chat/{sessionId} - Default isNew parameter should be false")
    void clearChat_withoutIsNewParameter_shouldDefaultToFalse() throws Exception {
        // Given
        String sessionId = "session-to-clear-789";

        // Mock the void method
        doNothing().when(voicePipelineService).clearUserChatHistory(sessionId, false);

        // When & Then
        mockMvc.perform(delete("/api/voice/chat/{sessionId}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify the service was called with default isNew=false
        verify(voicePipelineService, times(1)).clearUserChatHistory(sessionId, false);
    }
}