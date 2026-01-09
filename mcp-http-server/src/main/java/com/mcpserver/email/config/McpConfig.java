package com.mcpserver.email.config;

import com.mcpserver.email.tool.EmailGreetingTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider userTools(EmailGreetingTools emailGreetingTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(emailGreetingTools)
                .build();
    }

}
