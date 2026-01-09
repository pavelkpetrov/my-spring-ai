#!/bin/bash

# Script to test Splunk connectivity and API access
# Usage: ./test-connection.sh

set -e

SPLUNK_HOST="localhost"
SPLUNK_WEB_PORT="8000"
SPLUNK_API_PORT="8089"
SPLUNK_HEC_PORT="8088"
SPLUNK_USER="admin"
SPLUNK_PASSWORD="Admin123!"

echo "========================================="
echo "Splunk Sandbox Connection Test"
echo "========================================="
echo ""

# Test 1: Check if container is running
echo "[1/6] Checking if Splunk container is running..."
if docker ps | grep -q splunk-sandbox; then
    echo "  ✓ Container is running"
else
    echo "  ✗ Container is NOT running"
    echo "  Start it with: docker-compose up -d"
    exit 1
fi
echo ""

# Test 2: Check Web UI port
echo "[2/6] Testing Web UI (port $SPLUNK_WEB_PORT)..."
if curl -s -o /dev/null -w "%{http_code}" http://$SPLUNK_HOST:$SPLUNK_WEB_PORT 2>/dev/null | grep -q "200\|303"; then
    echo "  ✓ Web UI is accessible at http://$SPLUNK_HOST:$SPLUNK_WEB_PORT"
else
    echo "  ✗ Web UI is not accessible"
    echo "  Splunk may still be starting up. Wait 1-2 minutes and try again."
fi
echo ""

# Test 3: Check Management API port
echo "[3/6] Testing Management API (port $SPLUNK_API_PORT)..."
if curl -k -s -o /dev/null -w "%{http_code}" https://$SPLUNK_HOST:$SPLUNK_API_PORT 2>/dev/null | grep -q "401\|200"; then
    echo "  ✓ Management API is accessible at https://$SPLUNK_HOST:$SPLUNK_API_PORT"
else
    echo "  ✗ Management API is not accessible"
fi
echo ""

# Test 4: Test authentication
echo "[4/6] Testing authentication..."
AUTH_TEST=$(curl -k -s -u "$SPLUNK_USER:$SPLUNK_PASSWORD" \
    "https://$SPLUNK_HOST:$SPLUNK_API_PORT/services/server/info" \
    -w "%{http_code}" -o /tmp/splunk_auth_test 2>/dev/null)

if echo "$AUTH_TEST" | grep -q "200"; then
    echo "  ✓ Authentication successful"

    # Extract version info
    VERSION=$(grep -o '<s:key name="version">[^<]*' /tmp/splunk_auth_test | sed 's/.*>//' || echo "Unknown")
    BUILD=$(grep -o '<s:key name="build">[^<]*' /tmp/splunk_auth_test | sed 's/.*>//' || echo "Unknown")

    echo "  ✓ Splunk Version: $VERSION (Build: $BUILD)"
    rm -f /tmp/splunk_auth_test
else
    echo "  ✗ Authentication failed"
    echo "  Check username/password: $SPLUNK_USER / $SPLUNK_PASSWORD"
fi
echo ""

# Test 5: Check HEC port
echo "[5/6] Testing HTTP Event Collector (port $SPLUNK_HEC_PORT)..."
HEC_TEST=$(curl -k -s -o /dev/null -w "%{http_code}" https://$SPLUNK_HOST:$SPLUNK_HEC_PORT/services/collector/health 2>/dev/null)

if echo "$HEC_TEST" | grep -q "200"; then
    echo "  ✓ HEC is accessible at https://$SPLUNK_HOST:$SPLUNK_HEC_PORT"
else
    echo "  ⚠ HEC health check returned: $HEC_TEST"
    echo "  HEC may need to be enabled in Splunk Web"
fi
echo ""

# Test 6: Send test event via HEC
echo "[6/6] Testing data ingestion via HEC..."
HEC_RESPONSE=$(curl -k -s \
    -H "Authorization: Splunk 12345678-1234-1234-1234-123456789012" \
    -d '{"event": "Test event from connection test script", "sourcetype": "test"}' \
    https://$SPLUNK_HOST:$SPLUNK_HEC_PORT/services/collector/event 2>/dev/null)

if echo "$HEC_RESPONSE" | grep -q '"code":0'; then
    echo "  ✓ Successfully sent test event via HEC"
else
    echo "  ⚠ Could not send test event"
    echo "  Response: $HEC_RESPONSE"
    echo "  You may need to configure HEC in Splunk Web"
fi
echo ""

# Summary
echo "========================================="
echo "Test Summary"
echo "========================================="
echo ""
echo "Splunk Web UI: http://$SPLUNK_HOST:$SPLUNK_WEB_PORT"
echo "  Username: $SPLUNK_USER"
echo "  Password: $SPLUNK_PASSWORD"
echo ""
echo "Management API: https://$SPLUNK_HOST:$SPLUNK_API_PORT"
echo "HEC Endpoint: https://$SPLUNK_HOST:$SPLUNK_HEC_PORT"
echo ""
echo "To load tutorial data, run:"
echo "  ./load-tutorial-data.sh"
echo ""
echo "To view logs, run:"
echo "  docker-compose logs -f splunk"
echo ""
echo "========================================="