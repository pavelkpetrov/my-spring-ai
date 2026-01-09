package com.my.spring.ai.bot.service;

import com.my.spring.ai.bot.dto.GenerateRequest;
import com.my.spring.ai.bot.dto.GenerateResponse;
import com.my.spring.ai.bot.exception.TextGenerationException;
import com.my.spring.ai.bot.service.impl.OllamaAITextGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Unit tests for OpenAITextGeneratorService.
 * 
 * This test class demonstrates proper service layer testing with mocked dependencies.
 * It focuses on testing business logic, error handling, and integration with the
 * Spring AI ChatClient while avoiding actual API calls.
 * 
 * Testing Strategy:
 * - Mock ChatClient to avoid real API calls (cost and speed)
 * - Test business logic and error handling thoroughly
 * - Verify proper exception translation
 * - Cover edge cases and validation scenarios
 * 
 * Educational Value:
 * - Shows how to test AI service integrations without API costs
 * - Demonstrates proper mocking of complex objects
 * - Illustrates exception testing patterns
 * - Teaches validation testing at service level
 * - Shows how to verify method calls and arguments
 * 
 * TODO for students: Complete the test implementations
 * 
 * @author Student Name
 * @version 1.0
 * @see com.my.spring.ai.bot.service.impl.OllamaAITextGeneratorService
 */
