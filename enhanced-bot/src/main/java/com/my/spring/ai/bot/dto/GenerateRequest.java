package com.my.spring.ai.bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for text generation endpoint.
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
}