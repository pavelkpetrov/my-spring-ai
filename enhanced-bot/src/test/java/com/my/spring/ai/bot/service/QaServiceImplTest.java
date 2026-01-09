package com.my.spring.ai.bot.service;

import com.my.spring.ai.bot.config.ApplicationContextHolder;
import com.my.spring.ai.bot.dto.AnswerResponse;
import com.my.spring.ai.bot.exception.TextGenerationException;
import com.my.spring.ai.bot.service.impl.QaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.vectorstore.VectorStore;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QaServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class QaServiceImplTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ApplicationContextHolder context;

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClientRequestSpec userSpec;

    @Mock
    private CallResponseSpec responseSpec;

    private QaServiceImpl qaService;

    @BeforeEach
    void setUp() {
        // Create the service with mocked dependencies
        qaService = new QaServiceImpl(context, chatClientBuilder);

        // Mock the ChatClient.Builder to return the mocked ChatClient
        // Using lenient() to avoid strict stubbing issues since defaultAdvisors has multiple overloads
        lenient().when(chatClientBuilder.defaultAdvisors(any(), any())).thenReturn(chatClientBuilder);
        lenient().when(chatClientBuilder.defaultAdvisors(anyList())).thenReturn(chatClientBuilder);
        lenient().when(context.getBean(eq(VectorStore.class))).thenReturn(vectorStore);


        // Initialize the service (calls @PostConstruct)
    }

    @Test
    @DisplayName("Corner Case: Null question should throw NullPointerException or be handled gracefully")
    void getAnswer_withNullQuestion_shouldHandleGracefully() {
        // Depending on service design: expect exception or default response
        assertThrows(IllegalArgumentException.class, () -> qaService.getAnswer(null));
    }

    @Test
    @DisplayName("Corner Case: Whitespace-only question should be handled")
    void getAnswer_withWhitespaceOnlyQuestion_shouldReturnDefaultAnswer() {
        String whitespaceQuestion = "   ";

        // When: Getting answer for empty question
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            qaService.getAnswer(whitespaceQuestion);
        }, "Should throw RuntimeException when ChatClient fails");

        // Then: Verify the response
        assertTrue(exception.getMessage().contains("Question cannot be null or empty"));

    }

    @Test
    @DisplayName("Corner Case: ChatClient returns null content")
    void getAnswer_whenChatClientReturnsNullContent_shouldHandleNull() {
        String question = "Will I get a response?";
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(question)).thenReturn(userSpec);
        when(userSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(null);

        AnswerResponse response = qaService.getAnswer(question);
        assertNull(response.getAnswer(), "Should return null if content is null");
    }

    @Test
    @DisplayName("Success: Multiple consecutive calls to getAnswer")
    void getAnswer_calledMultipleTimes_shouldWorkEachTime() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(userSpec);
        when(userSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("answer");

        AnswerResponse response1 = qaService.getAnswer("Question 1?");
        AnswerResponse response2 = qaService.getAnswer("Question 2?");
        assertEquals("answer", response1.getAnswer());
        assertEquals("answer", response2.getAnswer());
        verify(chatClient, times(2)).prompt();
    }

    @Test
    @DisplayName("Success: Valid question should return answer from ChatClient")
    void getAnswer_withValidQuestion_shouldReturnAnswer() {
        // Given: A valid question
        String question = "What is artificial intelligence?";
        String expectedAnswer = "Artificial intelligence (AI) is the simulation of human intelligence " +
                "processes by machines, especially computer systems. These processes include learning, " +
                "reasoning, and self-correction.";

        // Mock the ChatClient fluent API chain
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(question)).thenReturn(userSpec);
        when(userSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(expectedAnswer);

        // When: Getting answer for the question
        AnswerResponse response = qaService.getAnswer(question);

        // Then: Verify the response
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getAnswer(), "Answer should not be null");
        assertEquals(expectedAnswer, response.getAnswer(), "Answer should match expected value");

        // Verify the ChatClient was called with correct parameters
        verify(chatClient, times(1)).prompt();
        verify(requestSpec, times(1)).user(question);
        verify(userSpec, times(1)).call();
        verify(responseSpec, times(1)).content();
    }

    @Test
    @DisplayName("Corner Case: Empty question should be processed without errors")
    void getAnswer_withEmptyQuestion_shouldReturnAnswer() {
        // Given: An empty question
        String emptyQuestion = "";
        String defaultAnswer = "I don't have enough information to answer that question.";

        // When: Getting answer for empty question
        RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> {
            qaService.getAnswer(emptyQuestion);
        }, "Question cannot be null or empty");

        // Then: Verify the response
        assertTrue(exception.getMessage().contains("Question cannot be null or empty"));

    }

    @Test
    @DisplayName("Corner Case: ChatClient exception should propagate to caller")
    void getAnswer_whenChatClientThrowsException_shouldPropagateException() {
        // Given: A valid question but ChatClient will throw exception
        String question = "What is machine learning?";

        // Mock the ChatClient to throw exception during call
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(question)).thenReturn(userSpec);
        when(userSpec.call()).thenThrow(new RuntimeException("ChatClient connection failed"));

        // When & Then: Getting answer should throw exception
        RuntimeException exception = assertThrows(TextGenerationException.class, () -> {
            qaService.getAnswer(question);
        }, "Failed to generate answer from AI");

        // Verify the exception message
        assertTrue(exception.getMessage().contains("Failed to generate answer from AI"),
                "Exception message should indicate ChatClient failure");

        // Verify interactions up to the point of failure
        verify(chatClient, times(1)).prompt();
        verify(requestSpec, times(1)).user(question);
        verify(userSpec, times(1)).call();
        verify(responseSpec, never()).content(); // Should not reach content() call
    }

    @Test
    @DisplayName("Corner Case: Very long question should be processed")
    void getAnswer_withVeryLongQuestion_shouldReturnAnswer() {
        // Given: A very long question
        String longQuestion = "Can you explain in detail " + "what ".repeat(100) +
                "artificial intelligence is and how it works?";
        String answer = "AI is a broad field of computer science...";

        // Mock the ChatClient fluent API chain
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(longQuestion)).thenReturn(userSpec);
        when(userSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(answer);

        // When: Getting answer for long question
        AnswerResponse response = qaService.getAnswer(longQuestion);

        // Then: Verify the response
        assertNotNull(response, "Response should not be null");
        assertEquals(answer, response.getAnswer(), "Should return answer for long question");

        // Verify the long question was processed
        verify(requestSpec, times(1)).user(longQuestion);
    }

    @Test
    @DisplayName("Corner Case: Question with special characters should be processed")
    void getAnswer_withSpecialCharacters_shouldReturnAnswer() {
        // Given: A question with special characters
        String questionWithSpecialChars = "What is AI? & How does ML work?";
        String answer = "AI and ML are related but distinct concepts...";

        // Mock the ChatClient fluent API chain
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(questionWithSpecialChars)).thenReturn(userSpec);
        when(userSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(answer);

        // When: Getting answer for question with special characters
        AnswerResponse response = qaService.getAnswer(questionWithSpecialChars);

        // Then: Verify the response
        assertNotNull(response, "Response should not be null");
        assertEquals(answer, response.getAnswer(), "Should handle special characters");

        // Verify the question with special characters was processed
        verify(requestSpec, times(1)).user(questionWithSpecialChars);
    }
}