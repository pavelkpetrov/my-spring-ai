package com.my.spring.ai.bot.client.speech;
import com.my.spring.ai.bot.config.KokoroTTSOptions;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Client for interacting with Kokoro's text-to-speech models.
 *
 * This client specifically handles the kokoro  model which generates
 * audio from text using Fast API.
 */
@Slf4j
@Profile("voice")
@Component
public class KokoroTtsClient {

    private final RestClient restClient;

    public KokoroTtsClient(
            @Value("${kokoro.tts.base-url:http://localhost:8880}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        log.info("Initialized KokoroTtsClient with base-url={}", baseUrl);
    }

    /**
     * Convert text to speech using Kokoro's API.
     *
     * @param text The text to convert to speech
     * @return Audio data as byte array
     */
    public byte[] textToSpeech(String text, KokoroTTSOptions options) {
        log.debug("Sending text-to-speech request to Kokoro: text={}, options={}", text, options);

        KokoroGenerateRequest request = KokoroGenerateRequest.builder()
                .input(text)
                .voice(options.getVoice())
                .stream(true)
                .responseFormat(options.getResponseFormat())
                .downloadFormat(options.getDownloadFormat())
                .speed(options.getSpeed())
                .build();

        try {
            byte[] response = restClient.post()
                    .uri("/v1/audio/speech")
                    .body(request)
                    .retrieve()
                    .body(byte[].class);

            if (response == null) {
                throw new RuntimeException("Null response from Kokoro API");
            }

            log.info("Received response from Kokoro: response_length={}",
                    response != null ? response.length : "0");

            if (response == null || response.length <= 0) {
                log.error("Empty or null response from Kokoro.");
                throw new RuntimeException("Empty response from Kokoro API. Response field is null or empty.");
            }

            // The Kokoro model returns byte array audio in the response
            // We do not need to decode it to get the actual audio bytes
            byte[] audioBytes = response;

            log.info("Successfully decoded audio_length={} bytes", audioBytes.length);

            return audioBytes;

        } catch (IllegalArgumentException e) {
            log.error("Failed to decode base64 response from Kokoro. Response might not be base64 encoded.", e);
            throw new RuntimeException("Failed to decode audio data - response is not valid base64", e);
        } catch (Exception e) {
            log.error("Failed to generate speech from Kokoro: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate speech", e);
        }
    }

    /**
     * Request object for Kokoro's generate API.
     */
    @Data
    @Builder
    public static class KokoroGenerateRequest {
          @Builder.Default
          private String model = "kokoro";
          private String input;
          private String voice;
          @Builder.Default
          @JsonProperty("response_format")
          private String responseFormat = "mp3";
          @Builder.Default
          @JsonProperty("download_format")
          private String downloadFormat = "mp3";
          @Builder.Default
          @JsonProperty("return_download_link")
          private boolean returnDownloadLink = true;
          @Builder.Default
          private boolean stream = false;
          @Builder.Default
          private int speed = 1;
    }
}