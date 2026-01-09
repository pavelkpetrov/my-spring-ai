package com.my.spring.ai.bot.controller;

import com.my.spring.ai.bot.dto.GenerateRequest;
import com.my.spring.ai.bot.dto.GenerateResponse;
import com.my.spring.ai.bot.service.TextGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for text generation operations.
 * 
 * This controller provides the HTTP API for generating text using AI models.
 * It demonstrates proper REST API design patterns, request validation,
 * and integration with the service layer.
 * 
 * API Design Patterns:
 * - RESTful endpoints with appropriate HTTP methods
 * - JSON request/response format
 * - Proper HTTP status codes
 * - Request validation using Bean Validation
 * - Error handling through exception propagation
 * 
 * Endpoint Specification:
 * POST /generate
 * - Accepts: JSON payload with prompt
 * - Returns: JSON response with generated text
 * - Status Codes: 200 (success), 400 (validation error), 500 (service error)
 * 
 * Educational Learning Objectives:
 * - Understand REST controller patterns in Spring Boot
 * - Practice request/response DTO design
 * - Learn proper validation and error handling
 * - Demonstrate service layer integration
 * - Apply logging best practices
 * 
 * Grading Criteria Focus:
 * 
 * Correctness (70%):
 * - Endpoint accepts POST requests at /generate
 * - Properly validates incoming JSON requests
 * - Returns appropriate JSON responses
 * - Handles errors gracefully with proper HTTP status codes
 * 
 * Efficiency (30%):
 * - Clean Spring Boot controller patterns
 * - Proper dependency injection
 * - No unnecessary processing or redundant calls
 * - Appropriate logging without performance impact
 * 
 * TODO for students: Complete the controller implementation
 * 
 * @author Student Name
 * @version 1.0
 * @see GenerateRequest
 * @see GenerateResponse
 * @see TextGeneratorService
 */
@RestController
@RequestMapping("/generate")
@RequiredArgsConstructor
@Slf4j
public class TextGeneratorController {
    
    // TODO: Inject the TextGeneratorService dependency
    // Hint: Use final field and @RequiredArgsConstructor for constructor injection
     private final TextGeneratorService textGeneratorService;
    
    /**
     * Generates text based on the provided prompt using AI.
     * 
     * This endpoint accepts a POST request with a JSON payload containing a prompt
     * and returns an AI-generated text response. It demonstrates the complete
     * request-response cycle in a Spring Boot REST API.
     * 
     * TODO for students: Implement this method following these steps:
     * 
     * 1. REQUEST LOGGING
     *    - Log the incoming request (but not sensitive data)
     *    - Use appropriate log level (debug or info)
     *    - Include request context like prompt length
     * 
     * 2. SERVICE CALL
     *    - Call the injected TextGeneratorService
     *    - Pass the validated request to the service
     *    - Let exceptions propagate to GlobalExceptionHandler
     * 
     * 3. RESPONSE LOGGING
     *    - Log successful response generation
     *    - Include response metadata for monitoring
     *    - Don't log the full response content (too verbose)
     * 
     * 4. RETURN RESPONSE
     *    - Wrap GenerateResponse in ResponseEntity
     *    - Use ResponseEntity.ok() for successful responses
     *    - Let Spring handle JSON serialization
     * 
     * Request Example:
     * POST /generate
     * Content-Type: application/json
     * {
     *   "prompt": "Tell me a fun fact about Spring Boot"
     * }
     * 
     * Response Example:
     * HTTP 200 OK
     * Content-Type: application/json
     * {
     *   "response": "Spring Boot was first released in 2014...",
     *   "timestamp": "2024-01-15T10:30:00",
     *   "model": "gpt-3.5-turbo"
     * }
     * 
     * Error Handling:
     * - @Valid annotation triggers validation automatically
     * - Validation errors are handled by GlobalExceptionHandler
     * - Service exceptions are handled by GlobalExceptionHandler
     * - No need for try-catch in this method
     * 
     * @param request the text generation request (validated automatically)
     * @return ResponseEntity containing the generated text response
     */
    @PostMapping
    public ResponseEntity<GenerateResponse> generateText(@Valid @RequestBody GenerateRequest request) {
        // Step 1: Log the incoming request
        log.debug("Received text generation request with prompt length: {}",
                 request.getPrompt() != null ? request.getPrompt().length() : 0);

        // Step 2: Call the service
        GenerateResponse response = textGeneratorService.generateText(request);

        // Step 3: Log successful response
        log.info("Successfully generated text response (length: {})",
                response.getResponse() != null ? response.getResponse().length() : 0);

        // Step 4: Return the response
        return ResponseEntity.ok(response);
    }
    
    // TODO for students: Consider adding additional endpoints as your application grows
    
    /**
     * Health check endpoint to verify the service is operational.
     * 
     * TODO: Implement a simple health check endpoint:
     * - GET /generate/health
     * - Returns simple status message
     * - Can be used by monitoring systems
     * - Should check if OpenAI service is reachable
     * 
     * Example implementation:
     * @GetMapping("/health")
     * public ResponseEntity<Map<String, String>> health() {
     *     Map<String, String> status = new HashMap<>();
     *     status.put("status", "UP");
     *     status.put("service", "text-generator");
     *     return ResponseEntity.ok(status);
     * }
     */
    
    /**
     * Get supported models endpoint.
     * 
     * TODO: Consider adding an endpoint to list supported models:
     * - GET /generate/models
     * - Returns list of available AI models
     * - Helps clients understand capabilities
     * - Can be used for dynamic model selection
     */
    
    // Common Implementation Mistakes to Avoid (for student reference):
    // 1. Don't put business logic in the controller - delegate to service layer
    // 2. Don't handle exceptions here - let GlobalExceptionHandler handle them
    // 3. Don't log sensitive information (full prompts can contain personal data)
    // 4. Don't return null - always return ResponseEntity with proper status
    // 5. Don't forget @Valid annotation for request validation
    // 6. Don't catch and ignore exceptions - let them propagate properly
    // 7. Don't perform expensive operations directly in controller methods
    // 8. Don't hardcode response values - get them from the service
    
    // Best Practices Demonstrated:
    // 1. Constructor injection with @RequiredArgsConstructor
    // 2. Proper logging with structured information
    // 3. Clean separation between controller and service layers
    // 4. Appropriate use of Spring annotations
    // 5. RESTful endpoint design
    // 6. Proper HTTP status code usage
    // 7. JSON request/response handling
    // 8. Request validation with Bean Validation
}