@ExtendWith(MockitoExtension.class)
class OllamaAITextGeneratorServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private Resource promptResource; // Mock the injected resource

    private OllamaAITextGeneratorService service;

    // --- Mocks for the fluent API chain ---
    @Mock
    private ChatClient.ChatClientRequestSpec promptSpec;
    @Mock
    private ChatClient.PromptUserSpec userSpec;
    @Mock
    private ChatClient.ChatClientRequestSpec callSpec;
    @Mock
    private ChatClient.CallResponseSpec chatResponse;

    @BeforeEach
    void setUp() {
        openMocks(this);

        // TODO: Initialize the service with mocked ChatClient
        // This will require modifying the service to accept ChatClient injection
        // service = new OpenAITextGeneratorService(chatClient);
        
        // For now, we'll throw an exception to remind students to implement
        // Initialize the service with the mocked ChatClient
        service = new OllamaAITextGeneratorService(chatClient);

        // Manually inject the mocked @Value resource field
        // This is necessary because we are not using a Spring Context
        ReflectionTestUtils.setField(service, "promptResource", promptResource);
    }
    
    // TODO for students: Implement these test methods
    
    @Test
    @DisplayName("Should generate text for valid request")
    void generateText_ValidRequest_ReturnsResponse() {
        // TODO: Implement this test following these steps:
        //
        // 1. ARRANGE: Set up test data and mock behavior
        //    - Create a valid GenerateRequest
        //    - Mock the ChatClient chain: chatClient.prompt().user().call().content()
        //    - Define what the mock should return
        //
        // 2. ACT: Call the service method
        //    - Call service.generateText(request)
        //    - Capture the response
        //
        // 3. ASSERT: Verify the results
        //    - Assert the response is not null
        //    - Verify the response content matches expectations
        //    - Check that metadata is properly set (timestamp, model, etc.)
        //    - Verify the ChatClient was called with correct parameters
        //
        // Example structure:
        // GenerateRequest request = new GenerateRequest();
        // request.setPrompt("Test prompt");
        // 
        // when(chatClient.prompt()).thenReturn(promptSpec);
        // when(promptSpec.user(anyString())).thenReturn(callSpec);
        // when(callSpec.call()).thenReturn(chatResponse);
        // when(chatResponse.content()).thenReturn("Generated response");
        //
        // GenerateResponse response = service.generateText(request);
        //
        // assertNotNull(response);
        // assertEquals("Generated response", response.getResponse());
        // verify(chatClient).prompt();
        // 1. ARRANGE: Set up test data and mock behavior
        GenerateRequest request = createValidRequest();
        String mockApiResponse = "This is a mock AI response.";

        // Mock the entire ChatClient fluent API chain
        when(chatClient.prompt()).thenReturn(promptSpec);
        // Handle the lambda passed to .user()
        when(promptSpec.user(any(Consumer.class))).thenAnswer(invocation -> {
            Consumer<ChatClient.PromptUserSpec> userConsumer = invocation.getArgument(0);
            userConsumer.accept(userSpec); // Execute the lambda on our mock UserSpec
            return callSpec; // Return the next step in the chain
        });
        when(callSpec.call()).thenReturn(chatResponse);
        when(chatResponse.content()).thenReturn(mockApiResponse);

        // 2. ACT: Call the service method
        GenerateResponse response = service.generateText(request);

        // 3. ASSERT: Verify the results
        assertNotNull(response);
        assertEquals(mockApiResponse, response.getResponse());
        assertNotNull(response.getTimestamp());
        assertEquals("gpt-3.5-turbo", response.getModel()); // Matches service TODO

        // Verify the ChatClient chain was called correctly
        verify(chatClient, times(1)).prompt();
        verify(promptSpec, times(1)).user(any(Consumer.class));
        verify(callSpec, times(1)).call();
        verify(chatResponse, times(1)).content();

        // Verify the lambda's internal calls
        verify(userSpec, times(1)).text(promptResource);
        verify(userSpec, times(1)).param("question", request.getPrompt());
    }
    
    @Test
    @DisplayName("Should throw exception for null request")
    void generateText_NullRequest_ThrowsException() {
        // TODO: Test null request handling
        //
        // This test should verify that:
        // 1. Null requests are rejected
        // 2. Appropriate exception is thrown
        // 3. ChatClient is never called
        //
        // Example:
        // assertThrows(TextGenerationException.class, () -> {
        //     service.generateText(null);
        // });
        // verify(chatClient, never()).prompt();
        // 1. ARRANGE: (request is null)

        // 2. ACT & 3. ASSERT
        TextGenerationException exception = assertThrows(
                TextGenerationException.class,
                () -> service.generateText(null)
        );

        assertTrue(exception.getMessage().contains("Invalid request"));

        // Verify ChatClient was never called
        verify(chatClient, never()).prompt();
    }
    
    @Test
    @DisplayName("Should throw exception for empty prompt")
    void generateText_EmptyPrompt_ThrowsException() {
        // TODO: Test empty prompt handling
        //
        // Create a request with empty/null prompt and verify
        // that appropriate validation exception is thrown.
        // 1. ARRANGE: Create request with null prompt
        GenerateRequest requestNull = new GenerateRequest();
        requestNull.setPrompt(null);

        // 2. ACT & 3. ASSERT (for null prompt)
        TextGenerationException exceptionNull = assertThrows(
                TextGenerationException.class,
                () -> service.generateText(requestNull)
        );
        assertTrue(exceptionNull.getMessage().contains("Invalid request"));

        // 1. ARRANGE: Create request with empty prompt
        GenerateRequest requestEmpty = new GenerateRequest();
        requestEmpty.setPrompt("");

        // 2. ACT & 3. ASSERT (for empty prompt)
        TextGenerationException exceptionEmpty = assertThrows(
                TextGenerationException.class,
                () -> service.generateText(requestEmpty)
        );
        assertTrue(exceptionEmpty.getMessage().contains("Invalid request"));

        // Verify ChatClient was never called
        verify(chatClient, never()).prompt();
    }
    
    @Test
    @DisplayName("Should handle ChatClient exceptions gracefully")
    void generateText_ChatClientThrowsException_ThrowsTextGenerationException() {
        // TODO: Test exception handling from ChatClient
        //
        // This test should verify that:
        // 1. Exceptions from ChatClient are caught
        // 2. They are translated to TextGenerationException
        // 3. Original exception is preserved as cause
        // 4. User-friendly error message is provided
        //
        // Example:
        // GenerateRequest request = createValidRequest();
        // when(chatClient.prompt()).thenThrow(new RuntimeException("API Error"));
        //
        // TextGenerationException exception = assertThrows(
        //     TextGenerationException.class, 
        //     () -> service.generateText(request)
        // );
        // 
        // assertNotNull(exception.getCause());
        // assertTrue(exception.getMessage().contains("user-friendly message"));
        // 1. ARRANGE: Create request with null prompt
        // 1. ARRANGE: Create a valid request
        GenerateRequest request = createValidRequest();
        RuntimeException apiError = new RuntimeException("API Error");

        // Mock the ChatClient to throw an exception
        when(chatClient.prompt()).thenThrow(apiError);

        // 2. ACT & 3. ASSERT
        TextGenerationException exception = assertThrows(
                TextGenerationException.class,
                () -> service.generateText(request)
        );

        // Verify the exception is wrapped
        assertEquals("Failed to generate text: API Error", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals(apiError, exception.getCause());
    }
    
    @Test
    @DisplayName("Should handle authentication errors appropriately")
    void generateText_AuthenticationError_ThrowsAppropriateException() {
        // TODO: Test authentication error handling
        //
        // Simulate an authentication error (invalid API key) and verify
        // that it's handled with an appropriate user-friendly message.
        // 1. ARRANGE
        GenerateRequest request = createValidRequest();
        IllegalArgumentException authError = new IllegalArgumentException("Invalid API Key");
        when(chatClient.prompt()).thenThrow(authError);

        // 2. ACT & 3. ASSERT
        TextGenerationException exception = assertThrows(
                TextGenerationException.class,
                () -> service.generateText(request)
        );

        // Current behavior: Generic wrap
        assertEquals("Failed to generate text: Invalid API Key", exception.getMessage());
        assertEquals(authError, exception.getCause());
    }

    // This is a placeholder exception for the test
    private static class RateLimitException extends RuntimeException {
        public RateLimitException(String message) { super(message); }
    }

    @Test
    @DisplayName("Should handle rate limiting errors")
    void generateText_RateLimitError_ThrowsAppropriateException() {
        // TODO: Test rate limiting error handling
        //
        // Simulate a rate limiting error and verify proper handling
        // with guidance for the user to retry later.
        // 1. ARRANGE
        GenerateRequest request = createValidRequest();
        RateLimitException rateLimitError = new RateLimitException("Rate limit exceeded");
        when(chatClient.prompt()).thenThrow(rateLimitError);

        // 2. ACT & 3. ASSERT
        TextGenerationException exception = assertThrows(
                TextGenerationException.class,
                () -> service.generateText(request)
        );

        // Current behavior: Generic wrap
        assertEquals("Failed to generate text: Rate limit exceeded", exception.getMessage());
        assertEquals(rateLimitError, exception.getCause());
    }
    
    @Test
    @DisplayName("Should validate prompt length at service level")
    void generateText_PromptTooLong_ThrowsException() {
        // TODO: Test service-level prompt validation
        //
        // Even though DTO validation exists, the service should have
        // additional business rule validation.
        
    }
    
    @Test
    @DisplayName("Should set response metadata correctly")
    void generateText_ValidRequest_SetsMetadataCorrectly() {
        // TODO: Test that response metadata is properly populated
        //
        // Verify that the response includes:
        // - Timestamp (recent)
        // - Model name
        // - Token usage (if available)
        // - Other relevant metadata
        
    }
    
    @Test
    @DisplayName("Should handle empty response from ChatClient")
    void generateText_EmptyResponseFromChatClient_HandlesGracefully() {
        // TODO: Test handling of empty responses
        //
        // What should happen if ChatClient returns empty or null content?
        // Should it throw an exception or return a default message?
        
    }
    
    @Test
    @DisplayName("Should log requests and responses appropriately")
    void generateText_ValidRequest_LogsAppropriately() {
        // TODO: Test logging behavior
        //
        // This is more advanced testing, but you can verify that:
        // 1. Requests are logged (without sensitive data)
        // 2. Responses are logged (without full content)
        // 3. Errors are logged with appropriate level
        //
        // You might use a custom appender or verify log statements
        
    }
    
    // TODO for students: Add integration tests
    
    /**
     * Example: Integration test with real ChatClient (optional)
     * 
     * This would be a separate test class or marked with @IntegrationTest
     * to distinguish from unit tests.
     */
    @Test
    @DisplayName("Integration: Should work with real ChatClient")
    void generateText_RealChatClient_ReturnsActualResponse() {
        // TODO: Create an integration test that uses a real ChatClient
        // 
        // Note: This test should:
        // 1. Be marked as integration test (separate profile)
        // 2. Use real API key (from test environment)
        // 3. Be skipped in CI/CD if no API key available
        // 4. Use minimal token limits to control costs
        //
        // @Profile("integration")
        // @Disabled("Requires real API key and incurs costs")
        
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
     * Creates a request with invalid data for testing
     */
    private GenerateRequest createInvalidRequest() {
        GenerateRequest request = new GenerateRequest();
        request.setPrompt(""); // or null
        return request;
    }
    
    /**
     * Creates a very long prompt for testing length limits
     */
    private GenerateRequest createLongPromptRequest() {
        GenerateRequest request = new GenerateRequest();
        request.setPrompt("a".repeat(3000)); // Exceeds reasonable limits
        return request;
    }
    
}