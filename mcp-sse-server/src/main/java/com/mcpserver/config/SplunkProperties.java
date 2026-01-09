package com.mcpserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Splunk connection
 */
@Data
@Component
@ConfigurationProperties(prefix = "splunk")
public class SplunkProperties {

    /**
     * Splunk server host
     */
    private String host = "localhost";

    /**
     * Splunk management port (default: 8089)
     */
    private Integer port = 8089;

    /**
     * Splunk username
     */
    private String username = "admin";

    /**
     * Splunk password
     */
    private String password;

    /**
     * Connection scheme (http/https)
     */
    private String scheme = "https";

    /**
     * Whether to verify SSL certificates
     */
    private Boolean sslVerify = false;

    /**
     * Default index to search
     */
    private String defaultIndex = "main";

    /**
     * Search timeout in seconds
     */
    private Integer searchTimeout = 300;
}