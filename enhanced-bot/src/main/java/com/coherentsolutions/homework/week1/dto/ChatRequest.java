package com.coherentsolutions.homework.week1.dto;

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
public class ChatRequest {
    // The unique ID for the conversation session
    @NotNull(message = "sessionId cannot be null")
    private String sessionId;
    // The user's message
    @NotNull(message = "message cannot be null")
    private String message;
}
