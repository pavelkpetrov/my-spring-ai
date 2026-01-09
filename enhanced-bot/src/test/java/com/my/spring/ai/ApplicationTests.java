package com.my.spring.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for Week1HomeworkApplication
 *
 * These tests verify that the Spring Boot application context loads correctly
 * and all beans are properly configured.
 *
 * We exclude Ollama and ChromaDB autoconfiguration and provide mock implementations
 * to prevent connection attempts during tests.
 * For integration tests that need actual AI functionality, you should start the services via docker-compose first.
 *
 * TODO for students:
 * 1. Add integration tests for your complete application
 * 2. Test that the TextGeneratorController is properly loaded
 * 3. Test that the OpenAI configuration is correct
 * 4. Add tests that verify the application works end-to-end
 *
 * Testing Tips:
 * - Use @SpringBootTest for full application context testing
 * - Use @TestPropertySource to override properties for testing
 * - Consider mocking OpenAI API calls to avoid costs during testing
 * - Test both success and failure scenarios
 */
@SpringBootTest
@EnableAutoConfiguration(
    excludeName = {
        "org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration",
        "org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration",
        "org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration",
        "org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration",
        "org.springframework.ai.autoconfigure.chat.observation.ChatObservationAutoConfiguration",
        "org.springframework.ai.model.bedrock.titan.autoconfigure.BedrockTitanEmbeddingAutoConfiguration"
    }
)
class ApplicationTests {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ToolCallbackProvider toolCallbackProvider() {
            ToolCallbackProvider mock = mock(ToolCallbackProvider.class);
            when(mock.getToolCallbacks()).thenReturn(new ToolCallback[0]);
            return mock;
        }
    }

    @MockBean
    private ChatModel chatModel;

    @MockBean
    private EmbeddingModel embeddingModel;

    @MockBean
    private VectorStore vectorStore;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Basic smoke test to verify the application context loads successfully
     * This test ensures that:
     * - All required beans can be created
     * - Configuration is valid
     * - No circular dependencies exist
     * - Spring Boot auto-configuration works correctly
     */
    @Test
    void contextLoads() {

        assertNotNull(applicationContext, "ApplicationContext must not be null");
        assertNotNull(applicationContext.getBean(ChatModel.class), "ChatModel should be present");

        // If this test passes, it means:
        // 1. Spring Boot can start the application
        // 2. All @Component, @Service, @Controller annotations are working
        // 3. Configuration properties are valid
        // 4. All dependencies can be injected successfully
        // 
        // This is a foundational test - if this fails, check:
        // - Package structure and component scanning
        // - Configuration properties (especially OpenAI API key)
        // - Dependency injection setup
    }

    @Test
    void chatModelBeanExists() {
        assertNotNull(applicationContext.getBean(ChatModel.class), "ChatModel bean should be created");
    }

    // TODO: Add more comprehensive integration tests
    // Example tests to add:
    // - Test that TextGeneratorController bean exists
    // - Test that OpenAI configuration is loaded
    // - Test basic endpoint functionality (with mocked OpenAI)
}

