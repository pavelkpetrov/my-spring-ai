package com.mcpserver.tools;

import com.mcpserver.dto.SplunkEvent;
import com.mcpserver.dto.SplunkLogRequest;
import com.mcpserver.dto.SplunkLogResponse;
import com.mcpserver.service.SplunkSearchService;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SplunkMcpTools.
 *
 * This test class validates the MCP tool functionality for Splunk log extraction.
 * Tests cover successful operations as well as failure scenarios for each tool.
 *
 * Testing Strategy:
 * - Mock SplunkSearchService to isolate the tool logic
 * - Test each @McpTool method with success and failure scenarios
 * - Use reactor-test StepVerifier for reactive Mono responses
 * - Verify proper parameter handling and response formatting
 */
@ExtendWith(MockitoExtension.class)
class SplunkMcpToolsTest {

    @Mock
    private SplunkSearchService splunkSearchService;

    private SplunkMcpTools splunkMcpTools;

    @BeforeEach
    void setUp() {
        splunkMcpTools = new SplunkMcpTools(splunkSearchService);
    }

    // =========================================================================
    // extractLogs() - SUCCESSFUL TEST
    // =========================================================================

    @Test
    @DisplayName("extractLogs - Success: Valid parameters return log results")
    void whenExtractLogs_withValidParameters_shouldReturnSuccess() {
        // --- Arrange ---
        String query = "error OR exception";
        String startTime = "-24h";
        String endTime = "now";
        Integer limit = 100;
        String index = "main";
        String sourcetype = "access_combined";

        SplunkEvent event1 = SplunkEvent.builder()
                .time("2025-01-01T10:00:00")
                .raw("Error occurred in application")
                .source("/var/log/app.log")
                .sourcetype("access_combined")
                .index("main")
                .host("server01")
                .fields(Map.of("severity", "ERROR"))
                .build();

        SplunkLogResponse mockResponse = SplunkLogResponse.builder()
                .query("search index=main sourcetype=access_combined error OR exception")
                .totalCount(1L)
                .returnedCount(1)
                .events(List.of(event1))
                .executionTimeMs(150L)
                .status("SUCCESS")
                .build();

        when(splunkSearchService.searchLogs(any(SplunkLogRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        // --- Act & Assert ---
        Mono<McpSchema.CallToolResult> result = splunkMcpTools.extractLogs(
                query, startTime, endTime, limit, index, sourcetype);

        StepVerifier.create(result)
                .assertNext(callToolResult -> {
                    assertNotNull(callToolResult, "CallToolResult should not be null");
                    assertNotNull(callToolResult.structuredContent(), "Structured content should not be null");
                    assertTrue(callToolResult.structuredContent() instanceof SplunkLogResponse,
                            "Structured content should be SplunkLogResponse");

                    SplunkLogResponse response = (SplunkLogResponse) callToolResult.structuredContent();
                    assertEquals("SUCCESS", response.getStatus(), "Status should be SUCCESS");
                    assertEquals(1L, response.getTotalCount(), "Total count should match");
                    assertEquals(1, response.getReturnedCount(), "Returned count should match");
                    assertNotNull(response.getEvents(), "Events should not be null");
                    assertEquals(1, response.getEvents().size(), "Should have 1 event");
                })
                .verifyComplete();

        // --- Verify ---
        ArgumentCaptor<SplunkLogRequest> requestCaptor = ArgumentCaptor.forClass(SplunkLogRequest.class);
        verify(splunkSearchService, times(1)).searchLogs(requestCaptor.capture());

        SplunkLogRequest capturedRequest = requestCaptor.getValue();
        assertEquals(query, capturedRequest.getQuery(), "Query should match");
        assertEquals(startTime, capturedRequest.getStartTime(), "Start time should match");
        assertEquals(endTime, capturedRequest.getEndTime(), "End time should match");
        assertEquals(limit, capturedRequest.getLimit(), "Limit should match");
        assertEquals(index, capturedRequest.getIndex(), "Index should match");
        assertEquals(sourcetype, capturedRequest.getSourcetype(), "Sourcetype should match");
    }

    // =========================================================================
    // extractLogs() - FAILURE TEST
    // =========================================================================

    @Test
    @DisplayName("extractLogs - Failure: Service returns error response")
    void whenExtractLogs_andServiceFails_shouldReturnErrorResponse() {
        // --- Arrange ---
        String query = "invalid query";
        String startTime = "-24h";
        String endTime = "now";
        Integer limit = 100;

        SplunkLogResponse errorResponse = SplunkLogResponse.builder()
                .query("search index=main invalid query")
                .totalCount(0L)
                .returnedCount(0)
                .events(Collections.emptyList())
                .executionTimeMs(50L)
                .status("ERROR")
                .error("Splunk search syntax error")
                .build();

        when(splunkSearchService.searchLogs(any(SplunkLogRequest.class)))
                .thenReturn(Mono.just(errorResponse));

        // --- Act & Assert ---
        Mono<McpSchema.CallToolResult> result = splunkMcpTools.extractLogs(
                query, startTime, endTime, limit, null, null);

        StepVerifier.create(result)
                .assertNext(callToolResult -> {
                    assertNotNull(callToolResult, "CallToolResult should not be null");
                    assertNotNull(callToolResult.structuredContent(), "Structured content should not be null");
                    assertTrue(callToolResult.structuredContent() instanceof SplunkLogResponse,
                            "Structured content should be SplunkLogResponse");

                    SplunkLogResponse response = (SplunkLogResponse) callToolResult.structuredContent();
                    assertEquals("ERROR", response.getStatus(), "Status should be ERROR");
                    assertEquals(0L, response.getTotalCount(), "Total count should be 0");
                    assertNotNull(response.getError(), "Error message should be present");
                    assertTrue(response.getError().contains("syntax error"),
                            "Error should mention syntax error");
                })
                .verifyComplete();

        // --- Verify ---
        verify(splunkSearchService, times(1)).searchLogs(any(SplunkLogRequest.class));
    }

    // =========================================================================
    // testConnection() - SUCCESSFUL TEST
    // =========================================================================

    @Test
    @DisplayName("testConnection - Success: Connection to Splunk succeeds")
    void whenTestConnection_andConnectionSucceeds_shouldReturnSuccess() {
        // --- Arrange ---
        when(splunkSearchService.testConnection()).thenReturn(Mono.just(true));

        // --- Act & Assert ---
        Mono<Map<String, Object>> result = splunkMcpTools.testConnection();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response, "Response should not be null");
                    assertEquals(2, response.size(), "Response should contain 2 entries");

                    assertTrue((Boolean) response.get("connected"),
                            "Connected flag should be true");
                    assertEquals("Successfully connected to Splunk", response.get("message"),
                            "Success message should match");
                })
                .verifyComplete();

        // --- Verify ---
        verify(splunkSearchService, times(1)).testConnection();
    }

