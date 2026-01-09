package com.my.spring.ai.bot.util;

import com.my.spring.ai.bot.exception.InvalidAudioException;
import com.my.spring.ai.bot.exception.SpeechTranscriptionException;
import org.springframework.stereotype.Component;

@Component
public class AudioValidator {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public void validate(byte[] voice) {
        if (voice == null || voice.length == 0) {
            throw new SpeechTranscriptionException("Input audio cannot be null or empty");
        }

        // Check file size
        if (voice.length > MAX_FILE_SIZE) {
            throw new InvalidAudioException("File too large");
        }
    }
}
