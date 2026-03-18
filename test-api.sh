#!/bin/bash

# REST API Test Script
# Tests the contractor login API endpoint

echo "=========================================="
echo "REST API Test Script"
echo "=========================================="
echo ""

# Configuration
BASE_URL="http://localhost:8085"
API_ENDPOINT="${BASE_URL}/api/v1/auth/login"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to print test result
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASSED${NC}: $2"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ FAILED${NC}: $2"
        ((TESTS_FAILED++))
    fi
}

# Check if server is running
echo "Checking if server is running..."
if ! curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}" > /dev/null 2>&1; then
    echo -e "${RED}ERROR: Server is not running at ${BASE_URL}${NC}"
    echo "Please start the application first: ./gradlew bootRun"
    exit 1
fi
echo -e "${GREEN}Server is running${NC}"
echo ""

# Test 1: Successful login with valid credentials
echo "Test 1: Successful login with valid credentials"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_ENDPOINT}" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "contractor1",
        "password": "password123"
    }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    TOKEN=$(echo "$BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    if [ -n "$TOKEN" ]; then
        print_result 0 "Login successful, token received"
        echo "   Token: ${TOKEN:0:50}..."
    else
        print_result 1 "Login returned 200 but no token found"
    fi
else
    print_result 1 "Expected HTTP 200, got $HTTP_CODE"
    echo "   Response: $BODY"
fi
echo ""

# Test 2: Login with invalid username
echo "Test 2: Login with invalid username"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_ENDPOINT}" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "nonexistent",
        "password": "password123"
    }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 401 ]; then
    print_result 0 "Correctly returned 401 for invalid username"
else
    print_result 1 "Expected HTTP 401, got $HTTP_CODE"
fi
echo ""

# Test 3: Login with invalid password
echo "Test 3: Login with invalid password"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_ENDPOINT}" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "contractor1",
        "password": "wrongpassword"
    }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 401 ]; then
    print_result 0 "Correctly returned 401 for invalid password"
else
    print_result 1 "Expected HTTP 401, got $HTTP_CODE"
fi
echo ""

# Test 4: Login with short username (validation error)
echo "Test 4: Login with short username (validation error)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_ENDPOINT}" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "ab",
        "password": "password123"
    }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 400 ]; then
    if echo "$BODY" | grep -q "validationErrors"; then
        print_result 0 "Correctly returned 400 with validation errors"
    else
        print_result 1 "Returned 400 but no validation errors in response"
    fi
else
    print_result 1 "Expected HTTP 400, got $HTTP_CODE"
fi
echo ""

# Test 5: Login with short password (validation error)
echo "Test 5: Login with short password (validation error)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_ENDPOINT}" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "contractor1",
        "password": "short"
    }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Correctly returned 400 for short password"
else
    print_result 1 "Expected HTTP 400, got $HTTP_CODE"
fi
echo ""

# Test 6: Login with missing fields
echo "Test 6: Login with missing fields"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_ENDPOINT}" \
    -H "Content-Type: application/json" \
    -d '{}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Correctly returned 400 for missing fields"
else
    print_result 1 "Expected HTTP 400, got $HTTP_CODE"
fi
echo ""

# Test 7: Login with malformed JSON
echo "Test 7: Login with malformed JSON"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_ENDPOINT}" \
    -H "Content-Type: application/json" \
    -d '{invalid json}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 400 ]; then
    print_result 0 "Correctly returned 400 for malformed JSON"
else
    print_result 1 "Expected HTTP 400, got $HTTP_CODE"
fi
echo ""

# Test 8: Verify response structure
echo "Test 8: Verify response structure for successful login"
RESPONSE=$(curl -s -X POST "${API_ENDPOINT}" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "contractor1",
        "password": "password123"
    }')

REQUIRED_FIELDS=("token" "tokenType" "contractorId" "fullName" "expiresAt" "expiresIn")
ALL_PRESENT=true

for field in "${REQUIRED_FIELDS[@]}"; do
    if ! echo "$RESPONSE" | grep -q "\"$field\""; then
        ALL_PRESENT=false
        echo "   Missing field: $field"
    fi
done

if [ "$ALL_PRESENT" = true ]; then
    print_result 0 "All required fields present in response"
else
    print_result 1 "Some required fields missing in response"
fi
echo ""

# Summary
echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "Tests Passed: ${GREEN}${TESTS_PASSED}${NC}"
echo -e "Tests Failed: ${RED}${TESTS_FAILED}${NC}"
echo "Total Tests: $((TESTS_PASSED + TESTS_FAILED))"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed! ✓${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed! ✗${NC}"
    exit 1
fi
