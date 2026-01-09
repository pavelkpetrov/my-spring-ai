package com.mcpserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for extracting Splunk logs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplunkLogRequest {

    /**
     * Search query (SPL - Splunk Processing Language)
     * Example: "index=main sourcetype=access_combined"
     */
    @JsonProperty("query")
    private String query;

    /**
     * Start time for the search (ISO 8601 format or relative time)
     * Examples: "2025-01-01T00:00:00", "-24h", "-7d"
     */
    @JsonProperty("start_time")
    private String startTime;

    /**
     * End time for the search (ISO 8601 format or relative time)
     * Examples: "2025-01-02T00:00:00", "now"
     */
    @JsonProperty("end_time")
    private String endTime;

    /**
     * Maximum number of results to return
     */
    @JsonProperty("limit")
    @Builder.Default
    private Integer limit = 100;

    /**
     * Splunk index to search (optional, defaults to configuration)
     */
    @JsonProperty("index")
    private String index;

    /**
     * Source type filter (optional)
     */
    @JsonProperty("sourcetype")
    private String sourcetype;
}