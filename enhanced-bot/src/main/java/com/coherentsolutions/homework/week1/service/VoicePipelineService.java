package com.coherentsolutions.homework.week1.service;

import com.coherentsolutions.homework.week1.dto.AudioResponse;

public interface VoicePipelineService {
    AudioResponse processVoiceQuery(String sessionId, byte[] audioInput);
    String chat(String sessionId, String userMessage);
    void clearUserChatHistory(String sessionId, boolean isNew);
}
