package com.coherentsolutions.homework.week1.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAI integration using Spring AI.
 * 
 * This class demonstrates proper configuration patterns for AI services in Spring Boot.
 * It configures the ChatClient bean that will be used throughout the application
 * to interact with OpenAI's API.
 * 
 * Configuration Design Patterns:
 * - @Configuration class for centralized configuration
 * - @Bean methods for dependency injection
 * - Builder pattern for flexible client configuration
 * - Externalized configuration via application properties
 * 
 * Spring AI ChatClient:
 * The ChatClient is Spring AI's high-level abstraction for chat-based AI models.
 * It provides a fluent API for creating prompts, setting parameters, and
 * handling responses from various AI providers.
 * 
 * Educational Value:
 * - Demonstrates Spring configuration best practices
 * - Shows how to configure external service clients
 * - Illustrates the builder pattern for complex object creation
 * - Teaches separation of configuration from business logic
 * 
 * Why This Configuration Matters:
 * - Centralizes AI client configuration
 * - Enables dependency injection throughout the application
 * - Allows for easy testing with mock implementations
 * - Provides a single point for AI service configuration changes
 * 
 * TODO for students: Complete the ChatClient configuration
 * 
 * @author Student Name
 * @version 1.0
 * @see com.coherentsolutions.homework.week1.service.impl.OllamaAITextGeneratorService
 */
@Configuration
public class LLMConfig {
    
    /**
     * Configures the ChatClient bean for OpenAI integration.
     * 
     * The ChatClient is the primary interface for interacting with OpenAI's
     * chat completion API through Spring AI. This configuration sets up
     * default parameters and behavior for all AI interactions.
     * 
     * TODO for students: Implement this method following these steps:
     * 
     * 1. USE THE BUILDER PATTERN
     *    - Use ChatClient.builder() to start configuration
     *    - Spring AI auto-configures the underlying OpenAI client
     *    - The API key and basic settings come from application.yml
     * 
     * 2. SET DEFAULT SYSTEM MESSAGE (OPTIONAL)
     *    - Use .defaultSystem() to set a consistent system prompt
     *    - This helps ensure consistent AI behavior across requests
     *    - Example: "You are a helpful assistant that provides concise, accurate responses."
     * 
     * 3. CONFIGURE DEFAULT PARAMETERS (OPTIONAL)
     *    - Temperature, max tokens, etc. can be set here
     *    - These can be overridden per request if needed
     *    - Consider the balance between consistency and flexibility
     * 
     * 4. BUILD AND RETURN
     *    - Call .build() to create the ChatClient instance
     *    - Spring will manage this as a singleton bean
     * 
     * Configuration Philosophy:
     * - Set sensible defaults that work for most use cases
     * - Allow per-request overrides when needed
     * - Keep configuration simple and maintainable
     * - Follow Spring AI best practices
     * 
     * Example Usage in Service:
     * String response = chatClient
     *     .prompt()
     *     .user("Your prompt here")
     *     .call()
     *     .content();
     * 
     * @param builder the ChatClient.Builder provided by Spring AI auto-configuration
     * @return configured ChatClient bean
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {

        // TODO: Implementation template (remove this comment block when implementing):
        /*
        
        return builder
            // Optional: Set a default system message for consistent behavior
            .defaultSystem("You are a helpful assistant that provides concise, accurate responses.")
            // Build the ChatClient
            .build();
            
        */
        
        // Alternative configuration examples (choose one approach):
        
        // Minimal configuration (relies on application.yml for all settings):
        // return builder.build();
        
        // Configuration with default system prompt:
         return builder
             .defaultSystem("You are a helpful assistant.")
             .build();
        
        // Configuration with request-level defaults:
        // return builder
        //     .defaultSystem("You are a helpful assistant.")
        //     .defaultOptions(OpenAiChatOptions.builder()
        //         .withTemperature(0.7)
        //         .withMaxTokens(150)
        //         .build())
        //     .build();
    }
    
    // TODO for students: Consider adding additional configuration as your application grows
    
    /**
     * Example: Configure a custom ChatClient for specific use cases.
     * 
     * TODO: You might want to create specialized ChatClient beans for different purposes:
     * 
     * @Bean
     * @Qualifier("creativeChatClient")
     * public ChatClient creativeChatClient(ChatClient.Builder builder) {
     *     return builder
     *         .defaultSystem("You are a creative writing assistant.")
     *         .defaultOptions(OpenAiChatOptions.builder()
     *             .withTemperature(0.9)  // Higher creativity
     *             .withMaxTokens(500)    // Longer responses
     *             .build())
     *         .build();
     * }
     * 
     * Benefits of multiple ChatClient configurations:
     * - Different AI personalities for different use cases
     * - Optimized parameters for specific tasks
     * - Better cost control with task-specific token limits
     * - Easier A/B testing of different configurations
     */
    
    /**
     * Example: Configure OpenAI-specific options bean.
     * 
     * TODO: If you need fine-grained control over OpenAI parameters:
     * 
     * @Bean
     * public OpenAiChatOptions defaultChatOptions() {
     *     return OpenAiChatOptions.builder()
     *         .withModel("gpt-3.5-turbo")
     *         .withTemperature(0.7)
     *         .withMaxTokens(150)
     *         .withTopP(1.0)
     *         .withFrequencyPenalty(0.0)
     *         .withPresencePenalty(0.0)
     *         .build();
     * }
     * 
     * These options can then be used in your service layer when you need
     * to override the defaults for specific requests.
     */
    
    // Common Configuration Mistakes to Avoid:
    // 1. Don't hardcode API keys in configuration classes
    // 2. Don't set overly restrictive defaults that can't be overridden
    // 3. Don't ignore the builder pattern - it provides flexibility
    // 4. Don't create multiple ChatClient beans unless you need different configurations
    // 5. Don't forget that Spring AI handles most configuration automatically
    // 6. Don't configure sensitive parameters that should be in application.yml
    
    // Best Practices:
    // 1. Use sensible defaults that work for most use cases
    // 2. Document why specific configurations were chosen
    // 3. Keep configuration simple and maintainable
    // 4. Use external configuration (application.yml) for environment-specific settings
    // 5. Consider using @ConfigurationProperties for complex configurations
    // 6. Test your configuration with different scenarios
}