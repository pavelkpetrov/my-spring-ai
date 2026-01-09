package com.my.spring.ai.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    // The AI's response message
    private String response;
    // The session ID, returned for convenience
    private String sessionId;
}