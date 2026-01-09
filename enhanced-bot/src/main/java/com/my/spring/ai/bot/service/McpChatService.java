package com.my.spring.ai.bot.service;

import org.springframework.ai.tool.ToolCallback;

import java.util.List;

public interface McpChatService {
    List<ToolCallback> listAvailableMcpTools();
    String chat(String sessionId, String userMessage);
    void clearUserChatHistory(String sessionId, boolean isNew);
}
