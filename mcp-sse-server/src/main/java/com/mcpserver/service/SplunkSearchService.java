package com.mcpserver.service;

import com.mcpserver.config.SplunkProperties;
import com.mcpserver.dto.SplunkEvent;
import com.mcpserver.dto.SplunkLogRequest;
import com.mcpserver.dto.SplunkLogResponse;
import com.splunk.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Service for interacting with Splunk and extracting logs reactively
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SplunkSearchService {

    private final com.splunk.Service splunkService;
    private final SplunkProperties splunkProperties;

    /**
     * Search Splunk logs based on the request parameters
     */
    public Mono<SplunkLogResponse> searchLogs(SplunkLogRequest request) {
        return Mono.fromCallable(() -> executeSearch(request))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Error searching Splunk logs: {}", e.getMessage(), e));
    }

    /**
     * Stream Splunk logs as a Flux for SSE
     */
    public Flux<SplunkEvent> streamLogs(SplunkLogRequest request) {
        return Flux.defer(() -> Flux.fromIterable(executeSearchForStream(request)))
                .subscribeOn(Schedulers.boundedElastic())
                .delayElements(Duration.ofMillis(50)) // Small delay for better streaming
                .doOnError(e -> log.error("Error streaming Splunk logs: {}", e.getMessage(), e));
    }

    /**
     * Execute Splunk search and return response
     */
    private SplunkLogResponse executeSearch(SplunkLogRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            String query = buildSearchQuery(request);
            log.info("Executing Splunk search: {}", query);

            JobArgs jobArgs = new JobArgs();
            jobArgs.setExecutionMode(JobArgs.ExecutionMode.NORMAL);

            if (request.getStartTime() != null) {
                jobArgs.setEarliestTime(request.getStartTime());
            }
            if (request.getEndTime() != null) {
                jobArgs.setLatestTime(request.getEndTime());
            }

            Job job = splunkService.getJobs().create(query, jobArgs);

            // Wait for job to complete
            waitForJobCompletion(job);

            // Get results
            JobResultsArgs resultsArgs = new JobResultsArgs();
            resultsArgs.setCount(request.getLimit() != null ? request.getLimit() : 100);
            resultsArgs.setOutputMode(JobResultsArgs.OutputMode.JSON);

            InputStream resultsStream = job.getResults(resultsArgs);
            List<SplunkEvent> events = parseResults(resultsStream);

            long executionTime = System.currentTimeMillis() - startTime;

            return SplunkLogResponse.builder()
                    .query(query)
                    .totalCount((long) job.getResultCount())
                    .returnedCount(events.size())
                    .events(events)
                    .executionTimeMs(executionTime)
                    .status("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("Error executing Splunk search", e);
            long executionTime = System.currentTimeMillis() - startTime;

            return SplunkLogResponse.builder()
                    .query(request.getQuery())
                    .totalCount(0L)
                    .returnedCount(0)
                    .events(Collections.emptyList())
                    .executionTimeMs(executionTime)
                    .status("ERROR")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Execute search for streaming
     */
    private List<SplunkEvent> executeSearchForStream(SplunkLogRequest request) {
        try {
            SplunkLogResponse response = executeSearch(request);
            return response.getEvents() != null ? response.getEvents() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error in stream search", e);
            return Collections.emptyList();
        }
    }

    /**
     * Build Splunk search query from request
     */
    private String buildSearchQuery(SplunkLogRequest request) {
        StringBuilder query = new StringBuilder("search ");

        // Add index
        String index = request.getIndex() != null ? request.getIndex() : splunkProperties.getDefaultIndex();
        query.append("index=").append(index);

        // Add sourcetype if specified
        if (request.getSourcetype() != null && !request.getSourcetype().isEmpty()) {
            query.append(" sourcetype=").append(request.getSourcetype());
        }

        // Add custom query if provided
        if (request.getQuery() != null && !request.getQuery().isEmpty()) {
            query.append(" ").append(request.getQuery());
        }

        return query.toString();
    }

    /**
     * Wait for Splunk job to complete
     */
    private void waitForJobCompletion(Job job) throws InterruptedException {
        int maxWaitSeconds = splunkProperties.getSearchTimeout();
        int elapsedSeconds = 0;

        while (!job.isDone()) {
            if (elapsedSeconds >= maxWaitSeconds) {
                throw new RuntimeException("Search timeout exceeded");
            }
            TimeUnit.MILLISECONDS.sleep(500);
            elapsedSeconds++;
        }
    }

    /**
     * Parse Splunk results from JSON stream
     */
    private List<SplunkEvent> parseResults(InputStream resultsStream) {
        List<SplunkEvent> events = new ArrayList<>();

        try {
            ResultsReaderJson resultsReader = new ResultsReaderJson(resultsStream);
            HashMap<String, String> event;

            while ((event = resultsReader.getNextEvent()) != null) {
                SplunkEvent splunkEvent = SplunkEvent.builder()
                        .time(event.get("_time"))
                        .raw(event.get("_raw"))
                        .source(event.get("source"))
                        .sourcetype(event.get("sourcetype"))
                        .index(event.get("index"))
                        .host(event.get("host"))
                        .fields(new HashMap<>(event))
                        .build();

                events.add(splunkEvent);
            }

            resultsReader.close();
        } catch (Exception e) {
            log.error("Error parsing Splunk results", e);
        }

        return events;
    }

    /**
     * Test connection to Splunk
     */
    public Mono<Boolean> testConnection() {
        return Mono.fromCallable(() -> {
                    try {
                        splunkService.getInfo();
                        log.info("Splunk connection test successful");
                        return true;
                    } catch (Exception e) {
                        log.error("Splunk connection test failed: {}", e.getMessage());
                        return false;
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}