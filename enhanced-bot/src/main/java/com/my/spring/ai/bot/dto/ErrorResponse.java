package com.my.spring.ai.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Error response DTO for handling various error scenarios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    /**
     * High-level error category.
     * Examples: "Validation Error", "API Error", "Internal Server Error"
     * Should be user-friendly but not expose internal implementation details.
     */
    private String error;
    
    /**
     * Human-readable error message.
     * Should be clear and actionable for the API consumer.
     * Avoid technical jargon or internal system details.
     */
    private String message;
    
    /**
     * Optional list of detailed error descriptions.
     * Particularly useful for validation errors where multiple fields may be invalid.
     * Each item should be specific and actionable.
     */
    private List<String> details;
    
    /**
     * Timestamp when the error occurred.
     * Useful for debugging and correlating with server logs.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * The API path where the error occurred.
     * Helps with debugging, especially in applications with multiple endpoints.
     */
    private String path;
    
    /**
     * Creates a simple error response with just error type and message.
     * 
     * @param error the error category
     * @param message the error message
     * @return ErrorResponse with basic error information
     */
    public static ErrorResponse simple(String error, String message) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates an error response for validation failures.
     * 
     * @param message the main validation error message
     * @param details list of specific validation errors
     * @return ErrorResponse formatted for validation errors
     */
    public static ErrorResponse validation(String message, List<String> details) {
        return ErrorResponse.builder()
                .error("Validation Error")
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates an error response for external API failures.
     * 
     * @param message the error message
     * @param path the request path where error occurred
     * @return ErrorResponse formatted for API errors
     */
    public static ErrorResponse apiError(String message, String path) {
        return ErrorResponse.builder()
                .error("API Error")
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates an error response for internal server errors.
     * Should not expose sensitive internal details.
     * 
     * @param path the request path where error occurred
     * @return ErrorResponse formatted for internal errors
     */
    public static ErrorResponse internalError(String path) {
        return ErrorResponse.builder()
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}