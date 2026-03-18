#!/bin/bash

# Implementation Verification Script
# Verifies all files are in place and application builds successfully

echo "=========================================="
echo "REST API Implementation Verification"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
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

echo "Checking Application Layer Files..."
check_file "src/main/java/root/cyb/mh/skylink_media_service/application/dto/api/LoginRequest.java" "LoginRequest DTO"
check_file "src/main/java/root/cyb/mh/skylink_media_service/application/dto/api/LoginResponse.java" "LoginResponse DTO"
check_file "src/main/java/root/cyb/mh/skylink_media_service/application/dto/api/ErrorResponse.java" "ErrorResponse DTO"
check_file "src/main/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCase.java" "ContractorLoginUseCase"
echo ""

echo "Checking Infrastructure Layer Files..."
check_file "src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProvider.java" "JwtTokenProvider"
check_file "src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtAuthenticationFilter.java" "JwtAuthenticationFilter"
check_file "src/main/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtAuthenticationEntryPoint.java" "JwtAuthenticationEntryPoint"
check_file "src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/AuthApiController.java" "AuthApiController"
check_file "src/main/java/root/cyb/mh/skylink_media_service/infrastructure/web/api/exception/GlobalApiExceptionHandler.java" "GlobalApiExceptionHandler"
echo ""

echo "Checking Test Files..."
check_file "src/test/java/root/cyb/mh/skylink_media_service/infrastructure/security/jwt/JwtTokenProviderTest.java" "JwtTokenProviderTest"
check_file "src/test/java/root/cyb/mh/skylink_media_service/application/usecases/ContractorLoginUseCaseTest.java" "ContractorLoginUseCaseTest"
echo ""

echo "Checking Documentation Files..."
check_file "REST_API_IMPLEMENTATION.md" "API Implementation Documentation"
check_file "DELIVERY_SUMMARY.md" "Delivery Summary"
check_file "API_QUICK_REFERENCE.md" "Quick Reference"
check_file "test-api.sh" "API Test Script"
echo ""

echo "Checking Configuration..."
if grep -q "jwt.secret" src/main/resources/application.properties; then
    echo -e "${GREEN}✓${NC} JWT configuration present"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗${NC} JWT configuration missing"
    ((CHECKS_FAILED++))
fi

if grep -q "cors.allowed-origins" src/main/resources/application.properties; then
    echo -e "${GREEN}✓${NC} CORS configuration present"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗${NC} CORS configuration missing"
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
    echo "2. Test the API: ./test-api.sh"
    echo "3. Read documentation: cat REST_API_IMPLEMENTATION.md"
    exit 0
else
    echo -e "${RED}✗ Verification failed!${NC}"
    echo "Please check the missing files or failed checks above."
    exit 1
fi
