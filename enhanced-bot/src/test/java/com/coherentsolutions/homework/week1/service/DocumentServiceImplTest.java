package com.coherentsolutions.homework.week1.service;

import com.coherentsolutions.homework.week1.config.ApplicationContextHolder;
import com.coherentsolutions.homework.week1.dto.IngestResponse;
import com.coherentsolutions.homework.week1.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.coherentsolutions.homework.week1.util.Constants.CHUNK_SIZE;
import static com.coherentsolutions.homework.week1.util.Constants.SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DocumentServiceImpl.
 *
 * This test class demonstrates proper service layer testing patterns for document ingestion.
 * It covers successful document ingestion as well as edge cases and error scenarios.
 *
 * Testing Strategy:
 * - @ExtendWith(MockitoExtension.class) for Mockito support
 * - Mocked VectorStore and TextSplitter to isolate service logic
 * - Comprehensive scenario coverage (success, empty content, retry logic)
 *
 * Key Learning Points:
 * - Testing services with multiple dependencies
 * - Mocking Spring AI components (Document, TextSplitter, VectorStore)
 * - Testing retry logic and exception handling
 * - Verifying batch processing behavior
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private TextSplitter textSplitter;

    @Mock
    private ApplicationContextHolder context;

    private DocumentServiceImpl documentService;

    private List<Document> mockDocuments;

    @BeforeEach
    void setUp() {
        // Create mock documents for testing
        mockDocuments = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            mockDocuments.add(new Document("Chunk " + i));
        }

        lenient().when(context.getBean(eq(VectorStore.class))).thenReturn(vectorStore);

        documentService = new DocumentServiceImpl(context, textSplitter);
    }

    @Test
    void ingestDocument_withNullContent_shouldThrowException() {
        assertThrows(NullPointerException.class,
                () -> documentService.ingestDocument(null));
    }

    @Test
    @DisplayName("Success: Valid document content should be split and stored successfully")
    void ingestDocument_withValidContent_shouldReturnSuccessResponse() {
        // Given: A valid document content
        String content = "This is a test document about artificial intelligence and machine learning. " +
                "It contains multiple sentences that will be split into chunks.";

        // Mock the text splitter to return chunks
        when(textSplitter.apply(anyList())).thenReturn(mockDocuments);

        // Mock the vector store to successfully add documents
        doNothing().when(vectorStore).add(anyList());

        // When: Ingesting the document
        IngestResponse response = documentService.ingestDocument(content);

        // Then: Verify the response
        assertNotNull(response, "Response should not be null");
        assertEquals(SUCCESS, response.getStatus(), "Status should be success");
        assertEquals(5, response.getChunksCount(), "Should have 5 chunks");
        assertEquals(CHUNK_SIZE, response.getChunkSize(), "Chunk size should match constant");

        // Verify interactions
        verify(textSplitter, times(1)).apply(anyList());
        verify(vectorStore, times(2)).add(anyList()); // 5 chunks = 2 batches (3+2), BATCH_SIZE=3
    }

    @Test
    @DisplayName("Corner Case: Empty content should be processed without errors")
    void ingestDocument_withEmptyContent_shouldHandleGracefully() {
        // Given: Empty document content
        String emptyContent = "";

        // Mock the text splitter to return empty list for empty content
        when(textSplitter.apply(anyList())).thenReturn(Collections.emptyList());

        // When: Ingesting empty content
        IngestResponse response = documentService.ingestDocument(emptyContent);

        // Then: Verify the response
        assertNotNull(response, "Response should not be null");
        assertEquals(SUCCESS, response.getStatus(), "Status should be success even with empty content");
        assertEquals(0, response.getChunksCount(), "Should have 0 chunks");
        assertEquals(CHUNK_SIZE, response.getChunkSize(), "Chunk size should match constant");

        // Verify interactions
        verify(textSplitter, times(1)).apply(anyList());
        verify(vectorStore, never()).add(anyList()); // No documents to add
    }

    @Test
    @DisplayName("Corner Case: VectorStore failure should retry and eventually throw exception")
    void ingestDocument_whenVectorStoreFailsAfterRetries_shouldThrowException() {
        // Given: A valid document content
        String content = "Test document that will fail to be stored.";

        // Mock the text splitter to return chunks
        when(textSplitter.apply(anyList())).thenReturn(mockDocuments);

        // Mock the vector store to always throw exception
        doThrow(new RuntimeException("Vector store connection failed"))
                .when(vectorStore).add(anyList());

        // When & Then: Ingesting should throw exception after retries
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            documentService.ingestDocument(content);
        }, "Should throw RuntimeException after all retries exhausted");

        // Verify the exception message
        assertTrue(exception.getMessage().contains("Failed to add batch to vector store"),
                "Exception message should indicate batch addition failure");

        // Verify interactions - should retry 3 times (MAX_RETRIES)
        verify(textSplitter, times(1)).apply(anyList());
        verify(vectorStore, times(3)).add(anyList()); // MAX_RETRIES = 3
    }

    @Test
    @DisplayName("Corner Case: Large document should be processed in multiple batches")
    void ingestDocument_withLargeDocument_shouldProcessInBatches() {
        // Given: A large document that will create many chunks
        String largeContent = "Large document content ".repeat(100);

        // Create 10 mock documents (more than one batch, since BATCH_SIZE = 3)
        List<Document> manyDocuments = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            manyDocuments.add(new Document("Chunk " + i));
        }

        // Mock the text splitter to return many chunks
        when(textSplitter.apply(anyList())).thenReturn(manyDocuments);

        // Mock the vector store to successfully add documents
        doNothing().when(vectorStore).add(anyList());

        // When: Ingesting the large document
        IngestResponse response = documentService.ingestDocument(largeContent);

        // Then: Verify the response
        assertNotNull(response, "Response should not be null");
        assertEquals(SUCCESS, response.getStatus(), "Status should be success");
        assertEquals(10, response.getChunksCount(), "Should have 10 chunks");

        // Verify multiple batch additions (BATCH_SIZE = 3, so 10 chunks = 4 batches: 3+3+3+1)
        verify(vectorStore, times(4)).add(anyList());
    }

    @Test
    @DisplayName("Corner Case: VectorStore fails on first attempt but succeeds on retry")
    void ingestDocument_whenVectorStoreFailsOnce_shouldRetryAndSucceed() {
        // Given: A valid document content
        String content = "Test document that will fail once then succeed.";

        // Create exactly 3 documents to fit in one batch (BATCH_SIZE=3)
        List<Document> singleBatchDocuments = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            singleBatchDocuments.add(new Document("Chunk " + i));
        }

        // Mock the text splitter to return 3 chunks (1 batch)
        when(textSplitter.apply(anyList())).thenReturn(singleBatchDocuments);

        // Mock the vector store to fail once, then succeed
        doThrow(new RuntimeException("Temporary connection issue"))
                .doNothing()
                .when(vectorStore).add(anyList());

        // When: Ingesting the document
        IngestResponse response = documentService.ingestDocument(content);

        // Then: Verify the response
        assertNotNull(response, "Response should not be null");
        assertEquals(SUCCESS, response.getStatus(), "Status should be success after retry");
        assertEquals(3, response.getChunksCount(), "Should have 3 chunks");

        // Verify interactions - should have retried once (1 batch, 2 attempts: fail then succeed)
        verify(textSplitter, times(1)).apply(anyList());
        verify(vectorStore, times(2)).add(anyList()); // First attempt failed, second succeeded
    }
}