package com.coherentsolutions.homework.week1.exception;

import com.coherentsolutions.homework.week1.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for the application.
 * 
 * This class centralizes exception handling across all controllers, providing
 * consistent error responses and proper HTTP status codes. It demonstrates
 * the @ControllerAdvice pattern for cross-cutting concerns.
 * 
 * Exception Handling Strategy:
 * - Business exceptions (TextGenerationException) -> 400 Bad Request or 500 Internal Server Error
 * - Validation exceptions -> 400 Bad Request with detailed field errors
 * - Unexpected exceptions -> 500 Internal Server Error with generic message
 * 
 * Security Considerations:
 * - Never expose internal system details in error messages
 * - Log detailed errors for debugging but return safe messages to clients
 * - Don't leak configuration or sensitive information
 * 
 * Educational Value:
 * - Demonstrates centralized exception handling pattern
 * - Shows proper HTTP status code usage
 * - Illustrates logging best practices for errors
 * - Teaches security-conscious error message design
 * 
 * TODO for students: Complete the exception handling methods
 * 
 * @author Student Name
 * @version 1.0
 * @see ErrorResponse
 * @see TextGenerationException
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handles TextGenerationException thrown by the service layer.
     * 
     * TODO for students: Implement this method following these guidelines:
     * 
     * 1. LOG THE ERROR
     *    - Use appropriate log level (error for unexpected, warn for expected)
     *    - Include request context if available
     *    - Log the full exception stack trace for debugging
     *    - Don't log sensitive information
     * 
     * 2. DETERMINE HTTP STATUS
     *    - Use 400 Bad Request for client errors (invalid input)
     *    - Use 500 Internal Server Error for service failures
     *    - Consider 503 Service Unavailable for temporary failures
     * 
     * 3. CREATE ERROR RESPONSE
     *    - Use ErrorResponse.apiError() for consistent formatting
     *    - Provide user-friendly message (not internal technical details)
     *    - Include the request path for debugging
     * 
     * 4. RETURN RESPONSE ENTITY
     *    - Wrap ErrorResponse in ResponseEntity with appropriate status
     * 
     * @param ex the TextGenerationException that was thrown
     * @param request the web request context
     * @return ResponseEntity with ErrorResponse and appropriate HTTP status
     */
    @ExceptionHandler(TextGenerationException.class)
    public ResponseEntity<ErrorResponse> handleTextGenerationException(
            TextGenerationException ex, WebRequest request) {

        // Log the error for debugging
        log.error("Text generation failed: {}", ex.getMessage(), ex);

        // Create user-friendly error response
        ErrorResponse errorResponse = ErrorResponse.apiError(
            "Failed to generate text. Please try again later.",
            request.getDescription(false)
        );

        // Return with appropriate HTTP status
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles SpeechGenerationException thrown by the service layer.
     *
     * @param ex the SpeechGenerationException that was thrown
     * @param request the web request context
     * @return ResponseEntity with ErrorResponse and appropriate HTTP status
     */
    @ExceptionHandler(SpeechGenerationException.class)
    public ResponseEntity<ErrorResponse> handleSpeechGenerationException(
            SpeechGenerationException ex, WebRequest request) {

        // Log the error for debugging
        log.error("Speech generation failed: {}", ex.getMessage(), ex);

        // Create user-friendly error response
        ErrorResponse errorResponse = ErrorResponse.apiError(
            "Failed to generate speech. Please try again later.",
            request.getDescription(false)
        );

        // Return with appropriate HTTP status
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles SpeechTranscriptionException thrown by the service layer.
     *
     * @param ex the SpeechTranscriptionException that was thrown
     * @param request the web request context
     * @return ResponseEntity with ErrorResponse and appropriate HTTP status
     */
    @ExceptionHandler(SpeechTranscriptionException.class)
    public ResponseEntity<ErrorResponse> handleSpeechTranscriptionException(
            SpeechTranscriptionException ex, WebRequest request) {

        // Log the error for debugging
        log.error("Speech transcription failed: {}", ex.getMessage(), ex);

        // Create user-friendly error response
        ErrorResponse errorResponse = ErrorResponse.apiError(
            "Failed to transcribe speech. Please try again later.",
            request.getDescription(false)
        );

        // Return with appropriate HTTP status
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles InvalidAudioException thrown by the service layer.
     *
     * @param ex the InvalidAudioException that was thrown
     * @param request the web request context
     * @return ResponseEntity with ErrorResponse and appropriate HTTP status
     */
    @ExceptionHandler(InvalidAudioException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAudioException(
            InvalidAudioException ex, WebRequest request) {

        // Log the error for debugging
        log.error("Audio validation failed: {}", ex.getMessage(), ex);

        // Create user-friendly error response
        ErrorResponse errorResponse = ErrorResponse.apiError(
            "Failed to validate speech. Please try again later.",
            request.getDescription(false)
        );

        // Return with appropriate HTTP status
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles validation errors from @Valid annotations on request DTOs.
     * 
     * TODO for students: Implement this method to handle validation failures:
     * 
     * 1. EXTRACT VALIDATION ERRORS
     *    - Get all field errors from the exception
     *    - Create meaningful error messages for each field
     *    - Combine multiple errors into a list
     * 
     * 2. LOG VALIDATION FAILURE
     *    - Use debug or info level (these are expected errors)
     *    - Log the field errors for debugging
     *    - Don't spam logs with full stack traces for validation errors
     * 
     * 3. CREATE VALIDATION ERROR RESPONSE
     *    - Use ErrorResponse.validation() method
     *    - Include all field-specific error messages
     *    - Provide clear guidance on how to fix the issues
     * 
     * 4. RETURN BAD REQUEST
     *    - Always return 400 Bad Request for validation errors
     * 
     * @param ex the validation exception containing field errors
     * @param request the web request context
     * @return ResponseEntity with validation error details and 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        // Extract field errors
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(String.format("%s: %s", error.getField(), error.getDefaultMessage()));
        }

        log.debug("Validation failed for request: {}", errors);

        // Create validation error response
        ErrorResponse errorResponse = ErrorResponse.validation(
            "Request validation failed. Please check your input.",
            errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles HttpMessageNotReadableException for malformed JSON requests.
     *
     * This exception is thrown when the request body cannot be read or parsed,
     * typically due to malformed JSON syntax.
     *
     * @param ex the HttpMessageNotReadableException
     * @param request the web request context
     * @return ResponseEntity with error details and 400 status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        log.debug("Malformed JSON request: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.simple(
            "Bad Request",
            "Malformed JSON request. Please check your request body."
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles HttpRequestMethodNotSupportedException for wrong HTTP methods.
     *
     * This exception is thrown when a request is made with an unsupported HTTP method
     * (e.g., GET when only POST is allowed).
     *
     * @param ex the HttpRequestMethodNotSupportedException
     * @param request the web request context
     * @return ResponseEntity with error details and 405 status
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {

        log.debug("Method not allowed: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.simple(
            "Method Not Allowed",
            String.format("HTTP method '%s' is not supported for this endpoint.", ex.getMethod())
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles HttpMediaTypeNotSupportedException for unsupported media types.
     *
     * This exception is thrown when the Content-Type header is missing or
     * contains an unsupported media type.
     *
     * @param ex the HttpMediaTypeNotSupportedException
     * @param request the web request context
     * @return ResponseEntity with error details and 415 status
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {

        log.debug("Unsupported media type: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.simple(
            "Unsupported Media Type",
            "Content-Type header is missing or unsupported. Please use 'application/json'."
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handles ConstraintViolationException for @RequestParam validation failures.
     *
     * This exception is thrown when validation annotations like @NotEmpty on
     * @RequestParam fail validation.
     *
     * @param ex the ConstraintViolationException
     * @param request the web request context
     * @return ResponseEntity with error details and 400 status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        // Extract constraint violations
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getMessage());
        }

        log.debug("Constraint violation for request: {}", errors);

        // Create validation error response
        ErrorResponse errorResponse = ErrorResponse.validation(
            "Request validation failed. Please check your input.",
            errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles MissingServletRequestParameterException for missing required parameters.
     *
     * This exception is thrown when a required request parameter is missing.
     *
     * @param ex the MissingServletRequestParameterException
     * @param request the web request context
     * @return ResponseEntity with error details and 400 status
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {

        log.debug("Missing required parameter: {}", ex.getParameterName());

        ErrorResponse errorResponse = ErrorResponse.simple(
            "Bad Request",
            String.format("Required parameter '%s' is missing.", ex.getParameterName())
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles any unexpected exceptions that aren't caught by other handlers.
     * 
     * TODO for students: Implement this method as a safety net:
     * 
     * 1. LOG THE UNEXPECTED ERROR
     *    - Use ERROR level since these are unexpected
     *    - Include full stack trace for debugging
     *    - Log request context if available
     * 
     * 2. CREATE GENERIC ERROR RESPONSE
     *    - Don't expose internal error details to the client
     *    - Use ErrorResponse.internalError() for consistency
     *    - Provide generic "try again later" message
     * 
     * 3. RETURN INTERNAL SERVER ERROR
     *    - Always return 500 for unexpected exceptions
     * 
     * Security Note: Never expose internal system details, stack traces, or
     * configuration information in the error response sent to clients.
     * 
     * @param ex the unexpected exception
     * @param request the web request context
     * @return ResponseEntity with generic error message and 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {

        // Log the unexpected error
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        // Create generic error response (don't expose internal details)
        ErrorResponse errorResponse = ErrorResponse.internalError(
            request.getDescription(false)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // TODO for students: Consider adding more specific exception handlers
    
    /**
     * Example: Handle specific Spring framework exceptions
     * 
     * TODO: Add handlers for other common exceptions:
     * - HttpMessageNotReadableException (malformed JSON)
     * - HttpRequestMethodNotSupportedException (wrong HTTP method)
     * - MissingServletRequestParameterException (missing required parameters)
     * - IllegalArgumentException (invalid arguments)
     * 
     * Benefits of specific handlers:
     * 1. More precise error messages for different failure modes
     * 2. Appropriate HTTP status codes for each error type
     * 3. Better debugging information
     * 4. Improved client experience with actionable error messages
     */
    
    // Common Mistakes to Avoid (for student reference):
    // 1. Don't return 200 OK for errors - use proper HTTP status codes
    // 2. Don't expose stack traces or internal details to clients
    // 3. Don't ignore logging - always log errors for debugging
    // 4. Don't use generic error messages - be specific about what went wrong
    // 5. Don't forget to handle validation errors - they're common in REST APIs
    // 6. Don't log at ERROR level for expected business exceptions
    // 7. Don't forget to include request context in error responses
}