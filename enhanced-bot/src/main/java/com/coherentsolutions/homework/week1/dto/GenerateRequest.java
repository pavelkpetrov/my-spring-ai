package com.coherentsolutions.homework.week1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for text generation endpoint.
 * 
 * This class represents the incoming request structure for the POST /generate endpoint.
 * It includes validation annotations to ensure data integrity and provides meaningful
 * error messages when validation fails.
 * 
 * Validation Rules:
 * - prompt: Required, non-empty, with reasonable length limits
 * 
 * Educational Notes:
 * - Uses Bean Validation (JSR-303) annotations for input validation
 * - Lombok @Data generates getters, setters, toString, equals, and hashCode
 * - Follows DTO pattern: separate from domain models, focused on data transfer
 * 
 * Example JSON:
 * {
 *   "prompt": "Tell me a fun fact about Spring Boot"
 * }
 * 
 * @author Student Name
 * @version 1.0
 * @see com.coherentsolutions.homework.week1.controller.TextGeneratorController
 */
@Data
public class GenerateRequest {
    
    /**
     * The text prompt to send to the AI model for text generation.
     * 
     * Validation:
     * - @NotNull: Prevents null values
     * - @NotBlank: Prevents empty strings and whitespace-only strings
     * - @Size: Limits prompt length to prevent excessive API costs and memory usage
     * 
     * Why these limits?
     * - Min 1: Ensures there's actual content to process
     * - Max 2000: Reasonable limit for most use cases, prevents abuse
     * - Balances flexibility with cost control
     */
    @NotNull(message = "Prompt cannot be null")
    @NotBlank(message = "Prompt cannot be empty or contain only whitespace")
    @Size(min = 1, max = 2000, message = "Prompt must be between 1 and 2000 characters")
    private String prompt;
    
    // TODO for students: Consider adding optional parameters
    // Examples you might want to add:
    // - maxTokens: Allow users to specify response length
    // - temperature: Allow users to control creativity
    // - model: Allow users to choose different models
    // 
    // If you add these, remember to:
    // 1. Add appropriate validation annotations
    // 2. Use reasonable default values
    // 3. Document the purpose of each parameter
    // 4. Consider security implications of user-controlled parameters
}