    // =========================================================================
    // testConnection() - FAILURE TEST
    // =========================================================================

    @Test
    @DisplayName("testConnection - Failure: Connection to Splunk fails")
    void whenTestConnection_andConnectionFails_shouldReturnFailure() {
        // --- Arrange ---
        when(splunkSearchService.testConnection()).thenReturn(Mono.just(false));

        // --- Act & Assert ---
        Mono<Map<String, Object>> result = splunkMcpTools.testConnection();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response, "Response should not be null");
                    assertEquals(2, response.size(), "Response should contain 2 entries");

                    assertFalse((Boolean) response.get("connected"),
                            "Connected flag should be false");
                    assertEquals("Failed to connect to Splunk", response.get("message"),
                            "Failure message should match");
                })
                .verifyComplete();

        // --- Verify ---
        verify(splunkSearchService, times(1)).testConnection();
    }

    // =========================================================================
    // getRecentLogs() - SUCCESSFUL TEST
    // =========================================================================

    @Test
    @DisplayName("getRecentLogs - Success: Retrieve recent logs with default parameters")
    void whenGetRecentLogs_withDefaultParameters_shouldReturnSuccess() {
        // --- Arrange ---
        String timeRange = "1h";
        Integer limit = 50;

        SplunkEvent event1 = SplunkEvent.builder()
                .time("2025-01-01T10:00:00")
                .raw("Recent log entry")
                .source("/var/log/app.log")
                .sourcetype("syslog")
                .index("main")
                .host("server01")
                .build();

        SplunkLogResponse mockResponse = SplunkLogResponse.builder()
                .query("search index=main")
                .totalCount(1L)
                .returnedCount(1)
                .events(List.of(event1))
                .executionTimeMs(100L)
                .status("SUCCESS")
                .build();

        when(splunkSearchService.searchLogs(any(SplunkLogRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        // --- Act & Assert ---
        Mono<SplunkLogResponse> result = splunkMcpTools.getRecentLogs(timeRange, limit);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response, "Response should not be null");
                    assertEquals("SUCCESS", response.getStatus(), "Status should be SUCCESS");
                    assertEquals(1L, response.getTotalCount(), "Total count should match");
                    assertEquals(1, response.getReturnedCount(), "Returned count should match");
                    assertNotNull(response.getEvents(), "Events should not be null");
                    assertEquals(1, response.getEvents().size(), "Should have 1 event");
                })
                .verifyComplete();

        // --- Verify ---
        ArgumentCaptor<SplunkLogRequest> requestCaptor = ArgumentCaptor.forClass(SplunkLogRequest.class);
        verify(splunkSearchService, times(1)).searchLogs(requestCaptor.capture());

        SplunkLogRequest capturedRequest = requestCaptor.getValue();
        assertEquals("-1h", capturedRequest.getStartTime(), "Start time should be -1h");
        assertEquals("now", capturedRequest.getEndTime(), "End time should be now");
        assertEquals(50, capturedRequest.getLimit(), "Limit should be 50");
    }

    // =========================================================================
    // getRecentLogs() - FAILURE TEST
    // =========================================================================

    @Test
    @DisplayName("getRecentLogs - Failure: Service returns error")
    void whenGetRecentLogs_andServiceFails_shouldReturnErrorResponse() {
        // --- Arrange ---
        String timeRange = "24h";
        Integer limit = 100;

        SplunkLogResponse errorResponse = SplunkLogResponse.builder()
                .query("search index=main")
                .totalCount(0L)
                .returnedCount(0)
                .events(Collections.emptyList())
                .executionTimeMs(50L)
                .status("ERROR")
                .error("Connection timeout")
                .build();

        when(splunkSearchService.searchLogs(any(SplunkLogRequest.class)))
                .thenReturn(Mono.just(errorResponse));

        // --- Act & Assert ---
        Mono<SplunkLogResponse> result = splunkMcpTools.getRecentLogs(timeRange, limit);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response, "Response should not be null");
                    assertEquals("ERROR", response.getStatus(), "Status should be ERROR");
                    assertEquals(0L, response.getTotalCount(), "Total count should be 0");
                    assertNotNull(response.getError(), "Error message should be present");
                    assertEquals("Connection timeout", response.getError(),
                            "Error message should match");
                })
                .verifyComplete();

        // --- Verify ---
        verify(splunkSearchService, times(1)).searchLogs(any(SplunkLogRequest.class));
    }

    // =========================================================================
    // searchByKeyword() - SUCCESSFUL TEST
    // =========================================================================

    @Test
    @DisplayName("searchByKeyword - Success: Search by keyword returns matching results")
    void whenSearchByKeyword_withValidKeyword_shouldReturnSuccess() {
        // --- Arrange ---
        String keyword = "error";
        String timeRange = "24h";
        Integer limit = 100;

        SplunkEvent event1 = SplunkEvent.builder()
                .time("2025-01-01T10:00:00")
                .raw("Application error detected")
                .source("/var/log/app.log")
                .sourcetype("syslog")
                .index("main")
                .host("server01")
                .build();

        SplunkEvent event2 = SplunkEvent.builder()
                .time("2025-01-01T10:05:00")
                .raw("Database connection error")
                .source("/var/log/db.log")
                .sourcetype("syslog")
                .index("main")
                .host("server02")
                .build();

        SplunkLogResponse mockResponse = SplunkLogResponse.builder()
                .query("search index=main \"error\"")
                .totalCount(2L)
                .returnedCount(2)
                .events(List.of(event1, event2))
                .executionTimeMs(200L)
                .status("SUCCESS")
                .build();

        when(splunkSearchService.searchLogs(any(SplunkLogRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        // --- Act & Assert ---
        Mono<SplunkLogResponse> result = splunkMcpTools.searchByKeyword(keyword, timeRange, limit);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response, "Response should not be null");
                    assertEquals("SUCCESS", response.getStatus(), "Status should be SUCCESS");
                    assertEquals(2L, response.getTotalCount(), "Total count should be 2");
                    assertEquals(2, response.getReturnedCount(), "Returned count should be 2");
                    assertNotNull(response.getEvents(), "Events should not be null");
                    assertEquals(2, response.getEvents().size(), "Should have 2 events");
                })
                .verifyComplete();

        // --- Verify ---
        ArgumentCaptor<SplunkLogRequest> requestCaptor = ArgumentCaptor.forClass(SplunkLogRequest.class);
        verify(splunkSearchService, times(1)).searchLogs(requestCaptor.capture());

        SplunkLogRequest capturedRequest = requestCaptor.getValue();
        assertEquals("\"error\"", capturedRequest.getQuery(),
                "Query should be wrapped in quotes");
        assertEquals("-24h", capturedRequest.getStartTime(), "Start time should be -24h");
        assertEquals("now", capturedRequest.getEndTime(), "End time should be now");
        assertEquals(100, capturedRequest.getLimit(), "Limit should be 100");
    }

    // =========================================================================
    // searchByKeyword() - FAILURE TEST
    // =========================================================================

    @Test
    @DisplayName("searchByKeyword - Failure: No results found for keyword")
    void whenSearchByKeyword_andNoResultsFound_shouldReturnEmptyResponse() {
        // --- Arrange ---
        String keyword = "nonexistent_keyword_xyz";
        String timeRange = "1h";
        Integer limit = 50;

        SplunkLogResponse mockResponse = SplunkLogResponse.builder()
                .query("search index=main \"nonexistent_keyword_xyz\"")
                .totalCount(0L)
                .returnedCount(0)
                .events(Collections.emptyList())
                .executionTimeMs(80L)
                .status("SUCCESS")
                .build();

        when(splunkSearchService.searchLogs(any(SplunkLogRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        // --- Act & Assert ---
        Mono<SplunkLogResponse> result = splunkMcpTools.searchByKeyword(keyword, timeRange, limit);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response, "Response should not be null");
                    assertEquals("SUCCESS", response.getStatus(),
                            "Status should be SUCCESS even with no results");
                    assertEquals(0L, response.getTotalCount(), "Total count should be 0");
                    assertEquals(0, response.getReturnedCount(), "Returned count should be 0");
                    assertNotNull(response.getEvents(), "Events should not be null");
                    assertTrue(response.getEvents().isEmpty(), "Events list should be empty");
                })
                .verifyComplete();

        // --- Verify ---
        verify(splunkSearchService, times(1)).searchLogs(any(SplunkLogRequest.class));
    }

    // =========================================================================
    // Additional Edge Case Tests
    // =========================================================================

    @Test
    @DisplayName("extractLogs - Edge case: Null parameters use defaults")
    void whenExtractLogs_withNullParameters_shouldUseDefaults() {
        // --- Arrange ---
        SplunkLogResponse mockResponse = SplunkLogResponse.builder()
                .query("search index=main")
                .totalCount(0L)
                .returnedCount(0)
                .events(Collections.emptyList())
                .executionTimeMs(50L)
                .status("SUCCESS")
                .build();

        when(splunkSearchService.searchLogs(any(SplunkLogRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        // --- Act & Assert ---
        Mono<McpSchema.CallToolResult> result = splunkMcpTools.extractLogs(
                null, null, null, null, null, null);

        StepVerifier.create(result)
                .assertNext(callToolResult -> {
                    assertNotNull(callToolResult, "CallToolResult should not be null");
                })
                .verifyComplete();

        // --- Verify ---
        ArgumentCaptor<SplunkLogRequest> requestCaptor = ArgumentCaptor.forClass(SplunkLogRequest.class);
        verify(splunkSearchService, times(1)).searchLogs(requestCaptor.capture());

        SplunkLogRequest capturedRequest = requestCaptor.getValue();
        assertEquals("-24h", capturedRequest.getStartTime(), "Default start time should be -24h");
        assertEquals("now", capturedRequest.getEndTime(), "Default end time should be now");
        assertEquals(100, capturedRequest.getLimit(), "Default limit should be 100");
    }

    @Test
    @DisplayName("extractLogs - Edge case: Limit exceeds maximum, should be capped at 10000")
    void whenExtractLogs_withExcessiveLimit_shouldCapAt10000() {
        // --- Arrange ---
        Integer excessiveLimit = 50000;

        SplunkLogResponse mockResponse = SplunkLogResponse.builder()
                .query("search index=main")
                .totalCount(0L)
                .returnedCount(0)
                .events(Collections.emptyList())
                .executionTimeMs(50L)
                .status("SUCCESS")
                .build();

        when(splunkSearchService.searchLogs(any(SplunkLogRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        // --- Act & Assert ---
        Mono<McpSchema.CallToolResult> result = splunkMcpTools.extractLogs(
                null, null, null, excessiveLimit, null, null);

        StepVerifier.create(result).expectNextCount(1).verifyComplete();

        // --- Verify ---
        ArgumentCaptor<SplunkLogRequest> requestCaptor = ArgumentCaptor.forClass(SplunkLogRequest.class);
        verify(splunkSearchService, times(1)).searchLogs(requestCaptor.capture());

        SplunkLogRequest capturedRequest = requestCaptor.getValue();
        assertEquals(10000, capturedRequest.getLimit(), "Limit should be capped at 10000");
    }

    @Test
    @DisplayName("getRecentLogs - Edge case: Null parameters use defaults")
    void whenGetRecentLogs_withNullParameters_shouldUseDefaults() {
        // --- Arrange ---
        SplunkLogResponse mockResponse = SplunkLogResponse.builder()
                .query("search index=main")
                .totalCount(0L)
                .returnedCount(0)
                .events(Collections.emptyList())
                .executionTimeMs(50L)
                .status("SUCCESS")
                .build();

        when(splunkSearchService.searchLogs(any(SplunkLogRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        // --- Act & Assert ---
        Mono<SplunkLogResponse> result = splunkMcpTools.getRecentLogs(null, null);

        StepVerifier.create(result).expectNextCount(1).verifyComplete();

        // --- Verify ---
        ArgumentCaptor<SplunkLogRequest> requestCaptor = ArgumentCaptor.forClass(SplunkLogRequest.class);
        verify(splunkSearchService, times(1)).searchLogs(requestCaptor.capture());

        SplunkLogRequest capturedRequest = requestCaptor.getValue();
        assertEquals("-1h", capturedRequest.getStartTime(), "Default time range should be 1h");
        assertEquals(50, capturedRequest.getLimit(), "Default limit should be 50");
    }

    @Test
    @DisplayName("searchByKeyword - Edge case: Null timeRange uses default 24h")
    void whenSearchByKeyword_withNullTimeRange_shouldUseDefault() {
        // --- Arrange ---
        String keyword = "test";

        SplunkLogResponse mockResponse = SplunkLogResponse.builder()
                .query("search index=main \"test\"")
                .totalCount(0L)
                .returnedCount(0)
                .events(Collections.emptyList())
                .executionTimeMs(50L)
                .status("SUCCESS")
                .build();

        when(splunkSearchService.searchLogs(any(SplunkLogRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        // --- Act & Assert ---
        Mono<SplunkLogResponse> result = splunkMcpTools.searchByKeyword(keyword, null, null);

        StepVerifier.create(result).expectNextCount(1).verifyComplete();

        // --- Verify ---
        ArgumentCaptor<SplunkLogRequest> requestCaptor = ArgumentCaptor.forClass(SplunkLogRequest.class);
        verify(splunkSearchService, times(1)).searchLogs(requestCaptor.capture());

        SplunkLogRequest capturedRequest = requestCaptor.getValue();
        assertEquals("-24h", capturedRequest.getStartTime(), "Default time range should be 24h");
        assertEquals(100, capturedRequest.getLimit(), "Default limit should be 100");
    }
}