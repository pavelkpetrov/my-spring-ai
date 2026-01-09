package com.my.spring.ai.bot.util;

import com.my.spring.ai.bot.exception.InvalidAudioException;
import com.my.spring.ai.bot.exception.SpeechTranscriptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AudioValidatorTest {
    private AudioValidator validator;

    // 10 MB in bytes: 10 * 1024 * 1024
    private static final long MAX_FILE_SIZE = 10485760;

    @BeforeEach
    void setUp() {
        // Initialize a fresh instance before each test
        validator = new AudioValidator();
    }

    // --- Success Test Case ---

    @Test
    void validate_validAudio_shouldNotThrowException() {
        // ARRANGE: Create an audio array well within the limit (e.g., 1MB)
        byte[] validVoice = new byte[1024 * 1024]; // 1MB

        // ACT & ASSERT: Ensure the validation method executes without throwing any exception
        assertDoesNotThrow(() -> validator.validate(validVoice),
                "Validation should pass for a file within the size limit.");
    }

    @Test
    void validate_nullAudio_shouldThrowSpeechTranscriptionException() {
        // ARRANGE: Null input array
        byte[] nullVoice = null;

        // ACT & ASSERT: Expect a SpeechTranscriptionException
        assertThrows(SpeechTranscriptionException.class,
                () -> validator.validate(nullVoice),
                "Validation should throw SpeechTranscriptionException for null input.");
    }

    // --- Corner Case 2: Empty Input ---

    @Test
    void validate_emptyAudio_shouldThrowSpeechTranscriptionException() {
        // ARRANGE: Empty array (length 0)
        byte[] emptyVoice = new byte[0];

        // ACT & ASSERT: Expect a SpeechTranscriptionException
        assertThrows(SpeechTranscriptionException.class,
                () -> validator.validate(emptyVoice),
                "Validation should throw SpeechTranscriptionException for empty input.");
    }

    // --- Additional Edge Case: File size exactly at the limit ---

    @Test
    void validate_audioAtMaximumSize_shouldNotThrowException() {
        // ARRANGE: Create an audio array exactly 10MB
        byte[] maxVoice = new byte[(int) MAX_FILE_SIZE];

        // ACT & ASSERT: The inequality is '>' (greater than), so equality should pass.
        assertDoesNotThrow(() -> validator.validate(maxVoice),
                "Validation should pass for a file exactly at the maximum size limit (10MB).");
    }

    // --- Additional Edge Case: File size just over the limit ---

    @Test
    void validate_audioExceedingMaximumSize_shouldThrowInvalidAudioException() {
        // ARRANGE: Create an audio array 1 byte larger than the limit
        byte[] overMaxVoice = new byte[(int) MAX_FILE_SIZE + 1];

        // ACT & ASSERT: Expect an InvalidAudioException
        assertThrows(InvalidAudioException.class,
                () -> validator.validate(overMaxVoice),
                "Validation should throw InvalidAudioException for a file exceeding the size limit.");
    }

}
