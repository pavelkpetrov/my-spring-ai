package com.my.spring.ai.bot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom advisor that logs all prompts and completions for debugging purposes.
 * This advisor intercepts chat requests and responses to provide visibility
 * into what's being sent to and received from the AI model.
 */
@Slf4j
public class LoggingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private static final String LOG_SEPARATOR = "=".repeat(80);
    private final boolean logPrompts;
    private final boolean logCompletions;

    public LoggingAdvisor() {
        this(true, true);
    }

    public LoggingAdvisor(boolean logPrompts, boolean logCompletions) {
        this.logPrompts = logPrompts;
        this.logCompletions = logCompletions;
    }

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return 0; // Execute first
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        if (logPrompts) {
            logRequest(advisedRequest);
        }

        AdvisedResponse response = chain.nextAroundCall(advisedRequest);

        if (logCompletions) {
            logResponse(response);
        }

        return response;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        if (logPrompts) {
            logRequest(advisedRequest);
        }

        return chain.nextAroundStream(advisedRequest)
                .doOnNext(response -> {
                    if (logCompletions) {
                        logStreamResponse(response);
                    }
                });
    }

    private void logRequest(AdvisedRequest request) {
        log.info("\n{}\n🔵 PROMPT (to AI Model)\n{}", LOG_SEPARATOR, LOG_SEPARATOR);

        // Log system message if present
        if (request.systemText() != null && !request.systemText().isEmpty()) {
            log.info("📋 System Message:\n{}", request.systemText());
        }

        // Log system parameters if present
        if (request.systemParams() != null && !request.systemParams().isEmpty()) {
            log.info("⚙️  System Params: {}", request.systemParams());
        }

        // Log user message
        if (request.userText() != null && !request.userText().isEmpty()) {
            log.info("💬 User Message:\n{}", request.userText());
        }

        // Log user parameters if present
        if (request.userParams() != null && !request.userParams().isEmpty()) {
            log.info("⚙️  User Params: {}", request.userParams());
        }

        // Log chat options if present
        ChatOptions options = request.chatOptions();
        if (options != null) {
            log.info("🔧 Chat Options: model={}, temperature={}",
                    options.getModel(),
                    options.getTemperature());
        }

        log.info("{}\n", LOG_SEPARATOR);
    }

    private void logResponse(AdvisedResponse response) {
        ChatResponse chatResponse = response.response();

        log.info("\n{}\n🟢 COMPLETION (from AI Model)\n{}", LOG_SEPARATOR, LOG_SEPARATOR);

        if (chatResponse != null && chatResponse.getResult() != null) {
            String content = chatResponse.getResult().getOutput().getText();
            log.info("💡 AI Response:\n{}", content);

            // Log metadata
            if (chatResponse.getMetadata() != null) {
                log.info("📊 Metadata: usage={}, model={}, finishReason={}",
                        chatResponse.getMetadata().getUsage(),
                        chatResponse.getMetadata().getModel(),
                        chatResponse.getResult().getMetadata().getFinishReason());
            }
        }

        log.info("{}\n", LOG_SEPARATOR);
    }

    private void logStreamResponse(AdvisedResponse response) {
        ChatResponse chatResponse = response.response();

        if (chatResponse != null && chatResponse.getResult() != null) {
            String content = chatResponse.getResult().getOutput().getText();
            // For streaming, just log the content chunk without full formatting
            log.debug("🔄 Stream chunk: {}", content);
        }
    }
}