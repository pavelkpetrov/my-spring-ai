package com.coherentsolutions.homework.week1.service;

import com.coherentsolutions.homework.week1.exception.ConversationNotFoundException;
import com.coherentsolutions.homework.week1.service.impl.ChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    private final static int LAST_ENTRIES_COUNT = 10;

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec prompt;

    @Mock
    private ChatClient.CallResponseSpec call;

    @Captor
    private ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> advisorSpecCaptor;

    @Captor
    private ArgumentCaptor<String> conversationIdCaptor;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        // Mock the builder chain to return the mocked ChatClient
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.defaultAdvisors(any(MessageChatMemoryAdvisor.class))).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        // Instantiate the service under test
        chatService = new ChatServiceImpl(chatMemory, chatClientBuilder, LAST_ENTRIES_COUNT);
    }

    private void mockChatClientChain() {
        // Mock the fluent API calls for the chatClient
        when(chatClient.prompt()).thenReturn(prompt);
        when(prompt.user(anyString())).thenReturn(prompt);
        when(prompt.advisors(any(Consumer.class))).thenReturn(prompt);
        when(prompt.call()).thenReturn(call);
        when(call.content()).thenReturn("Mocked AI Response");
    }

    @Test
    void testChat_WithExistingSessionId() {
        String sessionId = "test-session-123";
        String userMessage = "Hello";
        String aiResponse = "Hi there!";

        // Mock the chat client's fluent API
        when(chatClient.prompt()).thenReturn(prompt);
        when(prompt.user(userMessage)).thenReturn(prompt);
        when(prompt.advisors(any(Consumer.class))).thenReturn(prompt);
        when(prompt.call()).thenReturn(call);
        when(call.content()).thenReturn(aiResponse);

        // Call the method
        String response = chatService.chat(sessionId, userMessage);

        // Verify the response
        assertEquals(aiResponse, response);

        // Verify interactions
        verify(chatClient).prompt();
        verify(prompt).user(userMessage);
        verify(prompt).advisors(advisorSpecCaptor.capture());
        verify(prompt).call();
        verify(call).content();

        // You can optionally inspect the advisor to ensure the conversation ID was set
        // This part is a bit more complex due to the nature of the advisor function
        ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class);
        advisorSpecCaptor.getValue().accept(advisorSpec);
        verify(advisorSpec).param(eq(CHAT_MEMORY_CONVERSATION_ID_KEY), eq(sessionId));
    }

    @Test
    void testChat_WithNullSessionId_GeneratesNewId() {
        String userMessage = "Hello";
        String aiResponse = "Welcome!";

        // Mock the chat client's fluent API
        mockChatClientChain();

        // Call the method
        String response = chatService.chat(null, userMessage);

        // Verify the response
        assertEquals("Mocked AI Response", response);

        // Verify that the advisor was called and capture the spec
        verify(prompt).advisors(advisorSpecCaptor.capture());

        // Execute the captured advisor consumer to verify the CONVERSATION_ID
        ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class);
        advisorSpecCaptor.getValue().accept(advisorSpec);

        // Verify that a new UUID (any string, really) was passed as the conversation ID
        verify(advisorSpec).param(eq(CHAT_MEMORY_CONVERSATION_ID_KEY), anyString());
    }

    @Test
    void testClearUserChatHistory_Success() throws ConversationNotFoundException {
        String sessionId = "session-to-clear";
        // Mock chatMemory.get() to return a non-empty list
        List<Message> mockHistory = List.of(new SystemMessage("test message"));
        when(chatMemory.get(sessionId, LAST_ENTRIES_COUNT)).thenReturn(mockHistory);

        // Call the method
        assertDoesNotThrow(() -> chatService.clearUserChatHistory(sessionId, false));

        // Verify that chatMemory.clear() was called
        verify(chatMemory).get(sessionId, LAST_ENTRIES_COUNT);
        verify(chatMemory).clear(sessionId);
    }

    @Test
    void testClearUserChatHistory_NotFound_NullHistory() {
        String sessionId = "non-existent-session";
        // Mock chatMemory.get() to return null
        when(chatMemory.get(sessionId, LAST_ENTRIES_COUNT)).thenReturn(null);

        // Call the method and assert exception
        ConversationNotFoundException exception = assertThrows(
                ConversationNotFoundException.class,
                () -> chatService.clearUserChatHistory(sessionId, false)
        );

        // Verify the exception message
        assertEquals("Conversation with ID '" + sessionId + "' not found.", exception.getMessage());

        // Verify that chatMemory.clear() was NOT called
        verify(chatMemory).get(sessionId, LAST_ENTRIES_COUNT);
        verify(chatMemory, never()).clear(sessionId);
    }

    @Test
    void testClearUserChatHistory_NotFound_EmptyHistory() {
        String sessionId = "empty-session";
        // Mock chatMemory.get() to return an empty list
        when(chatMemory.get(sessionId, LAST_ENTRIES_COUNT)).thenReturn(Collections.emptyList());

        // Call the method and assert exception
        ConversationNotFoundException exception = assertThrows(
                ConversationNotFoundException.class,
                () -> chatService.clearUserChatHistory(sessionId, false)
        );

        // Verify the exception message
        assertEquals("Conversation with ID '" + sessionId + "' not found.", exception.getMessage());

        // Verify that chatMemory.clear() was NOT called
        verify(chatMemory).get(sessionId, LAST_ENTRIES_COUNT);
        verify(chatMemory, never()).clear(sessionId);
    }

    @Test
    void testClearUserChatHistory_NotFound_New_Chat() {
        String sessionId = "empty-session";
        // Mock chatMemory.get() to return an empty list
        when(chatMemory.get(sessionId, LAST_ENTRIES_COUNT)).thenReturn(Collections.emptyList());

        // Call the method and assert exception
        chatService.clearUserChatHistory(sessionId, true);

        // Verify that chatMemory.clear() was NOT called
        verify(chatMemory).get(sessionId, LAST_ENTRIES_COUNT);
        verify(chatMemory, never()).clear(sessionId);
    }
}
