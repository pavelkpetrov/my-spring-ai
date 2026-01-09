#!/bin/bash

# Script to load tutorial data into Splunk
# Usage: ./load-tutorial-data.sh

# Don't exit on error - we want to show all results
set +e

CONTAINER_NAME="splunk-sandbox"
SPLUNK_USER="admin"
SPLUNK_PASSWORD="Admin123!"
TUTORIAL_DATA_DIR="/tutorial-data"
SPLUNK_EXEC_USER="splunk"

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo "==================================="
echo "Splunk Tutorial Data Loader"
echo "==================================="
echo "Working directory: $SCRIPT_DIR"
echo ""

# Check if container is running
if ! docker ps | grep -q $CONTAINER_NAME; then
    echo "Error: Splunk container is not running!"
    echo "Start it with: docker-compose up -d"
    exit 1
fi

echo "✓ Container is running"

# Wait for Splunk to be ready
echo "Waiting for Splunk to be ready..."
MAX_RETRIES=30
RETRY_COUNT=0
until docker exec -u $SPLUNK_EXEC_USER $CONTAINER_NAME /opt/splunk/bin/splunk status 2>&1 | grep -q "splunkd is running"; do
    echo "  Still waiting... ($RETRY_COUNT/$MAX_RETRIES)"
    sleep 5
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
        echo "Error: Splunk did not start within expected time"
        echo "Check logs with: docker-compose logs splunk"
        exit 1
    fi
done

echo "✓ Splunk is ready"

# Check if tutorial-data directory has files (ignoring .gitkeep)
FILE_COUNT=$(ls -1 tutorial-data | grep -v "^\.gitkeep$" | wc -l)
if [ "$FILE_COUNT" -eq 0 ]; then
    echo ""
    echo "No tutorial data files found in ./tutorial-data directory"
    echo "Creating sample data files with dynamic timestamps..."

    # Generate dynamic timestamps
    # Current time
    NOW_APACHE=$(date -u +"%d/%b/%Y:%H:%M:%S +0000")
    NOW_APP=$(date -u +"%Y-%m-%d %H:%M:%S")
    NOW_ISO=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

    # 24 hours ago
    if date --version >/dev/null 2>&1; then
        # GNU date (Linux)
        PAST_APACHE=$(date -u -d "24 hours ago" +"%d/%b/%Y:%H:%M:%S +0000")
        PAST_APP=$(date -u -d "24 hours ago" +"%Y-%m-%d %H:%M:%S")
        PAST_ISO=$(date -u -d "24 hours ago" +"%Y-%m-%dT%H:%M:%SZ")

        FUTURE_APACHE=$(date -u -d "24 hours" +"%d/%b/%Y:%H:%M:%S +0000")
        FUTURE_APP=$(date -u -d "24 hours" +"%Y-%m-%d %H:%M:%S")
        FUTURE_ISO=$(date -u -d "24 hours" +"%Y-%m-%dT%H:%M:%SZ")
    else
        # BSD date (macOS)
        PAST_APACHE=$(date -u -v-24H +"%d/%b/%Y:%H:%M:%S +0000")
        PAST_APP=$(date -u -v-24H +"%Y-%m-%d %H:%M:%S")
        PAST_ISO=$(date -u -v-24H +"%Y-%m-%dT%H:%M:%SZ")

        FUTURE_APACHE=$(date -u -v+24H +"%d/%b/%Y:%H:%M:%S +0000")
        FUTURE_APP=$(date -u -v+24H +"%Y-%m-%d %H:%M:%S")
        FUTURE_ISO=$(date -u -v+24H +"%Y-%m-%dT%H:%M:%SZ")
    fi

    echo "  Generating data for:"
    echo "    - 24 hours ago: $PAST_ISO"
    echo "    - Current time: $NOW_ISO"
    echo "    - 24 hours ahead: $FUTURE_ISO"

    # Create sample web access log with dynamic timestamps
    cat > tutorial-data/sample_web_access.log << EOF
192.168.1.100 - - [$PAST_APACHE] "GET /index.html HTTP/1.1" 200 1234
192.168.1.101 - - [$PAST_APACHE] "POST /api/login HTTP/1.1" 200 567
192.168.1.102 - - [$NOW_APACHE] "GET /products HTTP/1.1" 200 2345
192.168.1.103 - - [$NOW_APACHE] "GET /cart HTTP/1.1" 200 890
192.168.1.104 - - [$NOW_APACHE] "POST /api/checkout HTTP/1.1" 500 123
192.168.1.105 - - [$FUTURE_APACHE] "GET /error.html HTTP/1.1" 404 456
192.168.1.106 - - [$FUTURE_APACHE] "GET /api/status HTTP/1.1" 200 789
EOF

    # Create sample application log with dynamic timestamps
    cat > tutorial-data/sample_application.log << EOF
