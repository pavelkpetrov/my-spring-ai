package com.coherentsolutions.homework.week1.service.impl;

import com.coherentsolutions.homework.week1.exception.InvalidAudioException;
import com.coherentsolutions.homework.week1.exception.TextGenerationException;
import com.coherentsolutions.homework.week1.service.SpeechToTextService;
import com.coherentsolutions.homework.week1.util.AudioValidator;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Profile("voice")
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SpeechToTextServiceImpl implements SpeechToTextService {

    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;
    private final AudioValidator audioValidator;

    /**
     * Service implementation to transcribe speech audio bytes to text using OpenAI Whisper via Spring AI.
     * Expects caller to validate input format and length.
     *
     * @param voice audio data as byte array
     * @return recognized plain text
     * @throws SpeechTranscriptionException on failure transcribe audio
     * @throws InvalidAudioException on audio file exeeds size limits
     */
    @Override
    public String speechToText(byte[] voice) {

        audioValidator.validate(voice);

        try {
            Resource audioResource = new ByteArrayResource(voice);
            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(
                    audioResource,
                    OpenAiAudioTranscriptionOptions
                            .builder()
                            .build()
            );
            AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(prompt);
            String transcribedText = response.getResult().getOutput();

            log.debug("Transcribed text (first 50 chars): {}",
                    transcribedText == null
                            ? "(null)"
                            : transcribedText.substring(0, Math.min(50, transcribedText.length())));

            return transcribedText;

        } catch (Exception e) {
            log.error("Fail to transcribe provided speech", e);
            throw new TextGenerationException("Fail to transcribe provided speech", e);
        }
    }

}
