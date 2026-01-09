package com.coherentsolutions.homework.week1.controller;

import com.coherentsolutions.homework.week1.dto.DocumentIngest;
import com.coherentsolutions.homework.week1.dto.IngestResponse;
import com.coherentsolutions.homework.week1.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for DocumentController.
 *
 * This test class demonstrates proper controller testing patterns for the document ingestion endpoint.
 * It covers successful document ingestion as well as edge cases and validation scenarios.
 *
 * Testing Strategy:
 * - @WebMvcTest for focused controller testing
 * - MockMvc for HTTP request simulation
 * - Mocked DocumentService to isolate controller logic
 * - Comprehensive scenario coverage (success, validation errors, service errors)
 *
 * We exclude Ollama and ChromaDB autoconfiguration to prevent connection attempts during tests.
 */
@WebMvcTest(
    value = DocumentController.class,
    excludeAutoConfiguration = {
        OllamaAutoConfiguration.class,
        ChromaVectorStoreAutoConfiguration.class
    }
)
@ActiveProfiles("rag")
class DocumentControllerTest {

    private static final String VALID_CONTENT =
            "This is a test document about artificial intelligence and machine learning.";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /documents/ingest - Missing content property should return bad request")
    void ingest_withMissingContentField_shouldReturnBadRequest() throws Exception {
        String jsonWithoutContent = "{}";
        mockMvc.perform(post("/documents/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutContent))
                .andExpect(status().isBadRequest());

        verify(documentService, never()).ingestDocument(anyString());
    }

    @Test
    @DisplayName("POST /documents/ingest - Non-JSON Content-Type should return unsupported media type")
    void ingest_withNonJsonContentType_shouldReturnUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/documents/ingest")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Plain text input"))
                .andExpect(status().isUnsupportedMediaType());

        verify(documentService, never()).ingestDocument(anyString());
    }

    @Test
    @DisplayName("POST /documents/ingest - Success: Valid document should be ingested successfully")
    void ingest_withValidDocument_shouldReturnSuccess() throws Exception {
        // Given: A valid document to ingest
        DocumentIngest request = DocumentIngest.builder()
                .content(VALID_CONTENT)
                .build();

        IngestResponse mockResponse = IngestResponse.builder()
                .status("success")
                .chunksCount(5)
                .chunkSize(200)
                .build();

        // Mock the service behavior
        when(documentService.ingestDocument(VALID_CONTENT)).thenReturn(mockResponse);

        // When & Then: Perform the request and verify the response
        mockMvc.perform(post("/documents/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.chunksCount").value(5))
                .andExpect(jsonPath("$.chunkSize").value(200));

        // Verify the service was called exactly once with the correct content
        verify(documentService, times(1)).ingestDocument(VALID_CONTENT);
    }

    @Test
    @DisplayName("POST /documents/ingest - Corner Case: Empty document content should return bad request")
    void ingest_withEmptyContent_shouldReturnBadRequest() throws Exception {
        // Given: A document with empty content (validation should fail)
        DocumentIngest request = DocumentIngest.builder()
                .content("")
                .build();

        // When & Then: Perform the request and expect validation error
        mockMvc.perform(post("/documents/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify the service was never called due to validation failure
        verify(documentService, never()).ingestDocument(anyString());
    }

    @Test
    @DisplayName("POST /documents/ingest - Corner Case: Null document content should return bad request")
    void ingest_withNullContent_shouldReturnBadRequest() throws Exception {
        // Given: A document with null content (validation should fail)
        DocumentIngest request = DocumentIngest.builder()
                .content(null)
                .build();

        // When & Then: Perform the request and expect validation error
        mockMvc.perform(post("/documents/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify the service was never called due to validation failure
        verify(documentService, never()).ingestDocument(anyString());
    }

    @Test
    @DisplayName("POST /documents/ingest - Corner Case: Service exception should return internal server error")
    void ingest_whenServiceThrowsException_shouldReturnInternalServerError() throws Exception {
        // Given: A valid document but service throws an exception
        String documentContent = "Test document content";
        DocumentIngest request = DocumentIngest.builder()
                .content(documentContent)
                .build();

        // Mock the service to throw an exception
        when(documentService.ingestDocument(documentContent))
                .thenThrow(new RuntimeException("Vector store connection failed"));

        // When & Then: Perform the request and expect internal server error
        mockMvc.perform(post("/documents/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        // Verify the service was called
        verify(documentService, times(1)).ingestDocument(documentContent);
    }

    @Test
    @DisplayName("POST /documents/ingest - Corner Case: Malformed JSON should return bad request")
    void ingest_withMalformedJson_shouldReturnBadRequest() throws Exception {
        // Given: Malformed JSON (missing closing brace)
        String malformedJson = "{ \"content\": \"test document";

        // When & Then: Perform the request and expect bad request
        mockMvc.perform(post("/documents/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        // Verify the service was never called
        verify(documentService, never()).ingestDocument(anyString());
    }

    @Test
    @DisplayName("POST /documents/ingest - Corner Case: Very large document should be processed")
    void ingest_withLargeDocument_shouldReturnSuccess() throws Exception {
        // Given: A very large document (simulating real-world scenario)
        String largeContent = "A".repeat(10000); // 10KB document
        DocumentIngest request = DocumentIngest.builder()
                .content(largeContent)
                .build();

        IngestResponse mockResponse = IngestResponse.builder()
                .status("success")
                .chunksCount(50)
                .chunkSize(200)
                .build();

        when(documentService.ingestDocument(largeContent)).thenReturn(mockResponse);

        // When & Then: Perform the request and verify success
        mockMvc.perform(post("/documents/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chunksCount").value(50))
                .andExpect(jsonPath("$.chunkSize").value(200));

        verify(documentService, times(1)).ingestDocument(largeContent);
    }
}