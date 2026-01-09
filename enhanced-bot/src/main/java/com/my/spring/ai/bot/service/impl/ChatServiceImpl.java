package com.my.spring.ai.bot.service.impl;

import com.my.spring.ai.bot.exception.ConversationNotFoundException;
import com.my.spring.ai.bot.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

/**
 * Orchestrates the chat logic, interacting with the AI and ConversationService.
 */
@Slf4j // Using Lombok for logging
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    // Define the system message to set the bot's behavior
    private static final String SYSTEM_MESSAGE_CONTENT = """
             You are a helpful assistant that provides contextual responses.
                    Consider the user's location and preferences when responding.
                    Be concise but informative.
            """;


    private int lastEntriesCount;

    public ChatServiceImpl(ChatMemory chatMemory, ChatClient.Builder chatClientBuilder,
                           @Value("${chat.memory.history-window:10}") int lastEntriesCount) {
        this.chatMemory = chatMemory;
        this.lastEntriesCount = lastEntriesCount;

        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_MESSAGE_CONTENT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(this.chatMemory).build()
                )
                .build();

        log.info("ChatClient configured with LoggingAdvisor for prompt/completion logging");
    }

    /**
     * Handles a user's chat message, maintains context, and returns the AI's response.
     *
     * @param sessionId   The ID of the conversation.
     * @param userMessage The content of the user's message.
     * @return The AI's response content.
     */
    @Override
    public String chat(String sessionId, String userMessage) {
        log.debug("Chat request for sessionId={}: {}", sessionId, userMessage);

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
            log.info("No session ID provided. Generated new session: {}", sessionId);
        }

        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("User message cannot be null or empty");
        }

        try {
            var userId = sessionId;
            String response = this.chatClient.prompt()
                    .user(userMessage)
                    .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, userId))
                    .call()
                    .content();
            log.debug("Chat response from prompt for sessionId={}: {}", sessionId, response);
            return response;
        } catch (Exception e) {
            log.error("Error during chat completion for sessionId={}", sessionId, e);
            throw new RuntimeException("AI service failed. Please try again later.", e);
        }
    }

    @Override
    public void clearUserChatHistory(String sessionId, boolean isNew) {
        // Retrieve the history first to check for existence
        List<Message> history = chatMemory.get(sessionId, lastEntriesCount);

        // If the history is null or empty, we'll assume the session doesn't exist.
        // This is the most reliable check available from the standard ChatMemory interface.
        if (history == null || history.isEmpty()) {
            if (isNew) {
                log.debug("No existing chat history for new sessionId: {}", sessionId);
                return;
            }
            log.warn("Attempted to clear non-existent chat history for session ID: {}", sessionId);
            // Throw the custom exception as requested
            throw new ConversationNotFoundException("Conversation with ID '" + sessionId + "' not found.");
        }

        // If history exists (is not null and not empty), clear it
        log.info("Clearing chat history for session ID: {}", sessionId);
        chatMemory.clear(sessionId);
    }
}
