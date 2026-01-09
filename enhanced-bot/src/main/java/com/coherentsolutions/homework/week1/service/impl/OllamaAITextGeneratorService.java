package com.coherentsolutions.homework.week1.service.impl;

import com.coherentsolutions.homework.week1.dto.GenerateRequest;
import com.coherentsolutions.homework.week1.dto.GenerateResponse;
import com.coherentsolutions.homework.week1.exception.TextGenerationException;
import com.coherentsolutions.homework.week1.service.TextGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;


/**
 * OpenAI implementation of the TextGeneratorService.
 * 
 * This service integrates with OpenAI's GPT models using Spring AI's ChatClient.
 * It demonstrates proper integration patterns, error handling, and logging practices
 * for AI-powered applications.
 * 
 * Grading Criteria Implementation:
 * 
 * Correctness (70%):
 * - Properly calls OpenAI API using Spring AI ChatClient
 * - Returns relevant, properly formatted responses
 * - Handles errors gracefully with meaningful messages
 * - Validates input appropriately
 * 
 * Efficiency (30%):
 * - Makes only necessary API calls (no redundant requests)
 * - Uses proper Spring patterns (dependency injection, service layer)
 * - Handles large prompts appropriately
 * - Implements proper resource usage patterns
 * 
 * Architecture Patterns Demonstrated:
 * - Service Layer Pattern: Business logic separation
 * - Dependency Injection: Loose coupling with ChatClient
 * - Exception Translation: Internal exceptions to domain exceptions
 * - Structured Logging: Proper logging without sensitive data exposure
 * 
 * TODO for students: Complete the implementation of the generateText method
 * 
 * @author Student Name
 * @version 1.0
 * @see TextGeneratorService
 * @see com.coherentsolutions.homework.week1.config.LLMConfig
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaAITextGeneratorService implements TextGeneratorService {
    
    // TODO: Inject the ChatClient dependency
    // Hint: Use final field and @RequiredArgsConstructor for constructor injection
    private final ChatClient chatClient;

    @Value("classpath:/prompts/llama.st")
    private Resource promptResource;
    
    /**
     * Generates text using OpenAI's GPT model via Spring AI ChatClient.
     * 
     * TODO for students: Implement this method following these steps:
     * 
     * 1. INPUT VALIDATION
     *    - Even though DTO validation exists, add service-level validation
     *    - Check for null request
     *    - Validate prompt length and content
     *    - Log the incoming request (but not sensitive data)
     * 
     * 2. API CALL PREPARATION
     *    - Use the injected ChatClient to create a prompt
     *    - Set the user message to the prompt from the request
     *    - Configure any additional parameters if needed
     * 
     * 3. OPENAI API CALL
     *    - Execute the API call using chatClient.prompt().user(prompt).call().content()
     *    - Wrap in try-catch to handle various exception types
     *    - Log the successful API call (without logging the full response to avoid log spam)
     * 
     * 4. RESPONSE BUILDING
     *    - Create GenerateResponse with the AI response
     *    - Set metadata: timestamp, model name, tokens used (if available)
     *    - Use the response builder methods for clean construction
     * 
     * 5. ERROR HANDLING
     *    - Catch specific exceptions and translate to TextGenerationException
     *    - Handle: API key issues, rate limiting, network errors, service unavailable
     *    - Provide meaningful error messages without exposing internal details
     *    - Log errors appropriately for debugging
     * 
     * @param request the text generation request
     * @return GenerateResponse with AI-generated content and metadata
     * @throws TextGenerationException for any generation failures
     */
    @Override
    public GenerateResponse generateText(GenerateRequest request) throws TextGenerationException {

        // Step 1: Input validation
        logRequest(request);

        if (request == null || request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            TextGenerationException error =
                    new TextGenerationException("Invalid request: prompt cannot be null or empty");
            handleApiException(error.getMessage(), error, request);
            throw error;
        }
        
        try {
            // Step 2 & 3: Prepare and execute API call
            String responseStr = chatClient.prompt()
                    .user(u -> {
                        u.text(promptResource);
                        u.param("question",request.getPrompt());
                    })
                    .call()
                    .content();

            // Step 4: Build response
            GenerateResponse response = GenerateResponse.builder()
                .response(responseStr)
                .model("gpt-3.5-turbo") // TODO: Get actual model from configuration
                .timestamp(LocalDateTime.now())
                // TODO: Add token usage if available from API response
                .build();

            logResponse(response, request);

            return response;

        } catch (Exception e) {
            // Step 5: Error handling
            handleApiException(e.getMessage(), e, request);
            throw new TextGenerationException("Failed to generate text: " + e.getMessage(), e);
        }
        
    }
    
    // TODO for students: Consider implementing these helper methods
    
    /**
     * Validates the incoming request at the service level.
     * 
     * TODO: Implement additional validation beyond DTO validation:
     * - Check for potentially harmful prompts
     * - Validate prompt length against cost considerations  
     * - Check for rate limiting scenarios
     * 
     * @param request the request to validate
     * @throws TextGenerationException if validation fails
     */
    private void validateRequest(GenerateRequest request) throws TextGenerationException {
        // TODO: Implement service-level validation
        // Hint: This is beyond basic null/empty checks - think about business rules
    }
    
    /**
     * Handles different types of exceptions that can occur during OpenAI API calls.
     * 
     * TODO: Implement proper exception translation:
     * - API authentication errors -> TextGenerationException with user-friendly message
     * - Rate limiting errors -> TextGenerationException with retry guidance
     * - Network errors -> TextGenerationException with connectivity message
     * - Service unavailable -> TextGenerationException with temporary failure message
     * 
     * @param e the exception to handle
     * @param request the original request for context
     * @throws TextGenerationException the translated exception
     */
    private void handleApiException(String message, Exception e, GenerateRequest request) throws TextGenerationException {
        log.error("Error generating text: {}", message, e);
    }
    
    /**
     * Logs the request for debugging purposes while avoiding sensitive data exposure.
     * 
     * TODO: Implement safe logging:
     * - Log prompt length instead of full prompt content
     * - Include request timestamp
     * - Add any relevant metadata
     * - Never log API keys or other sensitive configuration
     * 
     * @param request the request to log
     */
    private void logRequest(GenerateRequest request) {
        log.debug("Received text generation request with prompt length: {}",
                request == null ? "null" : request.getPrompt() != null ? request.getPrompt().length() : 0);
    }
    
    /**
     * Logs the successful response for monitoring purposes.
     * 
     * TODO: Implement response logging:
     * - Log response length instead of full content
     * - Include generation time if measured
     * - Log token usage for cost tracking
     * - Add success metrics for monitoring
     * 
     * @param response the response to log
     * @param request the original request for context
     */
    private void logResponse(GenerateResponse response, GenerateRequest request) {
        log.info("Successfully generated text response=\n{}\n ; timestamp={} for prompt (length={})",
                response.getResponse(), response.getTimestamp(), request.getPrompt().length());
    }
    
    // Common Mistakes to Avoid (for student reference):
    // 1. Don't log the full prompt or response - this can fill up log files quickly
    // 2. Don't expose API keys or internal configuration in error messages
    // 3. Don't make multiple API calls for a single request
    // 4. Don't ignore different types of exceptions - handle them specifically
    // 5. Don't forget to set response metadata (timestamp, model, etc.)
    // 6. Don't return null - always return a proper response or throw an exception
    // 7. Don't catch and ignore exceptions - always log and rethrow appropriately
}