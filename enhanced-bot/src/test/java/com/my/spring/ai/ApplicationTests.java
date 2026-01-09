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
 * Integration tests for Application
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

    }

    @Test
    void chatModelBeanExists() {
        assertNotNull(applicationContext.getBean(ChatModel.class), "ChatModel bean should be created");
    }

}

