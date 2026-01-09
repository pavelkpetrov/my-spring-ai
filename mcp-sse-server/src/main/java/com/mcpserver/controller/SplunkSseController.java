package com.mcpserver.controller;

import com.mcpserver.dto.SplunkEvent;
import com.mcpserver.dto.SplunkLogRequest;
import com.mcpserver.dto.SplunkLogResponse;
import com.mcpserver.service.SplunkSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * REST Controller for SSE-based Splunk log streaming
 */
@Slf4j
@RestController
@RequestMapping("/api/splunk")
@RequiredArgsConstructor
public class SplunkSseController {

    private final SplunkSearchService splunkSearchService;

    /**
     * Stream Splunk logs via Server-Sent Events (SSE)
     *
     * Example usage:
     * curl -N "http://localhost:8081/api/splunk/logs/stream?startTime=-1h&endTime=now&limit=10"
     */
    @GetMapping(value = "/logs/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<SplunkEvent>> streamLogs(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "-24h") String startTime,
            @RequestParam(defaultValue = "now") String endTime,
            @RequestParam(defaultValue = "100") Integer limit,
            @RequestParam(required = false) String index,
            @RequestParam(required = false) String sourcetype
    ) {
        log.info("SSE stream request: query={}, startTime={}, endTime={}, limit={}",
                query, startTime, endTime, limit);

        SplunkLogRequest request = SplunkLogRequest.builder()
                .query(query)
                .startTime(startTime)
                .endTime(endTime)
                .limit(limit)
                .index(index)
                .sourcetype(sourcetype)
                .build();

        return splunkSearchService.streamLogs(request)
                .map(event -> ServerSentEvent.<SplunkEvent>builder()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .event("log")
                        .data(event)
                        .build())
                .concatWith(Mono.just(ServerSentEvent.<SplunkEvent>builder()
                        .event("complete")
                        .comment("Stream completed")
                        .build()))
                .doOnComplete(() -> log.info("SSE stream completed"))
                .doOnError(e -> log.error("SSE stream error: {}", e.getMessage(), e));
    }

    /**
     * POST endpoint for streaming logs with body
     *
     * Example usage:
     * curl -N -X POST http://localhost:8081/api/splunk/logs/stream \
     *   -H "Content-Type: application/json" \
     *   -d '{"query":"error", "startTime":"-1h", "endTime":"now", "limit":10}'
     */
    @PostMapping(value = "/logs/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<SplunkEvent>> streamLogsPost(@RequestBody SplunkLogRequest request) {
        log.info("SSE stream POST request: {}", request);

        return splunkSearchService.streamLogs(request)
                .map(event -> ServerSentEvent.<SplunkEvent>builder()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .event("log")
                        .data(event)
                        .build())
                .concatWith(Mono.just(ServerSentEvent.<SplunkEvent>builder()
                        .event("complete")
                        .comment("Stream completed")
                        .build()))
                .doOnComplete(() -> log.info("SSE stream completed"))
                .doOnError(e -> log.error("SSE stream error: {}", e.getMessage(), e));
    }

    /**
     * Regular REST endpoint for batch log retrieval
     *
     * Example usage:
     * curl "http://localhost:8081/api/splunk/logs?startTime=-1h&endTime=now&limit=10"
     */
    @GetMapping("/logs")
    public Mono<SplunkLogResponse> getLogs(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "-24h") String startTime,
            @RequestParam(defaultValue = "now") String endTime,
            @RequestParam(defaultValue = "100") Integer limit,
            @RequestParam(required = false) String index,
            @RequestParam(required = false) String sourcetype
    ) {
        log.info("Batch log request: query={}, startTime={}, endTime={}, limit={}",
                query, startTime, endTime, limit);

        SplunkLogRequest request = SplunkLogRequest.builder()
                .query(query)
                .startTime(startTime)
                .endTime(endTime)
                .limit(limit)
                .index(index)
                .sourcetype(sourcetype)
                .build();

        return splunkSearchService.searchLogs(request);
    }

    /**
     * POST endpoint for batch log retrieval with body
     *
     * Example usage:
     * curl -X POST http://localhost:8081/api/splunk/logs \
     *   -H "Content-Type: application/json" \
     *   -d '{"query":"error", "startTime":"-1h", "endTime":"now", "limit":10}'
     */
    @PostMapping("/logs")
    public Mono<SplunkLogResponse> getLogsPost(@RequestBody SplunkLogRequest request) {
        log.info("Batch log POST request: {}", request);
        return splunkSearchService.searchLogs(request);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public Mono<String> health() {
        return splunkSearchService.testConnection()
                .map(connected -> connected ? "Splunk connection OK" : "Splunk connection FAILED");
    }

    /**
     * Test SSE endpoint (sends periodic messages)
     */
    @GetMapping(value = "/test/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> testSse() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> ServerSentEvent.<String>builder()
                        .id(String.valueOf(sequence))
                        .event("message")
                        .data("Test message #" + sequence)
                        .build())
                .take(10);
    }
}