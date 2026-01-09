package com.mcpserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for API key authentication
 *
 * API keys are used to authenticate MCP clients accessing the server endpoints.
 * Keys are validated via the X-API-Key header.
 *
 * Configuration example in application.yml:
 * <pre>
 * mcp:
 *   auth:
 *     enabled: true
 *     api-keys:
 *       - key-1234567890
 *       - key-abcdefghij
 * </pre>
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mcp.auth")
public class ApiKeyProperties {

    /**
     * Enable/disable API key authentication
     * Default: false (authentication disabled)
     */
    private boolean enabled = false;

    /**
     * List of valid API keys
     * Each key should be a strong, randomly generated string
     */
    private List<String> apiKeys = new ArrayList<>();

    /**
     * Check if a given API key is valid
     *
     * @param apiKey the API key to validate
     * @return true if the key is in the list of valid keys, false otherwise
     */
    public boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        return apiKeys.contains(apiKey.trim());
    }
}