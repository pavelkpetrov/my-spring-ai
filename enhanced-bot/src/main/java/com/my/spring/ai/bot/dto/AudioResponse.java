package com.my.spring.ai.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudioResponse {
    // The AI's response message
    private byte[] voice;
    private String response;
    private String request;
    // The session ID, returned for convenience
    private String sessionId;
}