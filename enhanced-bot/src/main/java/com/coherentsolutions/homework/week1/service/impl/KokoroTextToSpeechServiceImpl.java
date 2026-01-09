package com.coherentsolutions.homework.week1.service.impl;

import com.coherentsolutions.homework.week1.client.speech.KokoroTtsClient;
import com.coherentsolutions.homework.week1.config.KokoroTTSOptions;
import com.coherentsolutions.homework.week1.exception.SpeechGenerationException;
import com.coherentsolutions.homework.week1.service.TextToSpeechService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementation of TextToSpeechService using Kokoro's model.
 */
@Slf4j
@Profile("voice")
@Service
@Primary
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class KokoroTextToSpeechServiceImpl implements TextToSpeechService {

    private final KokoroTtsClient kokoroTtsClient;
    private final KokoroTTSOptions ttsOptions;

    @Override
    public byte[] textToSpeech(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new SpeechGenerationException("Input text cannot be null or empty");
        }

        try {
            log.info("Converting text to speech using Kokoro: text={}", text);

            byte[] audioData = kokoroTtsClient.textToSpeech(text, ttsOptions);

            if (audioData == null || audioData.length == 0) {
                throw new SpeechGenerationException("Kokoro returned empty audio data");
            }

            log.info("Successfully converted text to speech: textLength={}, audioLength={}",
                    text.length(), audioData.length);

            return audioData;

        } catch (Exception e) {
            String error = String.format("Failed to convert text to speech: text=%s", text);
            log.error(error, e);
            throw new SpeechGenerationException(error, e);
        }
    }
}