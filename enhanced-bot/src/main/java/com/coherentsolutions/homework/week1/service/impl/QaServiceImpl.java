package com.coherentsolutions.homework.week1.service.impl;

import com.coherentsolutions.homework.week1.config.ApplicationContextHolder;
import com.coherentsolutions.homework.week1.config.LoggingAdvisor;
import com.coherentsolutions.homework.week1.dto.AnswerResponse;
import com.coherentsolutions.homework.week1.exception.TextGenerationException;
import com.coherentsolutions.homework.week1.service.QAService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Question-Answer service with RAG support - requires 'rag' profile
 *
 * This service depends on VectorStore, which is only available
 * when the 'rag' profile is active.
 */
@Slf4j
@Service
@Profile("rag")
public class QaServiceImpl implements QAService {

    private static final SearchRequest DEFAULT_SEARCH = new SearchRequest();

    private final ChatClient.Builder builder;
    private final ApplicationContextHolder context;

    private ChatClient chatClient;
    private VectorStore vectorStore;

    @Autowired
    public QaServiceImpl(ApplicationContextHolder context, ChatClient.Builder builder) {
        this.context = context;
        this.builder = builder;
    }

    /**
     * Answers a question using Retrieval-Augmented Generation (RAG).
     *
     * @param question the user's question (must not be null or empty)
     * @return the answer wrapped in an AnswerResponse
     * @throws IllegalArgumentException if the question is null or empty
     * @throws TextGenerationException for downstream AI failures
     */
    public AnswerResponse getAnswer(String question) {

        log.info("Received question: {}", question);

        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be null or empty");
        }

        try {

            String answer = getChatClient().prompt()
                    .user(question)
                    .call()
                    .content();

            log.debug("Generated answer: {}", answer);

            return AnswerResponse.builder().answer(answer).build();

        } catch (Exception e) {
            log.error("Failed to generate answer from AI", e);
            throw new TextGenerationException("Failed to generate answer from AI", e);
        }
    }

    private ChatClient getChatClient() {
        if (this.chatClient == null) {
            synchronized (this) {
                this.chatClient = builder
                        .defaultAdvisors(
                                new LoggingAdvisor(),  // Add logging advisor to log prompts and completions
                                new QuestionAnswerAdvisor(getVectorStore(), DEFAULT_SEARCH))
                        .build();
            }
        }
        return this.chatClient;
    }

    private VectorStore getVectorStore() {
        if (this.vectorStore == null) {
            synchronized (this) {
            this.vectorStore = context.getBean(VectorStore.class);
            }
        }
        return this.vectorStore;
    }

}
