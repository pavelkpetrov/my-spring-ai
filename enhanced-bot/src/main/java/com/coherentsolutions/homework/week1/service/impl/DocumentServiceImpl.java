package com.coherentsolutions.homework.week1.service.impl;


import com.coherentsolutions.homework.week1.config.ApplicationContextHolder;
import com.coherentsolutions.homework.week1.dto.IngestResponse;
import com.coherentsolutions.homework.week1.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.coherentsolutions.homework.week1.util.Constants.*;

/**
 * Document ingestion service - requires 'rag' profile
 *
 * This service depends on VectorStore, which is only available
 * when the 'rag' profile is active.
 */
@Service
@Profile("rag")
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private VectorStore vectorStore;
    private TextSplitter textSplitter;
    private ApplicationContextHolder context;

    @Autowired
    public DocumentServiceImpl(ApplicationContextHolder context, TextSplitter textSplitter) {
        this.context = context;
        this.textSplitter = textSplitter;
    }

    @Override
    public IngestResponse ingestDocument(String content) {
        log.info("Starting document ingestion. Content length: {} characters", content.length());

        // Split the document into chunks
        Document document = new Document(content);
        List<Document> splitDocs = textSplitter.apply(List.of(document));

        log.info("Document split into {} chunks", splitDocs.size());

        // Process in batches to avoid overwhelming ChromaDB
        int totalChunks = splitDocs.size();
        int processedChunks = 0;

        for (int i = 0; i < totalChunks; i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, totalChunks);
            List<Document> batch = splitDocs.subList(i, endIndex);

            log.info("Processing batch {}/{} ({} documents)",
                    (i / BATCH_SIZE) + 1,
                    (totalChunks + BATCH_SIZE - 1) / BATCH_SIZE,
                    batch.size());

            // Add batch with retry logic
            addBatchWithRetry(batch);
            processedChunks += batch.size();

            log.info("Progress: {}/{} chunks processed", processedChunks, totalChunks);
        }

        log.info("Document ingestion completed successfully. Total chunks: {}", totalChunks);
        return IngestResponse.builder()
                .chunksCount(totalChunks)
                .chunkSize(CHUNK_SIZE)
                .status(SUCCESS)
                .build();
    }

    private void addBatchWithRetry(List<Document> batch) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                getVectorStore().add(batch);
                return; // Success!
            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Attempt {}/{} failed to add batch to vector store: {}",
                        attempt, MAX_RETRIES, e.getMessage());

                if (attempt < MAX_RETRIES) {
                    try {
                        log.info("Waiting {}ms before retry...", RETRY_DELAY_MS);
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry delay", ie);
                    }
                }
            }
        }

        // All retries exhausted
        throw new RuntimeException(
                String.format("Failed to add batch to vector store after %d attempts", MAX_RETRIES),
                lastException
        );
    }

    private VectorStore getVectorStore() {
        if (this.vectorStore == null) {
            this.vectorStore = context.getBean(VectorStore.class);
        }
        return this.vectorStore;
    }

}
