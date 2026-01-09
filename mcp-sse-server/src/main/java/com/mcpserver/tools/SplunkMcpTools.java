package com.mcpserver.tools;

import com.mcpserver.dto.SplunkLogRequest;
import com.mcpserver.dto.SplunkLogResponse;
import com.mcpserver.service.SplunkSearchService;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * MCP-Compatible Tools for Splunk log extraction
 *
 * This service provides MCP-compatible tool functions that can be exposed via REST API/SSE
 * and integrated with Model Context Protocol clients. Each method is annotated with @Tool
 * to enable automatic discovery and invocation via the ToolCallbackProvider.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SplunkMcpTools {

    private final SplunkSearchService splunkSearchService;

    /**
     * MCP Tool: Extract Splunk logs by date-time range with limits
     * <p>
     * Description: Extract Splunk logs by date-time range with configurable limits.
     * Supports flexible time ranges (relative like '-24h', '-7d' or absolute ISO 8601),
     * custom SPL queries, and result limits.
     *
     * @param query      Optional SPL query filter (e.g., 'status=200', 'error OR exception')
     * @param startTime  Start time for log extraction (e.g., '-24h', '2025-01-01T00:00:00')
     * @param endTime    End time for log extraction (e.g., 'now')
     * @param limit      Maximum number of log entries to return (Default: 100, Max: 10000)
     * @param index      Splunk index to search (e.g., 'main', 'security')
     * @param sourcetype Source type filter (e.g., 'access_combined', 'syslog')
     * @return Mono containing the search results
     */
    @McpTool(
            name = "extractLogs",
            description = """
                    Extract Splunk logs by date-time range with configurable limits and filters.

                    This tool searches Splunk indexes for log events within a specified time range.
                    It supports both relative time ranges (e.g., '-24h' for last 24 hours, '-7d' for last 7 days)
                    and absolute ISO 8601 timestamps (e.g., '2025-01-01T00:00:00Z').

                    You can filter results using Splunk Processing Language (SPL) queries. Examples:
                    - Search for errors: query='error OR exception'
                    - Filter by status: query='status=500'
                    - Search specific field: query='user_id=123'

                    The tool returns structured log events with metadata including timestamp, source,
                    sourcetype, host, and the raw log message. Results include execution statistics
                    like total count, returned count, and query execution time.

                    Use this tool when you need to:
                    - Investigate issues within a specific time window
                    - Search for specific events or patterns in logs
                    - Analyze log data from particular sources or indexes
                    - Retrieve historical log data for analysis

                    Parameters:
                    - query: Optional SPL filter to narrow results (e.g., 'error', 'status=200')
                    - startTime: Start of time range (default: '-24h'). Use '-1h', '-7d', or ISO 8601
                    - endTime: End of time range (default: 'now')
                    - limit: Max results to return (default: 100, max: 10000)
                    - index: Splunk index to search (default: 'main')
                    - sourcetype: Filter by source type (e.g., 'syslog', '_json', 'access_combined')
                    """
    )
    public Mono<McpSchema.CallToolResult> extractLogs(
            String query,
            String startTime,
            String endTime,
            Integer limit,
            String index,
            String sourcetype
    ) {
        log.info("MCP Tool invoked: extractLogs with query={}, startTime={}, endTime={}, limit={}",
                query, startTime, endTime, limit);

        SplunkLogRequest request = SplunkLogRequest.builder()
                .query(query)
                .startTime(startTime != null ? startTime : "-24h")
                .endTime(endTime != null ? endTime : "now")
                .limit(limit != null ? Math.min(limit, 10000) : 100)
                .index(index)
                .sourcetype(sourcetype)
                .build();

        return splunkSearchService.searchLogs(request).map(response ->
                McpSchema.CallToolResult.builder().structuredContent(response).build());
    }

    /**
     * MCP Tool: Test Splunk connection
     *
     * Description: Test the connection to Splunk server to verify credentials and connectivity
     *
     * @return Mono containing connection test result
     */
    @McpTool(
            name = "testConnection",
            description = """
                    Test the connection to Splunk server to verify credentials and connectivity.

                    This tool performs a health check on the Splunk server connection by attempting
                    to retrieve server information. It verifies that:
                    - The server is reachable at the configured host and port
                    - Authentication credentials are valid
                    - The server is responding to API requests

                    Returns a status object containing:
                    - connected: Boolean indicating if connection was successful
                    - message: Descriptive message about the connection status

                    Use this tool when you need to:
                    - Verify Splunk server connectivity before running queries
                    - Troubleshoot connection issues
                    - Validate configuration settings
                    - Check if the Splunk server is available

                    No parameters required.
                    """
    )
    public Mono<Map<String, Object>> testConnection() {
        log.info("MCP Tool invoked: testConnection");

        return splunkSearchService.testConnection()
                .map(success -> Map.of(
                        "connected", success,
                        "message", success ?
                                "Successfully connected to Splunk" :
                                "Failed to connect to Splunk"
                ));
    }

    /**
     * MCP Tool: Get recent logs (convenience method)
     *
     * Description: Get recent logs from Splunk with simple time range.
     * Convenience method for quick log retrieval.
     *
     * @param timeRange Time range for recent logs (e.g., '1h', '24h', '7d'). Default: '1h'
     * @param limit Maximum number of logs to return. Default: 50
     * @return Mono containing the search results
     */
    @McpTool(
            name = "getRecentLogs",
            description = """
                    Get recent logs from Splunk with a simple relative time range.

                    This is a convenience tool for quickly retrieving recent log entries without
                    specifying complex time ranges or queries. Perfect for general monitoring
                    and quick log checks.

                    The tool retrieves all log events from the default index within the specified
                    time range, returning the most recent events first.

                    Time range format: Specify duration without the minus sign
                    - '1h' = last 1 hour
                    - '4h' = last 4 hours
                    - '24h' = last 24 hours
                    - '7d' = last 7 days
                    - '30d' = last 30 days

                    Use this tool when you need to:
                    - Quickly check recent activity in logs
                    - Monitor real-time or near-real-time events
                    - Get a general overview of system activity
                    - Verify that logs are being ingested correctly

                    Parameters:
                    - timeRange: Time duration to look back (default: '1h'). Format: '5m', '1h', '24h', '7d'
                    - limit: Maximum number of logs to return (default: 50)
                    """
    )
    public Mono<SplunkLogResponse> getRecentLogs(
            String timeRange,
            Integer limit
    ) {
        log.info("MCP Tool invoked: getRecentLogs with timeRange={}, limit={}", timeRange, limit);

        String range = timeRange != null ? timeRange : "1h";
        String startTime = "-" + range;

        SplunkLogRequest request = SplunkLogRequest.builder()
                .startTime(startTime)
                .endTime("now")
                .limit(limit != null ? limit : 50)
                .build();

        return splunkSearchService.searchLogs(request);
    }

    /**
     * MCP Tool: Search logs by keyword
     *
     * Description: Search Splunk logs by keyword or phrase.
     * Useful for finding specific events, errors, or patterns in logs.
     *
     * @param keyword Keyword or phrase to search for (e.g., 'error', 'exception')
     * @param timeRange Time range to search (e.g., '1h', '24h', '7d'). Default: '24h'
     * @param limit Maximum number of results. Default: 100
     * @return Mono containing the search results
     */
    @McpTool(
            name = "searchByKeyword",
            description = """
                    Search Splunk logs by keyword or phrase to find specific events or patterns.

                    This tool performs a text search across log messages to find events containing
                    the specified keyword or phrase. The search is case-insensitive and looks for
                    exact matches within the raw log messages.

                    The keyword will be automatically quoted to search for the exact phrase. This is
                    particularly useful for finding:
                    - Error messages and exceptions
                    - Specific user IDs or session IDs
                    - Transaction IDs or correlation IDs
                    - Specific URLs or endpoints
                    - Warning messages or specific log levels

                    Time range format (without minus sign):
                    - '5m' = last 5 minutes
                    - '1h' = last 1 hour
                    - '24h' = last 24 hours
                    - '7d' = last 7 days

                    Use this tool when you need to:
                    - Find all occurrences of an error message
                    - Search for specific transaction or request IDs
                    - Locate logs related to a specific user or entity
                    - Investigate patterns of specific events
                    - Troubleshoot issues by searching for error keywords

                    Parameters:
                    - keyword: The text to search for (e.g., 'error', 'exception', 'user123', 'timeout')
                    - timeRange: Time duration to search (default: '24h'). Format: '5m', '1h', '24h', '7d'
                    - limit: Maximum number of results to return (default: 100)

                    Returns structured log events containing the keyword with full metadata
                    (timestamp, source, sourcetype, host, raw message).
                    """
    )
    public Mono<SplunkLogResponse> searchByKeyword(
            String keyword,
            String timeRange,
            Integer limit
    ) {
        log.info("MCP Tool invoked: searchByKeyword with keyword={}, timeRange={}, limit={}",
                keyword, timeRange, limit);

        String range = timeRange != null ? timeRange : "24h";
        String startTime = "-" + range;

        SplunkLogRequest request = SplunkLogRequest.builder()
                .query("\"" + keyword + "\"")
                .startTime(startTime)
                .endTime("now")
                .limit(limit != null ? limit : 100)
                .build();

        return splunkSearchService.searchLogs(request);
    }
}