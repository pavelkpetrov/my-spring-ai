package com.mcpserver.config;

import org.springaicommunity.mcp.security.server.apikey.ApiKeyEntityRepository;
import org.springaicommunity.mcp.security.server.apikey.memory.ApiKeyEntityImpl;
import org.springaicommunity.mcp.security.server.apikey.memory.InMemoryApiKeyEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.IntStream;

@Configuration
public class SecurityConfig {

    @Bean
    public ApiKeyEntityRepository<ApiKeyEntityImpl> apiKeyRepository(ApiKeyProperties keyProperties) {
        var apiKeysEntities = IntStream.range(0, keyProperties.getApiKeys().size())
                .mapToObj(keyIndex ->
                        ApiKeyEntityImpl.builder()
                                .name("mcp_api_key_" + keyIndex)
                                .id("mcp_api_key_" + keyIndex)
                                .secret(keyProperties.getApiKeys().get(keyIndex))
                                .build()
                )
                .toList();
        return new InMemoryApiKeyEntityRepository<>(apiKeysEntities);
    }

}
