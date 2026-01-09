package com.coherentsolutions.homework.week1.util;

import com.coherentsolutions.homework.week1.exception.InvalidAudioException;
import com.coherentsolutions.homework.week1.exception.SpeechTranscriptionException;
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
