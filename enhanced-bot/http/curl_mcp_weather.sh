curl --location 'http://localhost:8080/api/mcp/chat' \
--header 'Content-Type: application/json' \
--data '{
          "sessionId": "326ee1f7-3203-4e80-98f3-e54bd854d8a7",
          "message": "What is the temperature in New York now? The New York latitude is 40.7128° N, longitude is 74.0060° W"
}'
