package com.coherentsolutions.homework.week1.service;

public interface ChatService {

    String chat(String sessionId, String userMessage);
    void clearUserChatHistory(String sessionId, boolean isNew);

}
