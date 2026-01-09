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
 */
@RestController
@RequestMapping("/generate")
@RequiredArgsConstructor
@Slf4j
public class TextGeneratorController {
    
    // TODO: Inject the TextGeneratorService dependency
    // Hint: Use final field and @RequiredArgsConstructor for constructor injection
     private final TextGeneratorService textGeneratorService;
    
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

}