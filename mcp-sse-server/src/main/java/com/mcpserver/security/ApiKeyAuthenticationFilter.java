package com.mcpserver.security;

import com.mcpserver.config.ApiKeyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.security.server.apikey.ApiKeyEntityRepository;
import org.springaicommunity.mcp.security.server.apikey.memory.ApiKeyEntityImpl;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * WebFilter for API key authentication in Spring WebFlux applications
 *
 * This filter validates API keys provided in the X-API-Key header for protected endpoints.
 * It is the WebFlux equivalent of OncePerRequestFilter used in Spring MVC.
 *
 * Authentication Flow:
 * 1. Check if authentication is enabled
 * 2. Check if the requested path requires authentication
 * 3. Extract API key from X-API-Key header
 * 4. Validate the API key against configured keys
 * 5. Return 401 Unauthorized if validation fails
 * 6. Continue filter chain if validation succeeds
 *
 * Protected Endpoints:
 * - /sse - SSE stream endpoint
 * - /mcp/message - JSON-RPC message endpoint
 *
 * Unprotected Endpoints:
 * - /api/splunk/health - Health check (always accessible)
 * - /actuator/** - Actuator endpoints (if enabled)
 */
@Slf4j
@Component
@Order(1) // Execute this filter first in the chain
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter implements WebFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final ApiKeyProperties apiKeyProperties;
    private final ApiKeyEntityRepository<ApiKeyEntityImpl> apiKeyRepository;

    /**
     * Paths that require API key authentication
     */
    private static final Set<String> PROTECTED_PATHS = Set.of(
        "/sse",
        "/mcp/message",
        "/api/splunk/logs",
        "/api/splunk/logs/stream"
    );

    /**
     * Paths that are always accessible without authentication
     */
    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/api/splunk/health",
        "/api/splunk/test/sse",
        "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Skip authentication if not enabled
        if (!apiKeyProperties.isEnabled()) {
            log.debug("API key authentication is disabled, allowing request");
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getPath().value();

        // Allow public paths without authentication
        if (isPublicPath(path)) {
            log.debug("Public path accessed: {}", path);
            return chain.filter(exchange);
        }

        // Check if path requires authentication
        if (!isProtectedPath(path)) {
            log.debug("Unprotected path accessed: {}", path);
            return chain.filter(exchange);
        }

        // Extract API key from header
        String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("API key missing for protected path: {}", path);
            return sendUnauthorizedResponse(exchange, "API key is required. Provide it in the " + API_KEY_HEADER + " header.");
        }

        // Validate API key
        if (apiKeyRepository.findByKeyId(apiKey) == null) {
            log.warn("Invalid API key attempt for path: {} from IP: {}",
                path,
                exchange.getRequest().getRemoteAddress()
            );
            return sendUnauthorizedResponse(exchange, "Invalid API key.");
        }

        log.debug("API key validated successfully for path: {}", path);

        // API key is valid, continue with the request
        return chain.filter(exchange);
    }

    /**
     * Check if the path is publicly accessible
     *
     * @param path the request path
     * @return true if the path is public, false otherwise
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Check if the path requires authentication
     *
     * @param path the request path
     * @return true if the path is protected, false otherwise
     */
    private boolean isProtectedPath(String path) {
        return PROTECTED_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Send 401 Unauthorized response with error message
     *
     * @param exchange the server web exchange
     * @param message the error message to include in the response
     * @return Mono<Void> completing the response
     */
    private Mono<Void> sendUnauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonResponse = String.format(
            "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
            java.time.Instant.now().toString(),
            message,
            exchange.getRequest().getPath().value()
        );

        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }
}