package com.my.spring.ai.bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Using Lombok @Data to auto-generate getters, setters, constructor, etc.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioRequest {
    // The unique ID for the conversation session
    @NotBlank(message = "sessionId cannot be null")
    private String sessionId;
    // The user's message
    @NotNull(message = "voice cannot be null")
    private byte[] voice;
}
