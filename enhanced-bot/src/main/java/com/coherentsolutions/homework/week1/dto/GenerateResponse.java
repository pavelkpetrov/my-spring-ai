package com.coherentsolutions.homework.week1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for text generation endpoint.
 * 
 * This class represents the response structure returned by the POST /generate endpoint
 * when text generation is successful. It includes the generated text along with 
 * metadata about the generation request.
 * 
 * Design Patterns:
 * - DTO Pattern: Separates internal models from API contract
 * - Builder Pattern: Allows flexible object construction
 * - Immutable-friendly: Can be easily made immutable if needed
 * 
 * Example JSON Response:
 * {
 *   "response": "Spring Boot was first released in 2014 and revolutionized Java development...",
 *   "timestamp": "2024-01-15T10:30:00",
 *   "model": "gpt-3.5-turbo",
 *   "tokensUsed": 45
 * }
 * 
 * Educational Notes:
 * - Including metadata helps with debugging and monitoring
 * - Timestamp allows tracking of response times
 * - Model information helps identify which AI model generated the response
 * - Token usage helps with cost tracking and optimization
 * 
 * @author Student Name
 * @version 1.0
 * @see GenerateRequest
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateResponse {
    
    /**
     * The AI-generated text response.
     * This is the main content returned by the OpenAI API.
     */
    private String response;
    
    /**
     * Timestamp when the response was generated.
     * Useful for debugging, logging, and client-side caching decisions.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * The AI model used to generate the response.
     * Examples: "gpt-3.5-turbo", "gpt-4", etc.
     * Helps with debugging and understanding response characteristics.
     */
    private String model;
    
    /**
     * Number of tokens used in the API call (optional).
     * Includes both prompt tokens and completion tokens.
     * Useful for cost tracking and optimization.
     * 
     * Note: This field is optional - set to null if not available.
     */
    private Integer tokensUsed;
    
    // TODO for students: Consider adding additional metadata
    // Examples you might want to include:
    // - requestId: Unique identifier for tracing requests
    // - processingTimeMs: How long the generation took
    // - temperature: The creativity setting used
    // - maxTokens: The token limit that was configured
    // 
    // Benefits of rich metadata:
    // 1. Better debugging capabilities
    // 2. Performance monitoring
    // 3. Cost analysis
    // 4. User experience insights
    
    /**
     * Convenience constructor for simple responses.
     * Creates a response with just the generated text and current timestamp.
     * 
     * @param response the AI-generated text
     * @return GenerateResponse with basic fields populated
     */
    public static GenerateResponse simple(String response) {
        return GenerateResponse.builder()
                .response(response)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Convenience constructor for responses with model information.
     * 
     * @param response the AI-generated text
     * @param model the model used for generation
     * @return GenerateResponse with response, timestamp, and model
     */
    public static GenerateResponse withModel(String response, String model) {
        return GenerateResponse.builder()
                .response(response)
                .model(model)
                .timestamp(LocalDateTime.now())
                .build();
    }
}