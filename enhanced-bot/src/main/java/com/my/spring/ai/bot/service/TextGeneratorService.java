package com.my.spring.ai.bot.service;

import com.my.spring.ai.bot.dto.GenerateRequest;
import com.my.spring.ai.bot.dto.GenerateResponse;
import com.my.spring.ai.bot.exception.TextGenerationException;

/**
 * Service interface for text generation using AI models.
 */
public interface TextGeneratorService {
    
    /**
     * Generates text using an AI model based on the provided prompt.
     */
    GenerateResponse generateText(GenerateRequest request) throws TextGenerationException;

}