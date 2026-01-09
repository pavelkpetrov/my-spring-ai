package com.coherentsolutions.homework.week1.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.coherentsolutions.homework.week1.util.Constants.CHUNK_SIZE;

@Configuration
@Slf4j
public class EmbeddingsConfig {

    @Bean
    public TextSplitter textSplitter() {
        return new TokenTextSplitter(CHUNK_SIZE, CHUNK_SIZE / 2, 5, 100, true);
    }

}
