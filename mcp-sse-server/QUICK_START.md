# Splunk MCP Server - Quick Start Guide

## 1. Start Splunk (if not running)

```bash
cd docker
docker-compose up -d
```

Wait ~2 minutes for Splunk to initialize. Verify at http://localhost:8000 (admin/Admin123!)

## 2. Build the MCP Server

```bash
cd <root>/mcp-server
mvn clean install
```

## 3. Run the Server

```bash
mvn spring-boot:run
```

Server starts on **http://localhost:8081**

## 4. Quick Tests

### Health Check
```bash
curl http://localhost:8081/api/splunk/health
```

### Test SSE Connection
```bash
curl -N http://localhost:8081/api/splunk/test/sse
```

### Stream Recent Logs
```bash
curl -N "http://localhost:8081/api/splunk/logs/stream?startTime=-1h&limit=5"
```

### Get Logs (Batch)
```bash
curl "http://localhost:8081/api/splunk/logs?startTime=-1h&limit=5" | jq
```

## 5. Run Test Suite

### Bash Tests
```bash
./curl/testSSE.sh
```

### Search Error Logs (Last Hour)
```bash
curl -N "http://localhost:8081/api/splunk/logs/stream?query=error&startTime=-1h"
```

### Get Last 24 Hours (Batch)
```bash
curl -X POST http://localhost:8081/api/splunk/logs \
  -H "Content-Type: application/json" \
  -d '{"startTime":"-24h","limit":100}' | jq
```

### Stream with Filter
```bash
curl -N -X POST http://localhost:8081/api/splunk/logs/stream \
  -H "Content-Type: application/json" \
  -d '{
    "query": "status=200",
    "startTime": "-1h",
    "limit": 10
  }'
```

## Available Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/splunk/health` | Health check |
| GET | `/api/splunk/test/sse` | Test SSE connection |
| GET | `/api/splunk/logs/stream` | Stream logs (SSE) |
| POST | `/api/splunk/logs/stream` | Stream logs with body (SSE) |
| GET | `/api/splunk/logs` | Get logs (batch) |
| POST | `/api/splunk/logs` | Get logs with body (batch) |

## MCP Tools Available

1. **extractLogs** - Extract logs by time range
2. **testConnection** - Test Splunk connection
3. **getRecentLogs** - Get recent logs (convenience)
4. **searchByKeyword** - Search by keyword

## Troubleshooting

### "Connection refused"
- Splunk not running → `docker-compose up -d`
- Check port 8089 is accessible

### "No results"
- Load sample data: `./docker/load-tutorial-data.sh`
- Check time range includes data

### "SSL error"
- Expected for local dev (SSL verification disabled)
- Configuration: `splunk.ssl-verify=false`

## Next Steps

1. ✅ Review SPLUNK_MCP_README.md for detailed documentation
2. ✅ Load sample data to Splunk
3. ✅ Test MCP tools with AI assistant
4. ✅ Integrate with your application

## Architecture Summary

```
Client (curl/Python/AI)
    ↓
SSE Stream / REST API
    ↓
SplunkSseController
    ↓
SplunkService (Reactive)
    ↓
Splunk SDK
    ↓
Splunk Server (Docker)
```

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
splunk:
  host: localhost
  port: 8089
  username: admin
  password: Admin123!
  default-index: main
  search-timeout: 300
```

Or use environment variables:
```bash
export SPLUNK_HOST=localhost
export SPLUNK_PASSWORD=YourPassword
mvn spring-boot:run
```