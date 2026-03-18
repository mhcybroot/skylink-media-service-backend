#!/bin/bash

# Swagger JWT Implementation Test Script
# Verifies JWT security scheme is properly configured in OpenAPI

echo "=========================================="
echo "Swagger JWT Implementation Test"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

TESTS_PASSED=0
TESTS_FAILED=0

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
if ! curl -s -o /dev/null -w "%{http_code}" "http://localhost:8085" > /dev/null 2>&1; then
    echo -e "${RED}ERROR: Server is not running at http://localhost:8085${NC}"
    echo "Please start the application first: ./gradlew bootRun"
    exit 1
fi
echo -e "${GREEN}Server is running${NC}"
echo ""

# Test 1: Check if OpenAPI spec is accessible
echo "Test 1: OpenAPI specification accessibility"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8085/v3/api-docs")
if [ "$HTTP_CODE" -eq 200 ]; then
    print_result 0 "OpenAPI spec accessible at /v3/api-docs"
else
    print_result 1 "OpenAPI spec not accessible (HTTP $HTTP_CODE)"
fi
echo ""

# Test 2: Check if security scheme is defined
echo "Test 2: JWT security scheme configuration"
SECURITY_SCHEME=$(curl -s http://localhost:8085/v3/api-docs | grep -o '"bearerAuth"' | head -1)
if [ -n "$SECURITY_SCHEME" ]; then
    print_result 0 "JWT security scheme 'bearerAuth' is defined"
else
    print_result 1 "JWT security scheme 'bearerAuth' not found"
fi
echo ""

# Test 3: Check security scheme details
echo "Test 3: Security scheme details"
SCHEME_TYPE=$(curl -s http://localhost:8085/v3/api-docs | grep -A 5 '"bearerAuth"' | grep '"type"' | grep -o '"http"')
if [ -n "$SCHEME_TYPE" ]; then
    print_result 0 "Security scheme type is 'http'"
else
    print_result 1 "Security scheme type not correctly set"
fi
echo ""

# Test 4: Check if Swagger UI is accessible
echo "Test 4: Swagger UI accessibility"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8085/swagger-ui/index.html")
if [ "$HTTP_CODE" -eq 200 ]; then
    print_result 0 "Swagger UI accessible at /swagger-ui/index.html"
else
    print_result 1 "Swagger UI not accessible (HTTP $HTTP_CODE)"
fi
echo ""

# Test 5: Check if login endpoint is documented
echo "Test 5: Login endpoint documentation"
LOGIN_ENDPOINT=$(curl -s http://localhost:8085/v3/api-docs | grep -o '"/api/v1/auth/login"')
if [ -n "$LOGIN_ENDPOINT" ]; then
    print_result 0 "Login endpoint documented in OpenAPI spec"
else
    print_result 1 "Login endpoint not found in OpenAPI spec"
fi
echo ""

# Test 6: Verify API info is present
echo "Test 6: API metadata"
API_TITLE=$(curl -s http://localhost:8085/v3/api-docs | grep -o '"title":"Skylink Media Service API"')
if [ -n "$API_TITLE" ]; then
    print_result 0 "API title configured correctly"
else
    print_result 1 "API title not found or incorrect"
fi
echo ""

# Test 7: Test actual login functionality
echo "Test 7: Login endpoint functionality"
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8085/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"contractor1","password":"password123"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    print_result 0 "Login works and returns JWT token"
    echo "   Token preview: ${TOKEN:0:50}..."
else
    print_result 1 "Login failed or no token returned"
fi
echo ""

# Test 8: Check if security requirement is applied globally
echo "Test 8: Global security requirement"
SECURITY_REQ=$(curl -s http://localhost:8085/v3/api-docs | grep -o '"security":\[{"bearerAuth":\[\]}' | head -1)
if [ -n "$SECURITY_REQ" ]; then
    print_result 0 "Global security requirement applied"
else
    print_result 1 "Global security requirement not found"
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
    echo -e "${GREEN}✓ All tests passed!${NC}"
    echo ""
    echo "Swagger UI is ready to use:"
    echo "1. Open: http://localhost:8085/swagger-ui/index.html"
    echo "2. Click 'Authorize' button (🔒 icon)"
    echo "3. Paste JWT token from login response"
    echo "4. Test protected endpoints"
    exit 0
else
    echo -e "${RED}✗ Some tests failed!${NC}"
    exit 1
fi
