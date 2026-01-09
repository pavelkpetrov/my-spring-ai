package com.my.spring.ai.bot.service;

public interface ChatService {

    String chat(String sessionId, String userMessage);
    void clearUserChatHistory(String sessionId, boolean isNew);

}
