package com.my.spring.ai.bot.exception;

import com.my.spring.ai.bot.dto.ErrorResponse;
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
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handles TextGenerationException thrown by the service layer.
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
    
}