$PAST_APP,123 INFO [main] Application started successfully
$PAST_APP,456 INFO [http-thread-1] User login: username=john.doe
$NOW_APP,789 DEBUG [http-thread-2] Database query executed in 45ms
$NOW_APP,012 WARN [http-thread-3] Slow query detected: 2500ms
$NOW_APP,345 ERROR [http-thread-4] Payment processing failed: Connection timeout
$FUTURE_APP,678 INFO [http-thread-5] Session expired for user: jane.smith
$FUTURE_APP,901 WARN [http-thread-6] Cache miss detected for key: user_profile_456
EOF

    # Create sample JSON events with dynamic timestamps
    cat > tutorial-data/sample_events.json << EOF
{"timestamp":"$PAST_ISO","level":"INFO","service":"api-gateway","message":"Request received","user_id":"user123","endpoint":"/api/users"}
{"timestamp":"$PAST_ISO","level":"INFO","service":"auth-service","message":"Authentication successful","user_id":"user123","method":"oauth2"}
{"timestamp":"$NOW_ISO","level":"WARN","service":"database","message":"Connection pool exhausted","active_connections":100,"max_connections":100}
{"timestamp":"$NOW_ISO","level":"ERROR","service":"payment-service","message":"Payment declined","user_id":"user456","amount":99.99,"reason":"insufficient_funds"}
{"timestamp":"$NOW_ISO","level":"INFO","service":"notification","message":"Email sent","user_id":"user123","type":"order_confirmation"}
{"timestamp":"$FUTURE_ISO","level":"INFO","service":"api-gateway","message":"Health check passed","response_time_ms":45}
{"timestamp":"$FUTURE_ISO","level":"WARN","service":"cache","message":"Redis latency spike detected","latency_ms":250}
EOF

    echo "✓ Sample data files created with dynamic timestamps"
fi

# Verify files are accessible in container
echo ""
echo "Verifying tutorial data directory in container..."
CONTAINER_FILES=$(docker exec $CONTAINER_NAME ls -1 $TUTORIAL_DATA_DIR 2>/dev/null | grep -v ".gitkeep" | wc -l)
if [ "$CONTAINER_FILES" -eq 0 ]; then
    echo "⚠ Warning: No data files found in container at $TUTORIAL_DATA_DIR"
    echo "Check that volume mount is working correctly"
fi

# Load data files
echo ""
echo "Loading data files into Splunk..."
echo ""

SUCCESS_COUNT=0
FAIL_COUNT=0

for file in tutorial-data/*; do
    if [ -f "$file" ] && [ "$(basename "$file")" != ".gitkeep" ]; then
        filename=$(basename "$file")
        echo "  Loading: $filename"

        # Verify file exists in container
        if ! docker exec $CONTAINER_NAME test -f "$TUTORIAL_DATA_DIR/$filename" 2>/dev/null; then
            echo "    ✗ File not accessible in container"
            FAIL_COUNT=$((FAIL_COUNT + 1))
            continue
        fi

        # Determine sourcetype based on file extension
        case "$filename" in
            *.log)
                sourcetype="syslog"
                ;;
            *.json)
                sourcetype="_json"
                ;;
            *.csv)
                sourcetype="csv"
                ;;
            *)
                sourcetype="auto"
                ;;
        esac

        # Add data to Splunk (run as splunk user)
        OUTPUT=$(docker exec -u $SPLUNK_EXEC_USER $CONTAINER_NAME /opt/splunk/bin/splunk add oneshot \
            "$TUTORIAL_DATA_DIR/$filename" \
            -sourcetype "$sourcetype" \
            -index main \
            -auth "$SPLUNK_USER:$SPLUNK_PASSWORD" 2>&1)

        if echo "$OUTPUT" | grep -qi "Oneshot\|Added"; then
            echo "    ✓ Loaded with sourcetype: $sourcetype"
            SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        else
            echo "    ✗ Failed to load"
            echo "    Output: $(echo "$OUTPUT" | grep -v "^Passphrase" | head -3 | tr '\n' ' ')"
            FAIL_COUNT=$((FAIL_COUNT + 1))
        fi
    fi
done

echo ""
echo "==================================="
if [ $FAIL_COUNT -eq 0 ]; then
    echo "✓ Tutorial data loaded successfully!"
    echo "  Files loaded: $SUCCESS_COUNT"
else
    echo "⚠ Tutorial data loading completed with errors"
    echo "  Success: $SUCCESS_COUNT"
    echo "  Failed: $FAIL_COUNT"
fi
echo "==================================="
echo ""
echo "Access Splunk Web at: http://localhost:8000"
echo "Username: $SPLUNK_USER"
echo "Password: $SPLUNK_PASSWORD"
echo ""
echo "Try this search in Splunk:"
echo "  index=main | stats count by sourcetype"
echo ""