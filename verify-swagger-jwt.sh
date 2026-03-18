#!/bin/bash

# Swagger JWT Implementation Verification Script
# Verifies all files are in place and application builds successfully

echo "=========================================="
echo "Swagger JWT Implementation Verification"
echo "=========================================="
echo ""

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

CHECKS_PASSED=0
CHECKS_FAILED=0

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $2"
        ((CHECKS_PASSED++))
    else
        echo -e "${RED}✗${NC} $2 - FILE MISSING: $1"
        ((CHECKS_FAILED++))
    fi
}

echo "Checking Implementation Files..."
check_file "src/main/java/root/cyb/mh/skylink_media_service/infrastructure/config/OpenApiConfig.java" "OpenApiConfig.java"
check_file "src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java" "AuthApiController.java (should have annotations)"
echo ""

echo "Checking Test & Documentation Files..."
check_file "test-swagger-jwt.sh" "Swagger JWT test script"
check_file "SWAGGER_JWT_IMPLEMENTATION.md" "Implementation documentation"
echo ""

echo "Checking OpenAPI Annotations..."
if grep -q "@Tag" src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java; then
    echo -e "${GREEN}✓${NC} @Tag annotation present"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗${NC} @Tag annotation missing"
    ((CHECKS_FAILED++))
fi

if grep -q "@SecurityRequirements" src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java; then
    echo -e "${GREEN}✓${NC} @SecurityRequirements annotation present"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗${NC} @SecurityRequirements annotation missing"
    ((CHECKS_FAILED++))
fi

if grep -q "@Operation" src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java; then
    echo -e "${GREEN}✓${NC} @Operation annotation present"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗${NC} @Operation annotation missing"
    ((CHECKS_FAILED++))
fi
echo ""

echo "Running Build..."
if ./gradlew build -x test > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} Application builds successfully"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗${NC} Build failed"
    ((CHECKS_FAILED++))
fi
echo ""

echo "Running Tests..."
if ./gradlew test > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} All tests pass"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗${NC} Some tests failed"
    ((CHECKS_FAILED++))
fi
echo ""

echo "=========================================="
echo "Verification Summary"
echo "=========================================="
echo -e "Checks Passed: ${GREEN}${CHECKS_PASSED}${NC}"
echo -e "Checks Failed: ${RED}${CHECKS_FAILED}${NC}"
echo "Total Checks: $((CHECKS_PASSED + CHECKS_FAILED))"
echo ""

if [ $CHECKS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ Implementation verified successfully!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Start the application: ./gradlew bootRun"
    echo "2. Open Swagger UI: http://localhost:8085/swagger-ui/index.html"
    echo "3. Test JWT authentication: ./test-swagger-jwt.sh"
    echo "4. Read documentation: cat SWAGGER_JWT_IMPLEMENTATION.md"
    exit 0
else
    echo -e "${RED}✗ Verification failed!${NC}"
    exit 1
fi
