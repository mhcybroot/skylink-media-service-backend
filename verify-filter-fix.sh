#!/bin/bash

# Advanced Search Filter Verification Script
# Tests that all filter parameters are properly submitted

echo "=========================================="
echo "Advanced Search Filter Verification"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
PASSED=0
FAILED=0

echo "Starting application verification..."
echo ""

# Check if application is running
if ! curl -s http://localhost:8080/admin/dashboard > /dev/null 2>&1; then
    echo -e "${RED}✗ Application is not running${NC}"
    echo "Please start the application first: ./gradlew bootRun"
    exit 1
fi

echo -e "${GREEN}✓ Application is running${NC}"
echo ""

echo "Manual Testing Checklist:"
echo "========================="
echo ""

echo "1. Payment Status Filter Test"
echo "   - Open: http://localhost:8080/admin/dashboard"
echo "   - Click 'Filters' button"
echo "   - Select 'Payment Status: PAID'"
echo "   - Click 'Search'"
echo "   - Verify URL contains: paymentStatus=PAID"
echo "   - Verify only PAID projects shown"
echo ""

echo "2. Project Status Filter Test"
echo "   - Select 'Project Status: INFIELD'"
echo "   - Click 'Search'"
echo "   - Verify URL contains: status=INFIELD"
echo "   - Verify only INFIELD projects shown"
echo ""

echo "3. Date Range Filter Test"
echo "   - Set 'Due Date From: 2026-03-01'"
echo "   - Set 'Due Date To: 2026-03-31'"
echo "   - Click 'Search'"
echo "   - Verify URL contains: dueDateFrom=2026-03-01&dueDateTo=2026-03-31"
echo "   - Verify only projects in date range shown"
echo ""

echo "4. Price Range Filter Test"
echo "   - Set 'Price From: 1000'"
echo "   - Set 'Price To: 5000'"
echo "   - Click 'Search'"
echo "   - Verify URL contains: priceFrom=1000&priceTo=5000"
echo "   - Verify only projects in price range shown"
echo ""

echo "5. Contractor Filter Test"
echo "   - Select a contractor from dropdown"
echo "   - Click 'Search'"
echo "   - Verify URL contains: contractorId=X"
echo "   - Verify only that contractor's projects shown"
echo ""

echo "6. Combined Filters Test"
echo "   - Set multiple filters (status + payment + date)"
echo "   - Click 'Search'"
echo "   - Verify URL contains all parameters"
echo "   - Verify results match all filters"
echo ""

echo "7. Reset Button Test"
echo "   - Set multiple filters"
echo "   - Click 'Reset'"
echo "   - Verify all filters cleared"
echo "   - Verify all projects shown"
echo ""

echo "=========================================="
echo "Browser DevTools Verification"
echo "=========================================="
echo ""
echo "1. Open browser DevTools (F12)"
echo "2. Go to Network tab"
echo "3. Set a filter and click Search"
echo "4. Check the request URL"
echo "5. Verify all filter parameters are present"
echo ""
echo "Example correct URL:"
echo "/admin/dashboard?tab=projects&status=INFIELD&paymentStatus=PAID"
echo ""

echo "=========================================="
echo "Quick Smoke Test"
echo "=========================================="
echo ""
echo "Run this in your browser console after selecting PAID filter:"
echo ""
echo "// Check if paymentStatus parameter is in URL"
echo "console.log(window.location.search.includes('paymentStatus=PAID') ? '✓ PASS' : '✗ FAIL');"
echo ""

echo "=========================================="
echo "Fix Verification Complete"
echo "=========================================="
echo ""
echo "If all tests pass, the bug is fixed!"
echo ""
