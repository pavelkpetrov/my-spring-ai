package com.mcpserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a single Splunk event/log entry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplunkEvent {

    /**
     * Event timestamp
     */
    @JsonProperty("_time")
    private String time;

    /**
     * Raw event data
     */
    @JsonProperty("_raw")
    private String raw;

    /**
     * Source of the event
     */
    @JsonProperty("source")
    private String source;

    /**
     * Source type
     */
    @JsonProperty("sourcetype")
    private String sourcetype;

    /**
     * Splunk index
     */
    @JsonProperty("index")
    private String index;

    /**
     * Host that generated the event
     */
    @JsonProperty("host")
    private String host;

    /**
     * Additional fields extracted from the event
     */
    @JsonProperty("fields")
    private Map<String, Object> fields;
}