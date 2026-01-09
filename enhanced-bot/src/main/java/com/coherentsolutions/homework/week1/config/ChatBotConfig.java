package com.coherentsolutions.homework.week1.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class ChatBotConfig {

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    @Primary
    @Profile("!mcp")
    public ChatModel ollamaPrimaryChatModel(@Qualifier("ollamaChatModel") ChatModel ollamaChatModel) {
        return ollamaChatModel;
    }

    @Bean
    @Primary
    @Profile("mcp")
    public ChatModel bedrockPrimaryChatModel(@Qualifier("bedrockProxyChatModel") ChatModel bedrockChatModel) {
        return bedrockChatModel;
    }

}
