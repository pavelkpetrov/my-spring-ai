package com.mcpserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for Splunk log extraction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplunkLogResponse {

    /**
     * Search query used
     */
    @JsonProperty("query")
    private String query;

    /**
     * Total number of events found
     */
    @JsonProperty("total_count")
    private Long totalCount;

    /**
     * Number of events returned
     */
    @JsonProperty("returned_count")
    private Integer returnedCount;

    /**
     * List of log events
     */
    @JsonProperty("events")
    private List<SplunkEvent> events;

    /**
     * Search execution time in milliseconds
     */
    @JsonProperty("execution_time_ms")
    private Long executionTimeMs;

    /**
     * Status of the search
     */
    @JsonProperty("status")
    private String status;

    /**
     * Error message if search failed
     */
    @JsonProperty("error")
    private String error;
}