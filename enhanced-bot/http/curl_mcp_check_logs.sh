curl --location 'http://localhost:8080/api/mcp/chat' \
--header 'Content-Type: application/json' \
--data '{
          "sessionId": "326ee1f7-3203-4e80-98f3-e54bd854d8a7",
          "message": "Can You check my application has any logs with the index name \"main\" for the last 24 hours?"
}'
