package com.coherentsolutions.homework.week1.service;

import com.coherentsolutions.homework.week1.dto.GenerateRequest;
import com.coherentsolutions.homework.week1.dto.GenerateResponse;
import com.coherentsolutions.homework.week1.exception.TextGenerationException;

/**
 * Service interface for text generation using AI models.
 * 
 * This interface defines the contract for text generation services in the application.
 * It abstracts the underlying AI provider (OpenAI, Anthropic, etc.) and provides
 * a clean separation between the controller layer and the actual AI integration.
 * 
 * Design Patterns Demonstrated:
 * - Service Layer Pattern: Encapsulates business logic
 * - Strategy Pattern: Allows different AI providers to be swapped
 * - Interface Segregation: Focused on single responsibility
 * 
 * Benefits of this interface approach:
 * - Testability: Easy to mock for unit tests
 * - Flexibility: Can switch AI providers without changing controllers
 * - Maintainability: Clear contract and separation of concerns
 * - Extensibility: Easy to add new methods or implementations
 * 
 * Educational Value:
 * This interface demonstrates proper enterprise Java patterns and clean architecture
 * principles. Students learn to think about abstraction and separation of concerns
 * rather than tightly coupling their controllers to specific AI providers.
 * 
 * @author Student Name
 * @version 1.0
 * @see com.coherentsolutions.homework.week1.service.impl.OllamaAITextGeneratorService
 * @see com.coherentsolutions.homework.week1.controller.TextGeneratorController
 */
public interface TextGeneratorService {
    
    /**
     * Generates text using an AI model based on the provided prompt.
     * 
     * This method processes the user's request and returns an AI-generated response.
     * The implementation should handle:
     * - Input validation (though basic validation is done at the DTO level)
     * - AI API communication
     * - Error handling and proper exception translation
     * - Response formatting and metadata population
     * 
     * Implementation Guidelines:
     * - Validate the request even though DTOs have validation
     * - Use structured logging for debugging and monitoring
     * - Handle API rate limits and timeouts gracefully
     * - Include proper error context in exceptions
     * - Consider implementing retry logic for transient failures
     * - Populate response metadata (timestamp, model, tokens used)
     * 
     * Error Scenarios to Handle:
     * - Invalid or empty prompts
     * - AI API authentication failures
     * - AI API rate limiting
     * - Network connectivity issues
     * - AI API service unavailable
     * - Malformed responses from AI API
     * - Token limits exceeded
     * 
     * Performance Considerations:
     * - AI API calls can be slow - consider async processing for batch requests
     * - Implement appropriate timeouts
     * - Consider caching for identical requests (if appropriate)
     * - Monitor token usage to control costs
     * 
     * @param request the text generation request containing the prompt and parameters
     * @return GenerateResponse containing the AI-generated text and metadata
     * @throws TextGenerationException if the text generation fails for any reason
     * @throws IllegalArgumentException if the request is invalid (additional validation)
     * 
     * @see GenerateRequest for request structure and validation rules
     * @see GenerateResponse for response structure and metadata
     * @see TextGenerationException for specific error scenarios
     */
    GenerateResponse generateText(GenerateRequest request) throws TextGenerationException;
    
    // TODO for students: Consider adding additional methods as your application grows
    // 
    // Examples of methods you might add in future iterations:
    // 
    // /**
    //  * Generates text with custom model parameters
    //  */
    // GenerateResponse generateText(GenerateRequest request, ModelParameters parameters);
    // 
    // /**
    //  * Checks if the service is available and properly configured
    //  */
    // boolean isServiceAvailable();
    // 
    // /**
    //  * Returns supported models for this service
    //  */
    // List<String> getSupportedModels();
    // 
    // /**
    //  * Validates a prompt without actually generating text (for cost savings)
    //  */
    // boolean isValidPrompt(String prompt);
    // 
    // /**
    //  * Estimates the cost of a text generation request
    //  */
    // CostEstimate estimateCost(GenerateRequest request);
    // 
    // Benefits of additional methods:
    // 1. Better testability and debugging
    // 2. More flexible configuration
    // 3. Cost optimization capabilities
    // 4. Better user experience
    // 5. Operational monitoring support
}