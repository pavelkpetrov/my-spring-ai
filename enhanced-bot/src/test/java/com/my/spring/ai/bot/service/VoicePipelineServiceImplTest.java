package com.my.spring.ai.bot.service;

import com.my.spring.ai.bot.dto.AudioResponse;
import com.my.spring.ai.bot.exception.SpeechGenerationException;
import com.my.spring.ai.bot.exception.TextGenerationException;
import com.my.spring.ai.bot.service.impl.VoicePipelineServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoicePipelineServiceImplTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SpeechToTextService speechToTextService;

    @Mock
    private TextToSpeechService textToSpeechService;

    private VoicePipelineServiceImpl voicePipelineService;

    @BeforeEach
    void setUp() {
        voicePipelineService = new VoicePipelineServiceImpl(
                chatService,
                speechToTextService,
                textToSpeechService
        );
    }

    @Test
    void processVoiceQuery_whenSpeechToTextFails_shouldThrowTextGenerationException_andNotCallChatOrTTS() {
        // Arrange
        String sessionId = "sid";
        byte[] audioInput = {5, 6};
        when(speechToTextService.speechToText(audioInput))
                .thenThrow(new TextGenerationException("STT failed"));

        // Act & Assert
        TextGenerationException ex = assertThrows(
                TextGenerationException.class,
                () -> voicePipelineService.processVoiceQuery(sessionId, audioInput)
        );
        assertEquals("STT failed", ex.getMessage());
        verify(chatService, never()).chat(anyString(), anyString());
        verify(textToSpeechService, never()).textToSpeech(anyString());
    }

    @Test
    void processVoiceQuery_withNullAudioInput_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                voicePipelineService.processVoiceQuery("sid", null)
        );
    }

    @Test
    void chat_shouldDelegateToChatService_andReturnResponse() {
        String sessionId = "foo";
        String msg = "Hi";
        String expected = "Hello, user!";
        when(chatService.chat(sessionId, msg)).thenReturn(expected);

        String result = voicePipelineService.chat(sessionId, msg);

        assertEquals(expected, result);
        verify(chatService).chat(sessionId, msg);
    }

    @Test
    void testProcessVoiceQuery_Success() throws TextGenerationException, SpeechGenerationException {
        // Given
        String sessionId = "test-session-123";
        byte[] audioInput = new byte[]{1, 2, 3, 4, 5};
        String transcribedText = "Hello, how are you?";
        String chatResponse = "I'm doing well, thank you!";
        byte[] audioOutput = new byte[]{6, 7, 8, 9, 10};

        // Mock the service chain
        when(speechToTextService.speechToText(audioInput)).thenReturn(transcribedText);
        when(chatService.chat(sessionId, transcribedText)).thenReturn(chatResponse);
        when(textToSpeechService.textToSpeech(chatResponse)).thenReturn(audioOutput);

        // When
        AudioResponse result = voicePipelineService.processVoiceQuery(sessionId, audioInput);

        // Then
        assertNotNull(result);
        assertEquals(sessionId, result.getSessionId());
        assertEquals(transcribedText, result.getRequest());
        assertEquals(chatResponse, result.getResponse());
        assertArrayEquals(audioOutput, result.getVoice());

        // Verify the service calls
        verify(speechToTextService).speechToText(audioInput);
        verify(chatService).chat(sessionId, transcribedText);
        verify(textToSpeechService).textToSpeech(chatResponse);
    }

    @Test
    void testProcessVoiceQuery_FailedToConvertSpeechToText() throws TextGenerationException {
        // Given
        String sessionId = "test-session-456";
        byte[] audioInput = new byte[]{1, 2, 3};
        String errorMessage = "Failed to transcribe audio";

        // Mock speechToTextService to throw exception
        when(speechToTextService.speechToText(audioInput))
                .thenThrow(new TextGenerationException(errorMessage));

        // When & Then
        TextGenerationException exception = assertThrows(
                TextGenerationException.class,
                () -> voicePipelineService.processVoiceQuery(sessionId, audioInput)
        );

        assertEquals(errorMessage, exception.getMessage());

        // Verify that speechToTextService was called but subsequent services were not
        verify(speechToTextService).speechToText(audioInput);
        verify(chatService, never()).chat(anyString(), anyString());
        verify(textToSpeechService, never()).textToSpeech(anyString());
    }

    @Test
    void testProcessVoiceQuery_FailedToChat() throws TextGenerationException {
        // Given
        String sessionId = "test-session-789";
        byte[] audioInput = new byte[]{4, 5, 6};
        String transcribedText = "What is the weather?";
        String errorMessage = "Chat service unavailable";

        // Mock services
        when(speechToTextService.speechToText(audioInput)).thenReturn(transcribedText);
        when(chatService.chat(sessionId, transcribedText))
                .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> voicePipelineService.processVoiceQuery(sessionId, audioInput)
        );

        assertEquals(errorMessage, exception.getMessage());

        // Verify service calls
        verify(speechToTextService).speechToText(audioInput);
        verify(chatService).chat(sessionId, transcribedText);
        verify(textToSpeechService, never()).textToSpeech(anyString());
    }

    @Test
    void testProcessVoiceQuery_FailedToConvertTextToSpeech() throws TextGenerationException, SpeechGenerationException {
        // Given
        String sessionId = "test-session-101";
        byte[] audioInput = new byte[]{7, 8, 9};
        String transcribedText = "Tell me a joke";
        String chatResponse = "Why did the chicken cross the road?";
        String errorMessage = "Failed to generate speech";

        // Mock services
        when(speechToTextService.speechToText(audioInput)).thenReturn(transcribedText);
        when(chatService.chat(sessionId, transcribedText)).thenReturn(chatResponse);
        when(textToSpeechService.textToSpeech(chatResponse))
                .thenThrow(new SpeechGenerationException(errorMessage));

        // When & Then
        SpeechGenerationException exception = assertThrows(
                SpeechGenerationException.class,
                () -> voicePipelineService.processVoiceQuery(sessionId, audioInput)
        );

        assertEquals(errorMessage, exception.getMessage());

        // Verify all services were called
        verify(speechToTextService).speechToText(audioInput);
        verify(chatService).chat(sessionId, transcribedText);
        verify(textToSpeechService).textToSpeech(chatResponse);
    }

    @Test
    void testProcessVoiceQuery_WithNullSessionId_GeneratesNewId() throws TextGenerationException, SpeechGenerationException {
        // Given
        byte[] audioInput = new byte[]{1, 2, 3};
        String transcribedText = "Hello";
        String chatResponse = "Hi there!";
        byte[] audioOutput = new byte[]{4, 5, 6};

        // Mock services
        when(speechToTextService.speechToText(audioInput)).thenReturn(transcribedText);
        when(chatService.chat(anyString(), eq(transcribedText))).thenReturn(chatResponse);
        when(textToSpeechService.textToSpeech(chatResponse)).thenReturn(audioOutput);

        // When
        AudioResponse result = voicePipelineService.processVoiceQuery(null, audioInput);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSessionId());
        assertFalse(result.getSessionId().isEmpty());
        assertEquals(transcribedText, result.getRequest());
        assertEquals(chatResponse, result.getResponse());
        assertArrayEquals(audioOutput, result.getVoice());

        // Verify services were called
        verify(speechToTextService).speechToText(audioInput);
        verify(chatService).chat(anyString(), eq(transcribedText));
        verify(textToSpeechService).textToSpeech(chatResponse);
    }

    @Test
    void testChat_DelegatesToChatService() {
        // Given
        String sessionId = "test-session-202";
        String userMessage = "Hello AI";
        String expectedResponse = "Hello User";

        when(chatService.chat(sessionId, userMessage)).thenReturn(expectedResponse);

        // When
        String result = voicePipelineService.chat(sessionId, userMessage);

        // Then
        assertEquals(expectedResponse, result);
        verify(chatService).chat(sessionId, userMessage);
    }

    @Test
    void testClearUserChatHistory_DelegatesToChatService() {
        // Given
        String sessionId = "test-session-303";
        boolean isNew = false;

        // When
        voicePipelineService.clearUserChatHistory(sessionId, isNew);

        // Then
        verify(chatService).clearUserChatHistory(sessionId, isNew);
    }
}