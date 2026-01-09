package com.my.spring.ai.bot.service;

import com.my.spring.ai.bot.service.impl.McpChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for McpChatServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class McpChatServiceImplTest {

    private static final int LAST_ENTRIES_COUNT = 10;

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ToolCallbackProvider toolCallbackProvider;

    @Mock
    private ToolCallback toolCallback1;

    @Mock
    private ToolCallback toolCallback2;

    @Mock
    private ToolCallback toolCallback3;

    private McpChatServiceImpl mcpChatService;

    @BeforeEach
    void setUp() {
        // Mock the builder chain to return the mocked ChatClient
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.defaultAdvisors(any(org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor.class)))
                .thenReturn(chatClientBuilder);
        when(chatClientBuilder.defaultTools(any(ToolCallback[].class))).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        // Mock tool callback provider to return empty array by default
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(new ToolCallback[0]);

        // Instantiate the service under test
        mcpChatService = new McpChatServiceImpl(
                chatMemory,
                chatClientBuilder,
                LAST_ENTRIES_COUNT,
                toolCallbackProvider
        );

        // Reset the mock to clear invocations from constructor
        reset(toolCallbackProvider);
    }

    // =========================================================================
    // listAvailableMcpTools() tests
    // =========================================================================

    @Test
    @DisplayName("listAvailableMcpTools - Success: Returns empty list when no tools available")
    void whenListAvailableMcpTools_withNoTools_shouldReturnEmptyList() {
        // --- Arrange ---
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(new ToolCallback[0]);

        // --- Act ---
        List<ToolCallback> result = mcpChatService.listAvailableMcpTools();

        // --- Assert ---
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty when no tools are available");

        // --- Verify ---
        verify(toolCallbackProvider, times(1)).getToolCallbacks();
    }

    @Test
    @DisplayName("listAvailableMcpTools - Success: Returns list with single tool")
    void whenListAvailableMcpTools_withSingleTool_shouldReturnListWithOneTool() {
        // --- Arrange ---
        ToolCallback[] toolCallbacks = new ToolCallback[]{toolCallback1};
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(toolCallbacks);

        // --- Act ---
        List<ToolCallback> result = mcpChatService.listAvailableMcpTools();

        // --- Assert ---
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result should contain exactly one tool");
        assertSame(toolCallback1, result.get(0), "Result should contain the same tool instance");

        // --- Verify ---
        verify(toolCallbackProvider, times(1)).getToolCallbacks();
    }

    @Test
    @DisplayName("listAvailableMcpTools - Success: Returns list with multiple tools")
    void whenListAvailableMcpTools_withMultipleTools_shouldReturnListWithAllTools() {
        // --- Arrange ---
        ToolCallback[] toolCallbacks = new ToolCallback[]{toolCallback1, toolCallback2, toolCallback3};
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(toolCallbacks);

        // --- Act ---
        List<ToolCallback> result = mcpChatService.listAvailableMcpTools();

        // --- Assert ---
        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.size(), "Result should contain exactly three tools");
        assertSame(toolCallback1, result.get(0), "First tool should match");
        assertSame(toolCallback2, result.get(1), "Second tool should match");
        assertSame(toolCallback3, result.get(2), "Third tool should match");

        // Verify order is preserved
        assertIterableEquals(List.of(toolCallback1, toolCallback2, toolCallback3), result,
                "Order of tools should be preserved");

        // --- Verify ---
        verify(toolCallbackProvider, times(1)).getToolCallbacks();
    }

    @Test
    @DisplayName("listAvailableMcpTools - Success: Returns immutable list")
    void whenListAvailableMcpTools_shouldReturnImmutableList() {
        // --- Arrange ---
        ToolCallback[] toolCallbacks = new ToolCallback[]{toolCallback1, toolCallback2};
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(toolCallbacks);

        // --- Act ---
        List<ToolCallback> result = mcpChatService.listAvailableMcpTools();

        // --- Assert ---
        // Verify the list is immutable by attempting to modify it
        assertThrows(UnsupportedOperationException.class, () -> result.add(toolCallback3),
                "List should be immutable (adding should throw exception)");
        assertThrows(UnsupportedOperationException.class, () -> result.remove(0),
                "List should be immutable (removing should throw exception)");

        // --- Verify ---
        verify(toolCallbackProvider, times(1)).getToolCallbacks();
    }

    @Test
    @DisplayName("listAvailableMcpTools - Success: Multiple calls return independent lists")
    void whenListAvailableMcpTools_calledMultipleTimes_shouldReturnIndependentLists() {
        // --- Arrange ---
        ToolCallback[] toolCallbacks = new ToolCallback[]{toolCallback1, toolCallback2};
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(toolCallbacks);

        // --- Act ---
        List<ToolCallback> result1 = mcpChatService.listAvailableMcpTools();
        List<ToolCallback> result2 = mcpChatService.listAvailableMcpTools();

        // --- Assert ---
        assertNotNull(result1, "First result should not be null");
        assertNotNull(result2, "Second result should not be null");
        assertEquals(result1.size(), result2.size(), "Both results should have same size");

        // Verify both lists contain the same elements
        for (int i = 0; i < result1.size(); i++) {
            assertSame(result1.get(i), result2.get(i),
                    "Lists should contain the same tool instances at index " + i);
        }

        // --- Verify ---
        // Should be called twice (once for each invocation)
        verify(toolCallbackProvider, times(2)).getToolCallbacks();
    }

    @Test
    @DisplayName("listAvailableMcpTools - Edge case: Provider returns null array")
    void whenListAvailableMcpTools_withNullArray_shouldHandleGracefully() {
        // --- Arrange ---
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(null);

        // --- Act & Assert ---
        // This should throw NullPointerException since Arrays.stream(null) throws NPE
        assertThrows(NullPointerException.class, () -> mcpChatService.listAvailableMcpTools(),
                "Should throw NullPointerException when provider returns null array");

        // --- Verify ---
        verify(toolCallbackProvider, times(1)).getToolCallbacks();
    }
}