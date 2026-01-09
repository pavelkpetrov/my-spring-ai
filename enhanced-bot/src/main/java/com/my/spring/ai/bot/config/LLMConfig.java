package com.my.spring.ai.bot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAI integration using Spring AI.
 */
@Configuration
public class LLMConfig {
    
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
         return builder
             .defaultSystem("You are a helpful assistant.")
             .build();
    }
    
}