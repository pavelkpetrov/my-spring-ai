package com.coherentsolutions.homework.week1.controller;

import com.coherentsolutions.homework.week1.dto.AnswerResponse;
import com.coherentsolutions.homework.week1.service.QAService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for QaController.
 *
 * This test class demonstrates proper controller testing patterns for the Q&A endpoint.
 * It covers successful question answering as well as edge cases and error scenarios.
 *
 * Testing Strategy:
 * - @WebMvcTest for focused controller testing
 * - MockMvc for HTTP request simulation
 * - Mocked QAService to isolate controller logic
 * - Comprehensive scenario coverage (success, validation errors, service errors)
 *
 * We exclude Ollama and ChromaDB autoconfiguration to prevent connection attempts during tests.
 */
@WebMvcTest(
    value = QaController.class,
    excludeAutoConfiguration = {
        OllamaAutoConfiguration.class,
        ChromaVectorStoreAutoConfiguration.class
    }
)
@ActiveProfiles("rag")
class QaControllerTest {

    private static final String VALID_QUESTION = "What is artificial intelligence?";
    private static final String LONG_QUESTION =
            "Can you explain " + "what ".repeat(100) + "artificial intelligence is?";
    private static final String NO_MATCH_QUESTION = "What is the weather like today?";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QAService qaService;

    @Test
    @DisplayName("GET /qa - Corner Case: Empty question should return bad request with error response")
    void question_withEmptyQuestion_shouldReturnBadRequestAndError() throws Exception {
        mockMvc.perform(get("/qa").param("question", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("Request validation failed. Please check your input."));
        verify(qaService, never()).getAnswer(anyString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void question_withBlankOrNull_shouldReturnBadRequest(String input) throws Exception {
        mockMvc.perform(get("/qa").param("question", input))
                .andExpect(status().isBadRequest());
        verify(qaService, never()).getAnswer(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void question_withBlankQuestion_shouldReturnBadRequest(String input) throws Exception {
        mockMvc.perform(get("/qa").param("question", input))
                .andExpect(status().isBadRequest());
        verify(qaService, never()).getAnswer(anyString());
    }

    @Test
    @DisplayName("GET /qa - Success: Valid question should return answer with relevant documents")
    void question_withValidQuestion_shouldReturnAnswer() throws Exception {
        // Given: A valid question
        AnswerResponse mockResponse = AnswerResponse.builder()
                .answer("Artificial intelligence (AI) is the simulation of human intelligence processes by machines, " +
                        "especially computer systems. These processes include learning, reasoning, and self-correction.")
                .build();

        // Mock the service behavior
        when(qaService.getAnswer(VALID_QUESTION)).thenReturn(mockResponse);

        // When & Then: Perform the request and verify the response
        mockMvc.perform(get("/qa")
                        .param("question", VALID_QUESTION))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.answer").value(mockResponse.getAnswer()));

        // Verify the service was called exactly once with the correct question
        verify(qaService, times(1)).getAnswer(VALID_QUESTION);
    }

    @Test
    @DisplayName("GET /qa - Corner Case: Empty question parameter should return bad request")
    void question_withEmptyQuestion_shouldReturnBadRequest() throws Exception {
        // Given: An empty question parameter
        // When & Then: Perform the request with empty question
        mockMvc.perform(get("/qa")
                        .param("question", ""))
                .andExpect(status().isBadRequest());

        // Verify the service was never called
        verify(qaService, never()).getAnswer(anyString());
    }

    @Test
    @DisplayName("GET /qa - Corner Case: Missing question parameter should return bad request")
    void question_withMissingQuestion_shouldReturnBadRequest() throws Exception {
        // Given: No question parameter provided
        // When & Then: Perform the request without question parameter
        mockMvc.perform(get("/qa"))
                .andExpect(status().isBadRequest());

        // Verify the service was never called
        verify(qaService, never()).getAnswer(anyString());
    }

    @Test
    @DisplayName("GET /qa - Corner Case: Question with special characters should be processed")
    void question_withSpecialCharacters_shouldReturnAnswer() throws Exception {
        // Given: A question with special characters
        String questionWithSpecialChars = "What is AI? & How does ML work?";

        AnswerResponse mockResponse = AnswerResponse.builder()
                .answer("AI and ML are related but distinct concepts...")
                .build();

        when(qaService.getAnswer(questionWithSpecialChars)).thenReturn(mockResponse);

        // When & Then: Perform the request with special characters
        mockMvc.perform(get("/qa")
                        .param("question", questionWithSpecialChars))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").exists());

        verify(qaService, times(1)).getAnswer(questionWithSpecialChars);
    }

    @Test
    @DisplayName("GET /qa - Corner Case: Service exception should return internal server error")
    void question_whenServiceThrowsException_shouldReturnInternalServerError() throws Exception {
        // Given: A valid question but service throws an exception

        // Mock the service to throw an exception
        when(qaService.getAnswer(VALID_QUESTION))
                .thenThrow(new RuntimeException("Vector store search failed"));

        // When & Then: Perform the request and expect internal server error
        mockMvc.perform(get("/qa")
                        .param("question", VALID_QUESTION))
                .andExpect(status().isInternalServerError());

        // Verify the service was called
        verify(qaService, times(1)).getAnswer(VALID_QUESTION);
    }

    @Test
    @DisplayName("GET /qa - Corner Case: Very long question should be processed")
    void question_withVeryLongQuestion_shouldReturnAnswer() throws Exception {
        // Given: A very long question (testing edge case)
        AnswerResponse mockResponse = AnswerResponse.builder()
                .answer("Artificial intelligence is...")
                .build();

        when(qaService.getAnswer(LONG_QUESTION)).thenReturn(mockResponse);

        // When & Then: Perform the request with a very long question
        mockMvc.perform(get("/qa")
                        .param("question", LONG_QUESTION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").exists());

        verify(qaService, times(1)).getAnswer(LONG_QUESTION);
    }

    @Test
    @DisplayName("GET /qa - Corner Case: Question with no matching documents should return answer with low confidence")
    void question_withNoMatchingDocuments_shouldReturnLowConfidenceAnswer() throws Exception {
        // Given: A question that has no matching documents in the vector store

        AnswerResponse mockResponse = AnswerResponse.builder()
                .answer("I don't have information about that topic in my knowledge base.")
                .build();

        when(qaService.getAnswer(NO_MATCH_QUESTION)).thenReturn(mockResponse);

        // When & Then: Perform the request
        mockMvc.perform(get("/qa")
                        .param("question", NO_MATCH_QUESTION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(mockResponse.getAnswer()));

        verify(qaService, times(1)).getAnswer(NO_MATCH_QUESTION);
    }
}