package com.my.spring.ai.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("voice")
@Data
@Component
@ConfigurationProperties(prefix = "kokoro.tts.options")
public class KokoroTTSOptions {
    private String voice = "af_bella";
    private String responseFormat = "mp3";
    private String downloadFormat = "mp3";
    private int speed = 1;
}
