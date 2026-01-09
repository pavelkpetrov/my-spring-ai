package com.my.spring.ai.bot.controller;

import com.my.spring.ai.bot.dto.ChatRequest;
import com.my.spring.ai.bot.dto.ChatResponse;
import com.my.spring.ai.bot.dto.ToolDefinitionResponse;
import com.my.spring.ai.bot.service.McpChatService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mcp/chat")
@Slf4j // Using Lombok for logging
public class ChatWithMcpController {

    private final McpChatService chatService;

    public ChatWithMcpController(McpChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Handles a chat message from a user.
     * The request body must contain the 'sessionId' and 'message'.
     *
     * @param request The ChatRequest DTO.
     * @return A ChatResponse DTO containing the AI's response.
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat request for session: {}", request.getSessionId());
        String responseMessage = chatService.chat(request.getSessionId(), request.getMessage());
        ChatResponse response = new ChatResponse(responseMessage, request.getSessionId());
        return ResponseEntity.ok(response);
    }

    /**
     * Resets/clears the conversation history for a given session.
     * Fulfills "Reset/clear conversation history" requirement.
     *
     * @param sessionId The ID of the session to clear.
     * @return An HTTP 200 OK response.
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> clearChat(@PathVariable String sessionId,
                                          @RequestParam(name = "isNew", defaultValue = "false") boolean isNew) {
        log.info("Clearing chat history for session: {}", sessionId);
        chatService.clearUserChatHistory(sessionId, isNew);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tools")
    public ResponseEntity<List<ToolDefinitionResponse>> listTools() {
        log.info("Received request to list tools");
        List<ToolCallback> tools = chatService.listAvailableMcpTools();
        var toolsList = tools.stream()
                .map(tool ->
                        ToolDefinitionResponse.builder()
                                .description(tool.getToolDefinition().description())
                                .name(tool.getToolDefinition().name())
                                .inputSchema(tool.getToolDefinition().inputSchema())
                                .build()
                )
                .toList();
        return ResponseEntity.ok(toolsList);
    }

}
