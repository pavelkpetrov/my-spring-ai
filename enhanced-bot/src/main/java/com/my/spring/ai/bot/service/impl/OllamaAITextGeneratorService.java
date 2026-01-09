package com.my.spring.ai.bot.service.impl;

import com.my.spring.ai.bot.dto.GenerateRequest;
import com.my.spring.ai.bot.dto.GenerateResponse;
import com.my.spring.ai.bot.exception.TextGenerationException;
import com.my.spring.ai.bot.service.TextGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;


/**
 * OpenAI implementation of the TextGeneratorService.
 * 
 * This service integrates with OpenAI's GPT models using Spring AI's ChatClient.
 * It demonstrates proper integration patterns, error handling, and logging practices
 * for AI-powered applications.
 * 
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaAITextGeneratorService implements TextGeneratorService {
    
    // TODO: Inject the ChatClient dependency
    // Hint: Use final field and @RequiredArgsConstructor for constructor injection
    private final ChatClient chatClient;

    @Value("classpath:/prompts/llama.st")
    private Resource promptResource;
    
    /**
     * Generates text using OpenAI's GPT model via Spring AI ChatClient.
     * 
     */
    @Override
    public GenerateResponse generateText(GenerateRequest request) throws TextGenerationException {

        // Step 1: Input validation
        logRequest(request);

        if (request == null || request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            TextGenerationException error =
                    new TextGenerationException("Invalid request: prompt cannot be null or empty");
            handleApiException(error.getMessage(), error, request);
            throw error;
        }
        
        try {
            // Step 2 & 3: Prepare and execute API call
            String responseStr = chatClient.prompt()
                    .user(u -> {
                        u.text(promptResource);
                        u.param("question",request.getPrompt());
                    })
                    .call()
                    .content();

            // Step 4: Build response
            GenerateResponse response = GenerateResponse.builder()
                .response(responseStr)
                .model("gpt-3.5-turbo") // TODO: Get actual model from configuration
                .timestamp(LocalDateTime.now())
                // TODO: Add token usage if available from API response
                .build();

            logResponse(response, request);

            return response;

        } catch (Exception e) {
            // Step 5: Error handling
            handleApiException(e.getMessage(), e, request);
            throw new TextGenerationException("Failed to generate text: " + e.getMessage(), e);
        }
        
    }
    
    /**
     * Validates the incoming request at the service level.
     */
    private void validateRequest(GenerateRequest request) throws TextGenerationException {
        // TODO: Implement service-level validation
        // Hint: This is beyond basic null/empty checks - think about business rules
    }
    
    /**
     * Handles different types of exceptions that can occur during OpenAI API calls.
     */
    private void handleApiException(String message, Exception e, GenerateRequest request) throws TextGenerationException {
        log.error("Error generating text: {}", message, e);
    }
    
    /**
     * Logs the request for debugging purposes while avoiding sensitive data exposure.
     * 
     */
    private void logRequest(GenerateRequest request) {
        log.debug("Received text generation request with prompt length: {}",
                request == null ? "null" : request.getPrompt() != null ? request.getPrompt().length() : 0);
    }
    
    /**
     * Logs the successful response for monitoring purposes.
     */
    private void logResponse(GenerateResponse response, GenerateRequest request) {
        log.info("Successfully generated text response=\n{}\n ; timestamp={} for prompt (length={})",
                response.getResponse(), response.getTimestamp(), request.getPrompt().length());
    }
    
}