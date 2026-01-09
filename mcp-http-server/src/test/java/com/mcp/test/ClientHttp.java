package com.mcp.test;

import io.micrometer.common.util.StringUtils;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.netty.handler.logging.LogLevel;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.util.Map;

/** With stdio transport, the MCP server is automatically started by the client. */
public class ClientHttp {

    public static void main(String[] args) {

        String toEmail = System.getenv("TO_EMAIL");
        if (StringUtils.isEmpty(toEmail)) {
            throw new IllegalArgumentException("Need to setup TO_EMAIL environment variable");
        }

        HttpClient httpClient = HttpClient.create()
                .wiretap("reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL);

        var webClientBuilder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://localhost:8082");

        var client =
                McpClient.sync(
                                new WebFluxSseClientTransport(webClientBuilder))
                        .build();

        client.initialize();

        client.ping();

        // List and demonstrate tools
        ListToolsResult toolsList = client.listTools();
        System.out.println("Available Tools = " + toolsList);

        McpSchema.CallToolResult response =
                client.callTool(
                        new McpSchema.CallToolRequest(
                                "send_greeting_email",
                                Map.of("toEmail", toEmail, "greetingText", "Hally holidays!!!")));
        System.out.println("Response = " + response);

        client.closeGracefully();
    }
}

