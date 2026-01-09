package com.my.spring.ai.bot.controller;

import com.my.spring.ai.bot.dto.GenerateRequest;
import com.my.spring.ai.bot.dto.GenerateResponse;
import com.my.spring.ai.bot.exception.TextGenerationException;
import com.my.spring.ai.bot.service.TextGeneratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
 */
@WebMvcTest(
    value = TextGeneratorController.class,
    excludeAutoConfiguration = {
        OllamaAutoConfiguration.class,
        ChromaVectorStoreAutoConfiguration.class
    }
)
class TextGeneratorControllerTest {
    
    /**
     * Test configuration that provides a mocked TextGeneratorService.
     * This allows us to test the controller in isolation without
     * the actual service implementation.
     */

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TextGeneratorService textGeneratorService;
    
    @Autowired
    private ObjectMapper objectMapper;
    

    @Test
    @DisplayName("Should return generated text for valid prompt")
    void generateText_ValidPrompt_ReturnsSuccess() throws Exception {
        // TODO: Implement this test following these steps:
        //
        // 1. ARRANGE: Prepare test data
        //    - Create a valid GenerateRequest with a test prompt
        //    - Create a mock GenerateResponse that the service should return
        //    - Configure the mocked service to return the mock response
        //
        // 2. ACT: Perform the HTTP request
        //    - Use MockMvc to POST to /generate
        //    - Include the request JSON in the request body
        //    - Set appropriate Content-Type header
        //
        // 3. ASSERT: Verify the response
        //    - Check HTTP status is 200 OK
        //    - Verify Content-Type is application/json
        //    - Assert the response contains expected fields
        //    - Verify the service was called exactly once with correct parameters
        //
        // Example implementation structure:
        // Given
        // GenerateRequest request = new GenerateRequest();
        // request.setPrompt("Test prompt");
        // GenerateResponse mockResponse = GenerateResponse.builder()...
        // when(textGeneratorService.generateText(any())).thenReturn(mockResponse);
        //
        // When & Then
        // mockMvc.perform(post("/generate")...)
        //     .andExpect(status().isOk())
        //     .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        //     .andExpect(jsonPath("$.response").value("Expected response"));

        GenerateRequest request = createValidRequest();
        GenerateResponse mockResponse = createMockResponse();

        // Configure the mocked service
        when(textGeneratorService.generateText(any(GenerateRequest.class))).thenReturn(mockResponse);

        // 2. ACT: Perform the HTTP request & 3. ASSERT: Verify the response
        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value(mockResponse.getResponse()))
                .andExpect(jsonPath("$.model").value(mockResponse.getModel()));

        // Verify the service was called
        verify(textGeneratorService, times(1)).generateText(any(GenerateRequest.class));
    }
    
    @Test
    @DisplayName("Should return 400 for empty prompt")
    void generateText_EmptyPrompt_ReturnsBadRequest() throws Exception {
        // TODO: Implement validation error testing
        //
        // This test should verify that:
        // 1. Empty prompts are rejected with 400 Bad Request
        // 2. The response contains validation error details
        // 3. The service is never called for invalid requests
        //
        // Test scenarios to cover:
        // - Null prompt
        // - Empty string prompt
        // - Whitespace-only prompt
        // - Prompt exceeding maximum length
        //
        // Example structure:
        // GenerateRequest request = new GenerateRequest();
        // request.setPrompt(""); // or null, or very long string
        //
        // mockMvc.perform(post("/generate")...)
        //     .andExpect(status().isBadRequest())
        //     .andExpect(jsonPath("$.error").value("Validation Error"));
        //
        // verify(textGeneratorService, never()).generateText(any());
// This test assumes @NotBlank validation is on the GenerateRequest.prompt field
        GenerateRequest request = new GenerateRequest();

        // Test 1: Null prompt
        request.setPrompt(null);
        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Test 2: Empty prompt
        request.setPrompt("");
        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Test 3: Whitespace-only prompt
        request.setPrompt("   ");
        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify the service was never called
        verify(textGeneratorService, never()).generateText(any());
    }
    
    @Test
    @DisplayName("Should return 500 when service throws exception")
    void generateText_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        // TODO: Implement service error testing
        //
        // This test should verify that:
        // 1. Service exceptions are properly handled
        // 2. HTTP 500 status is returned
        // 3. Error response format is correct
        // 4. Internal error details are not exposed to clients
        //
        // Example structure:
        // GenerateRequest request = new GenerateRequest();
        // request.setPrompt("Valid prompt");
        // when(textGeneratorService.generateText(any()))
        //     .thenThrow(new TextGenerationException("Service error"));
        //
        // mockMvc.perform(post("/generate")...)
        //     .andExpect(status().isInternalServerError())
        //     .andExpect(jsonPath("$.error").value("API Error"));
