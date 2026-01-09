#!/bin/bash

# --- Configuration ---
API_ENDPOINT="http://localhost:8080/api/voice/chat"
SESSION_ID="sess_1764381493060729"
FILE_PATH="./what_is_a_capital_of_france.webm" # The script expects the file path as the first argument

# --- Function to check for required commands ---
check_prerequisites() {
    if ! command -v base64 &> /dev/null
    then
        echo "Error: 'base64' command not found. Please install it."
        exit 1
    fi

    if ! command -v curl &> /dev/null
    then
        echo "Error: 'curl' command not found. Please install it."
        exit 1
    fi
}

# --- Main Logic ---

# 1. Check if a file path was provided
if [ -z "$FILE_PATH" ]; then
    echo "Usage: $0 <path_to_audio_file>"
    exit 1
fi

# 2. Check if the file exists
if [ ! -f "$FILE_PATH" ]; then
    echo "Error: File not found at '$FILE_PATH'"
    exit 1
fi

# 3. Check for necessary commands
check_prerequisites

# 4. Read file content and convert to Base64
# The '<' redirects the file content to the 'base64' command.
echo "Encoding file: $FILE_PATH to Base64..."
BASE64_STRING=$(base64 < "$FILE_PATH" | tr -d '\n')

# Check if the Base64 conversion was successful (i.e., not empty)
if [ -z "$BASE64_STRING" ]; then
    echo "Error: Base64 string is empty. The file might be empty or the command failed."
    exit 1
fi

# 5. Construct the JSON payload
# Note: The quotes for the 'voice' value are handled by the shell/JSON structure.
# Base64 string is inserted directly.
JSON_PAYLOAD=$(cat <<EOF
{
  "sessionId": "$SESSION_ID",
  "voice": "$BASE64_STRING"
}
EOF
)

# 6. Send the request using curl
echo "Sending cURL request to $API_ENDPOINT..."

curl -X POST "$API_ENDPOINT" \
     -H "Content-Type: application/json" \
     -d "$JSON_PAYLOAD"

echo "" # Final newline for clean output
echo "Request complete."