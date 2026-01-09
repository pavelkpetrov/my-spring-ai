package com.mcpserver.email.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for email settings.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "email.from")
public class EmailProperties {

    /**
     * Email address to use as sender
     */
    private String address;

    /**
     * Name to display as sender
     */
    private String name = "Greeting Service";
}