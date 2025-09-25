#!/bin/bash

# Test script for JFRAPI endpoints with Javalin
echo "Testing JFRAPI endpoints with Javalin and CORS support"
echo "======================================================"

BASE_URL="http://localhost:8080"

echo "1. Testing health endpoint:"
curl -X GET "$BASE_URL/health" \
     -w "\nStatus: %{http_code}\n\n"

echo "2. Testing CORS preflight request:"
curl -X OPTIONS "$BASE_URL/query" \
     -H "Origin: http://localhost:5173" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -v -w "\nStatus: %{http_code}\n\n"

echo "3. Testing POST with CORS headers (simulating frontend on port 5173):"
curl -X POST "$BASE_URL/query" \
     -H "Origin: http://localhost:5173" \
     -H "Content-Type: application/json" \
     -d '{"query": "SELECT * FROM jdk.CPULoad"}' \
     -w "\nStatus: %{http_code}\n\n"

echo "4. Testing with URL parameters:"
curl -X POST "$BASE_URL/query?query=SELECT%20*%20FROM%20jdk.CPULoad" \
     -H "Origin: http://localhost:5173" \
     -w "\nStatus: %{http_code}\n\n"

echo "5. Testing with JSON body:"
curl -X POST "$BASE_URL/query" \
     -H "Origin: http://localhost:5173" \
     -H "Content-Type: application/json" \
     -d '{"query": "SELECT * FROM jdk.GarbageCollection"}' \
     -w "\nStatus: %{http_code}\n\n"

echo "6. Testing with form data:"
curl -X POST "$BASE_URL/query" \
     -H "Origin: http://localhost:5173" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "query=SELECT * FROM jdk.ThreadSleep" \
     -w "\nStatus: %{http_code}\n\n"

echo "7. Testing with missing query (should return 400):"
curl -X POST "$BASE_URL/query" \
     -H "Origin: http://localhost:5173" \
     -H "Content-Type: application/json" \
     -d '{}' \
     -w "\nStatus: %{http_code}\n\n"

echo "8. Testing with GET method (should return 404 since only POST is defined):"
curl -X GET "$BASE_URL/query?query=test" \
     -H "Origin: http://localhost:5173" \
     -w "\nStatus: %{http_code}\n\n"

echo "9. Testing with different origin (should work with Javalin CORS):"
curl -X POST "$BASE_URL/query" \
     -H "Origin: http://example.com" \
     -H "Content-Type: application/json" \
     -d '{"query": "SELECT * FROM jdk.CPULoad"}' \
     -w "\nStatus: %{http_code}\n\n"
