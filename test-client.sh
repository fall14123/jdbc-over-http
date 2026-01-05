#!/bin/bash

# HTTP JDBC Client Test Script
# Tests the JDBC over HTTP implementation

echo "=== JDBC over HTTP Test Script ==="
echo

# Check if server is running
echo "1. Testing server connectivity..."
if curl -s -X POST -d "SELECT 1" "http://user:pass@localhost:9999/" > /dev/null; then
    echo "✓ Server is responding on localhost:9999"
else
    echo "✗ Server is not responding. Make sure your HTTP server is running on port 9999"
    exit 1
fi

echo

# Test basic query
echo "2. Testing basic query..."
response=$(curl -s -X POST -d "SELECT 'hello' as greeting, 'world' as target" "http://user:pass@localhost:9999/")
echo "Response: $response"

echo

# Test with version function
echo "3. Testing version query..."
response=$(curl -s -X POST -d "SELECT version()" "http://user:pass@localhost:9999/")
echo "Response: $response"

echo

# Test numeric values
echo "4. Testing numeric query..."
response=$(curl -s -X POST -d "SELECT 42 as answer, 3.14159 as pi, true as flag" "http://user:pass@localhost:9999/")
echo "Response: $response"

echo

# Test error handling
echo "5. Testing error handling..."
response=$(curl -s -X POST -d "SELECT * FROM non_existent_table" "http://user:pass@localhost:9999/")
echo "Response: $response"

echo

# Test different HTTP methods (should fail)
echo "6. Testing unsupported HTTP method..."
response=$(curl -s -X GET "http://user:pass@localhost:9999/")
echo "Response: $response"

echo

echo "=== Test completed ==="

# Run Java client example if available
if [ -f "./gradlew" ]; then
    echo
    echo "7. Running Java client example..."
    ./gradlew run -PmainClass=com.example.jdbc.http.HttpJdbcClientExample --quiet
fi