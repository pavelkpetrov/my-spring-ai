package com.coherentsolutions.homework.week1.controller;

import com.coherentsolutions.homework.week1.dto.AudioRequest;
import com.coherentsolutions.homework.week1.dto.AudioResponse;
import com.coherentsolutions.homework.week1.service.VoicePipelineService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("voice")
@RequestMapping("/api/voice/chat")
@Slf4j
public class VoiceController {

    private final VoicePipelineService chatService;

    public VoiceController(VoicePipelineService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<AudioResponse> chat(@Valid @RequestBody AudioRequest request) {
        log.info("Received voice chat request for session: {}", request.getSessionId());
        AudioResponse responseMessage = chatService.processVoiceQuery(request.getSessionId(), request.getVoice());
        return ResponseEntity.ok(responseMessage);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> clearChat(@PathVariable String sessionId,
                                          @RequestParam(name = "isNew", defaultValue = "false") boolean isNew) {
        log.info("Clearing voice chat history for session: {}", sessionId);
        chatService.clearUserChatHistory(sessionId, isNew);
        return ResponseEntity.ok().build();
    }

}
