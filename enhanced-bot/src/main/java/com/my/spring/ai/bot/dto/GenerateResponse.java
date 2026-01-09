package com.my.spring.ai.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for text generation endpoint.
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