package com.my.spring.ai.bot.service.impl;

import com.my.spring.ai.bot.dto.AudioResponse;
import com.my.spring.ai.bot.service.ChatService;
import com.my.spring.ai.bot.service.SpeechToTextService;
import com.my.spring.ai.bot.service.TextToSpeechService;
import com.my.spring.ai.bot.service.VoicePipelineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Profile("voice")
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class VoicePipelineServiceImpl implements VoicePipelineService {

    private final ChatService chatService;
    private final SpeechToTextService speechToTextService;
    private final TextToSpeechService textToSpeechService;

    @Override
    public AudioResponse processVoiceQuery(String sessionId, byte[] audioInput) {

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        log.info("Start processVoiceQuery. Start speechToText sessionId={}, requestBytesLength={}",
                sessionId,  audioInput == null ? 0 : audioInput.length);

        if (audioInput == null || audioInput.length == 0) {
            throw new IllegalArgumentException("audioInput must not be null or empty");
        }

        String requestText = speechToTextService.speechToText(audioInput);

        log.debug("Start chat sessionId={}, requestText={}", sessionId,  requestText);

        if (requestText == null || requestText.trim().isEmpty()) {
            throw new IllegalStateException("Speech-to-text result is empty");
        }

        String responseText = chatService.chat(sessionId, requestText);

        log.debug("Start speech generation sessionId={}, requestText={}, responseText={}", sessionId,  requestText, responseText);

        if (responseText == null || responseText.trim().isEmpty()) {
            throw new IllegalStateException("Chat response is empty");
        }

        byte[] responseVoice = textToSpeechService.textToSpeech(responseText);

        log.debug("Create full response sessionId={}, requestText={}, responseText={}, responseBytesLength={}",
                sessionId,  requestText, responseText, responseVoice == null ? 0 : responseVoice.length);

        if (responseVoice == null || responseVoice.length == 0) {
            throw new IllegalStateException("Text-to-speech output is empty");
        }

        return new AudioResponse(responseVoice, responseText, requestText, sessionId);
    }

    @Override
    public String chat(String sessionId, String userMessage) {
        return chatService.chat(sessionId, userMessage);
    }

    @Override
    public void clearUserChatHistory(String sessionId, boolean isNew) {
        chatService.clearUserChatHistory(sessionId, isNew);
    }
}
