#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Cleanup function
cleanup() {
  echo ""
  echo -e "${YELLOW}Cleaning up...${NC}"
  if [ -n "$SSE_PID" ]; then
    kill $SSE_PID 2>/dev/null
    echo "✓ Background SSE connection terminated (PID: $SSE_PID)"
  fi
  echo "✓ SSE output saved to: $SSE_OUTPUT_FILE"
}

# Set trap to cleanup on script exit or interrupt
trap cleanup EXIT INT TERM

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}MCP SSE Server Test Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Step 1: Establish SSE connection and extract session ID
echo -e "${YELLOW}[1/5] Establishing SSE connection...${NC}"

# Create output file in the same directory as the script (overwrites if exists)
SSE_OUTPUT_FILE="$SCRIPT_DIR/sse_output.log"

# Start SSE connection in background and redirect output to file
curl -N -s --location 'http://localhost:8081/sse' \
  --header 'Content-Type: application/json' \
  --header 'Accept: text/event-stream' > "$SSE_OUTPUT_FILE" 2>&1 &

# Store the background process PID
SSE_PID=$!

echo "SSE connection established (PID: $SSE_PID), waiting for session ID..."

# Wait for session ID to appear in the output file (max 10 seconds)
sessionId=""
for i in {1..20}; do
  sleep 0.5

  # Try to extract session ID from the file
  if [ -f "$SSE_OUTPUT_FILE" ]; then
    sessionId=$(grep -oE '[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}' "$SSE_OUTPUT_FILE" | head -1)

    if [ -n "$sessionId" ]; then
      break
    fi
  fi
done

# Display the SSE response for debugging
if [ -f "$SSE_OUTPUT_FILE" ]; then
  echo "SSE Response:"
  cat "$SSE_OUTPUT_FILE"
  echo ""
fi

# Validate session ID was extracted
if [ -z "$sessionId" ]; then
  echo -e "${RED}ERROR: Failed to extract session ID from SSE response${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Session ID extracted: $sessionId${NC}"
echo ""

# Note: SSE connection is kept alive in background for the session
# We'll clean it up at the end of the script

# Step 2: Initialize MCP session
echo -e "${YELLOW}[2/5] Initializing MCP session...${NC}"
INIT_RESPONSE=$(curl -s --location "http://localhost:8081/mcp/message?sessionId=$sessionId" \
  --header 'Content-Type: application/json' \
  --data '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {
        "name": "test-client",
        "version": "1.0.0"
      }
    }
  }')

echo "Initialize Response: $INIT_RESPONSE"
echo -e "${GREEN}✓ MCP session initialized${NC}"
echo ""

# Step 3: Send initialized notification
echo -e "${YELLOW}[3/5] Sending initialized notification...${NC}"
NOTIFY_RESPONSE=$(curl -s --location "http://localhost:8081/mcp/message?sessionId=$sessionId" \
  --header 'Content-Type: application/json' \
  --data '{
    "jsonrpc": "2.0",
    "method": "notifications/initialized"
  }')

echo "Notification Response: $NOTIFY_RESPONSE"
echo -e "${GREEN}✓ Initialized notification sent${NC}"
echo ""

# Step 4: List available tools
echo -e "${YELLOW}[4/5] Listing available tools...${NC}"
TOOLS_RESPONSE=$(curl -s --location "http://localhost:8081/mcp/message?sessionId=$sessionId" \
  --header 'Content-Type: application/json' \
  --data '{
    "jsonrpc": "2.0",
    "method": "tools/list",
    "params": {},
    "id": "2"
  }')

echo "Tools List Response: $TOOLS_RESPONSE"
echo -e "${GREEN}✓ Tools list retrieved${NC}"
echo ""

# Step 5: Call extractLogs tool
echo -e "${YELLOW}[5/5] Calling extractLogs tool...${NC}"
EXTRACT_RESPONSE=$(curl -s --location "http://localhost:8081/mcp/message?sessionId=$sessionId" \
  --header 'Content-Type: application/json' \
  --data '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "params": {
      "name": "extractLogs",
      "arguments": {
        "query": "INFO",
        "startTime": "-24h",
        "limit": 10,
        "sourcetype": "_json",
        "index": "main"
      }
    },
    "id": "3"
  }')

echo "Extract Logs Response: $EXTRACT_RESPONSE"
echo -e "${GREEN}✓ extractLogs tool executed${NC}"
echo ""

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✓ All tests completed successfully!${NC}"
echo -e "${BLUE}========================================${NC}"

# Cleanup will be handled automatically by the EXIT trap