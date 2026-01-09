package com.my.spring.ai.bot.service;

import com.my.spring.ai.bot.dto.AudioResponse;

public interface VoicePipelineService {
    AudioResponse processVoiceQuery(String sessionId, byte[] audioInput);
    String chat(String sessionId, String userMessage);
    void clearUserChatHistory(String sessionId, boolean isNew);
}