// 1. ARRANGE: Prepare test data
        GenerateRequest request = createValidRequest();

        // Configure mock to throw an exception
        when(textGeneratorService.generateText(any(GenerateRequest.class)))
                .thenThrow(new TextGenerationException("Service layer failure"));

        // 2. ACT & 3. ASSERT
        // GlobalExceptionHandler now handles the exception and returns proper error response
        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("API Error"))
                .andExpect(jsonPath("$.message").value("Failed to generate text. Please try again later."));

        // Verify the service was called
        verify(textGeneratorService, times(1)).generateText(any(GenerateRequest.class));
    }
    
    @Test
    @DisplayName("Should handle malformed JSON request")
    void generateText_MalformedJson_ReturnsBadRequest() throws Exception {
        // TODO: Implement malformed JSON testing
        //
        // This test should verify that:
        // 1. Malformed JSON is rejected with 400 Bad Request
        // 2. Appropriate error message is returned
        // 3. Service is never called
        //
        // Example:
        // String malformedJson = "{ invalid json }";
        //
        // mockMvc.perform(post("/generate")
        //         .contentType(MediaType.APPLICATION_JSON)
        //         .content(malformedJson))
        //     .andExpect(status().isBadRequest());
        // 1. ARRANGE: Create malformed JSON
        String malformedJson = "{ \"prompt\": \"This JSON is missing a closing brace\"";

        // 2. ACT & 3. ASSERT
        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        // Verify the service was never called
        verify(textGeneratorService, never()).generateText(any());
    }
    
    @Test
    @DisplayName("Should handle missing Content-Type header")
    void generateText_MissingContentType_ReturnsBadRequest() throws Exception {
        // TODO: Implement missing Content-Type testing
        //
        // This test verifies that requests without proper Content-Type
        // are handled appropriately.
        
    }
    
    @Test
    @DisplayName("Should validate prompt length limits")
    void generateText_PromptTooLong_ReturnsBadRequest() throws Exception {
        // TODO: Implement prompt length validation testing
        //
        // Create a prompt that exceeds the maximum length (2000 characters)
        // and verify it's rejected with appropriate validation error.
        //
        // String longPrompt = "a".repeat(2001);
        // GenerateRequest request = new GenerateRequest();
        // request.setPrompt(longPrompt);
// 1. ARRANGE: Create a prompt that exceeds 2000 characters
        // This assumes @Size(max = 2000) on the GenerateRequest.prompt field
        String longPrompt = "a".repeat(2001);
        GenerateRequest request = new GenerateRequest();
        request.setPrompt(longPrompt);

        // 2. ACT & 3. ASSERT
        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify the service was never called
        verify(textGeneratorService, never()).generateText(any());
    }
    
    /**
     * Example: Test different HTTP methods
     */
    @Test
    @DisplayName("Should return 405 for GET request to generate endpoint")
    void generateText_GetMethod_ReturnsMethodNotAllowed() throws Exception {
        // TODO: Verify that GET requests are rejected
        // This teaches students about proper HTTP method usage
        // 1. ACT & 2. ASSERT
        // Perform a GET request to an endpoint that only accepts POST
        mockMvc.perform(get("/generate"))
                .andExpect(status().isMethodNotAllowed()); // 405 Method Not Allowed
    }
    
    /**
     * Example: Test response format and structure
     */
    @Test
    @DisplayName("Should return properly structured JSON response")
    void generateText_ValidRequest_ReturnsWellFormedResponse() throws Exception {
        // TODO: Verify the complete response structure
        // - All required fields are present
        // - Field types are correct
        // - Timestamp format is valid
        // - Response follows API contract
// 1. ARRANGE
        GenerateRequest request = createValidRequest();
        GenerateResponse mockResponse = createMockResponse();
        when(textGeneratorService.generateText(any(GenerateRequest.class))).thenReturn(mockResponse);

        // 2. ACT & 3. ASSERT
        // Verify the complete response structure
        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value(mockResponse.getResponse()))
                .andExpect(jsonPath("$.model").value(mockResponse.getModel()))
                .andExpect(jsonPath("$.tokensUsed").value(mockResponse.getTokensUsed()))
                .andExpect(jsonPath("$.timestamp").exists()) // Check timestamp is present
                .andExpect(jsonPath("$.timestamp").isNotEmpty()); // Check timestamp is not empty
    }
    
    /**
     * Example: Performance testing
     */
    @Test
    @DisplayName("Should handle concurrent requests properly")
    void generateText_ConcurrentRequests_HandlesCorrectly() throws Exception {
        // TODO: Test concurrent request handling
        // This is more advanced but shows how to test thread safety
// 1. ARRANGE
        final int NUMBER_OF_REQUESTS = 10;
        final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_REQUESTS);

        // This latch makes all threads wait until they are all ready to start
        final CountDownLatch startLatch = new CountDownLatch(1);
        // This latch makes the main thread wait until all requests are complete
        final CountDownLatch endLatch = new CountDownLatch(NUMBER_OF_REQUESTS);

        GenerateRequest request = createValidRequest();
        GenerateResponse mockResponse = createMockResponse();

        // Configure the mock service.
        // We add a small delay to increase the chance of a race condition.
        when(textGeneratorService.generateText(any(GenerateRequest.class)))
                .thenAnswer((Answer<GenerateResponse>) invocation -> {
                    Thread.sleep(10); // Simulate some work
                    return mockResponse;
                });

        // 2. ACT
        for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
            executor.submit(() -> {
                try {
                    // Wait for the start signal
                    startLatch.await();

                    // Perform the request
                    mockMvc.perform(post("/generate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.response").value(mockResponse.getResponse()));

                } catch (Exception e) {
                    // Fail the test if any thread throws an exception
                    System.err.println("Concurrent request failed: " + e.getMessage());
                } finally {
                    // Signal that this thread has finished
                    endLatch.countDown();
                }
            });
        }

        // Give threads time to start up and wait on the latch
        Thread.sleep(100);
        // Unleash all threads at once
        startLatch.countDown();

        // Wait for all threads to finish, with a timeout
        boolean allFinished = endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdownNow();

        // 3. ASSERT
        assertTrue(allFinished, "Not all concurrent requests finished in time");

        // Verify the service (our mock) was called exactly once for each request
        verify(textGeneratorService, times(NUMBER_OF_REQUESTS)).generateText(any(GenerateRequest.class));
    }

    @Test
    @DisplayName("Should return 415 for missing Content-Type header")
    void generateText_MissingContentType_ReturnsUnsupportedMediaType() throws Exception {
        // 1. ARRANGE: Create a valid request body
        String requestJson = objectMapper.writeValueAsString(createValidRequest());

        // 2. ACT & 3. ASSERT
        // Perform the POST request *without* the .contentType() header
        mockMvc.perform(post("/generate")
                        .content(requestJson))
                .andExpect(status().isUnsupportedMediaType()); // 415 Unsupported Media Type

        // Verify the service was never called
        verify(textGeneratorService, never()).generateText(any());
    }
    
    // Helper methods for test data creation
    
    /**
     * Creates a valid GenerateRequest for testing
     */
    private GenerateRequest createValidRequest() {
        GenerateRequest request = new GenerateRequest();
        request.setPrompt("Tell me a fun fact about Spring Boot");
        return request;
    }
    
    /**
     * Creates a mock GenerateResponse for testing
     */
    private GenerateResponse createMockResponse() {
        return GenerateResponse.builder()
                .response("Spring Boot was first released in 2014...")
                .model("ollama3.2")
                .timestamp(LocalDateTime.now())
                .tokensUsed(45)
                .build();
    }
    
}