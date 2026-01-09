package com.coherentsolutions.homework.week1.exception;

/**
 * Custom exception for text generation failures.
 * 
 * This exception is thrown when text generation operations fail for various reasons
 * such as API communication errors, invalid requests, or service unavailability.
 * It provides a clean abstraction over underlying technical exceptions.
 * 
 * Exception Design Principles:
 * - Meaningful Names: The exception name clearly indicates what went wrong
 * - Proper Inheritance: Extends RuntimeException for unchecked exception behavior
 * - Message Clarity: Provides user-friendly error messages
 * - Cause Preservation: Maintains the original exception for debugging
 * 
 * When to Use This Exception:
 * - OpenAI API authentication failures
 * - Network connectivity issues
 * - Invalid API responses
 * - Rate limiting exceeded
 * - Service configuration errors
 * - Prompt processing failures
 * 
 * Exception Handling Strategy:
 * This is a RuntimeException, so it doesn't need to be declared in method signatures.
 * It will be caught by the GlobalExceptionHandler and converted to appropriate
 * HTTP responses for the client.
 * 
 * Educational Value:
 * - Demonstrates proper custom exception design
 * - Shows how to abstract technical details from business logic
 * - Illustrates exception handling best practices
 * - Teaches separation of concerns in error handling
 * 
 * @author Student Name
 * @version 1.0
 * @see com.coherentsolutions.homework.week1.exception.GlobalExceptionHandler
 */
public class TextGenerationException extends RuntimeException {
    
    /**
     * Constructs a new TextGenerationException with the specified detail message.
     * 
     * Use this constructor when you have a specific error message but no underlying cause.
     * The message should be user-friendly and actionable.
     * 
     * Example:
     * throw new TextGenerationException("Prompt cannot be empty");
     * 
     * @param message the detail message explaining what went wrong
     */
    public TextGenerationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new TextGenerationException with the specified detail message and cause.
     * 
     * Use this constructor when wrapping a lower-level exception. This preserves the
     * original exception for debugging while providing a business-friendly message.
     * 
     * Example:
     * catch (HttpClientException e) {
     *     throw new TextGenerationException("Failed to connect to OpenAI API", e);
     * }
     * 
     * @param message the detail message explaining what went wrong  
     * @param cause the underlying exception that caused this error
     */
    public TextGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new TextGenerationException with the specified cause.
     * 
     * Use this constructor when the underlying exception's message is sufficient
     * and you just need to change the exception type for proper handling.
     * 
     * Example:
     * catch (SpecificApiException e) {
     *     throw new TextGenerationException(e);
     * }
     * 
     * @param cause the underlying exception that caused this error
     */
    public TextGenerationException(Throwable cause) {
        super(cause);
    }
    
    // TODO for students: Consider adding more specific exception types as your application grows
    // 
    // Examples of more specific exceptions you might create:
    // - ApiKeyInvalidException extends TextGenerationException
    // - RateLimitExceededException extends TextGenerationException  
    // - ServiceUnavailableException extends TextGenerationException
    // - InvalidPromptException extends TextGenerationException
    // - TokenLimitExceededException extends TextGenerationException
    // 
    // Benefits of specific exception types:
    // 1. More granular error handling in the exception handler
    // 2. Different HTTP status codes for different error types
    // 3. Specific retry strategies for different failure modes
    // 4. Better monitoring and alerting capabilities
    // 5. More actionable error messages for users
    // 
    // Example implementation:
    // public static class ApiKeyInvalidException extends TextGenerationException {
    //     public ApiKeyInvalidException(String message) {
    //         super(message);
    //     }
    // }
}