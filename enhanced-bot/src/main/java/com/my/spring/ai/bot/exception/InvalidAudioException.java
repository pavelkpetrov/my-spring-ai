package com.my.spring.ai.bot.exception;

/**
 * Custom exception for text generation failures.
 */
public class InvalidAudioException extends RuntimeException {

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
    public InvalidAudioException(String message) {
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
    public InvalidAudioException(String message, Throwable cause) {
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
    public InvalidAudioException(Throwable cause) {
        super(cause);
    }

}