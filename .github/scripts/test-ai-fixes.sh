#!/bin/bash
# Test script for AI fix utilities
# This creates sample error scenarios and tests the fix logic

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test directory
TEST_DIR="test_ai_fixes"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

log_test() {
    echo -e "${BLUE}[TEST]${NC} $1"
}

log_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
}

log_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
}

# Setup test environment
setup_test_env() {
    log_test "Setting up test environment..."
    
    rm -rf "$TEST_DIR"
    mkdir -p "$TEST_DIR"
    cd "$TEST_DIR"
    
    # Create sample error log
    cat > sample_errors.log << 'EOF'
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile (default-compile) on project student-support-system: Compilation failure
[ERROR] /path/backend/src/test/java/be/ap/student/tickets/TicketControllerIT.java:[15,47] package org.springframework.boot.test.web.client does not exist
[ERROR] /path/backend/src/test/java/be/ap/student/tickets/TicketControllerIT.java:[25,5] cannot find symbol
[ERROR]   symbol:   class TestRestTemplate
npm ERR! code ERESOLVE
npm ERR! ERESOLVE unable to resolve dependency tree
npm ERR! peer dep missing: eslint@^8.0.0, required by @typescript-eslint/parser@6.0.0
npm ERR! ERESOLVE cannot find module '@testing-library/dom'
FAIL frontend/src/ui/TicketForm.test.tsx
  ● Test suite failed to run
    Cannot find module '@testing-library/dom'
    at resolver (/path/frontend/src/ui/TicketForm.test.tsx:2:31)
Module '@testing-library/dom' does not exist
screen and fireEvent from testing-library causing issues
EOF

    log_pass "Test environment created"
}

# Test error extraction
test_error_extraction() {
    log_test "Testing error extraction..."
    
    # Test the helper script
    ../ai-fix-utils.sh extract-errors sample_errors.log extracted_errors.md
    
    if [ -f "extracted_errors.md" ] && [ -s "extracted_errors.md" ]; then
        if grep -q "Maven Compilation Errors" extracted_errors.md && grep -q "NPM/Frontend Errors" extracted_errors.md; then
            log_pass "Error extraction works correctly"
            return 0
        else
            log_fail "Error extraction missing expected sections"
            return 1
        fi
    else
        log_fail "Error extraction failed to create output file"
        return 1
    fi
}

# Test fix suggestions
test_fix_suggestions() {
    log_test "Testing fix suggestions generation..."
    
    ../ai-fix-utils.sh generate-fixes extracted_errors.md suggestions.md
    
    if [ -f "suggestions.md" ] && [ -s "suggestions.md" ]; then
        if grep -q "Spring Boot 4.x TestRestTemplate Fix" suggestions.md && grep -q "React Testing Library v16+ Fix" suggestions.md; then
            log_pass "Fix suggestions generated correctly"
            return 0
        else
            log_fail "Fix suggestions missing expected content"
            return 1
        fi
    else
        log_fail "Fix suggestions generation failed"
        return 1
    fi
}

# Test Spring Boot fixes (mock)
test_spring_boot_fixes() {
    log_test "Testing Spring Boot fix logic..."
    
    # Create mock Java file with old imports
    mkdir -p backend/src/test/java/be/ap/student/tickets
    cat > backend/src/test/java/be/ap/student/tickets/TestFile.java << 'EOF'
package be.ap.student.tickets;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplateBuilder;

@SpringBootTest
public class TestFile {
    private TestRestTemplate restTemplate;
}
EOF

    # Source the utilities and test Spring Boot fixes
    source ../ai-fix-utils.sh
    
    if apply_spring_boot_fixes sample_errors.log; then
        if grep -q "org.springframework.boot.resttestclient.TestRestTemplate" backend/src/test/java/be/ap/student/tickets/TestFile.java; then
            log_pass "Spring Boot imports updated correctly"
            return 0
        else
            log_fail "Spring Boot imports not updated"
            return 1
        fi
    else
        log_fail "Spring Boot fix logic failed"
        return 1
    fi
}

# Test React Testing Library fixes (mock)
test_react_testing_fixes() {
    log_test "Testing React Testing Library fix logic..."
    
    # Create mock frontend structure
    mkdir -p frontend/src/ui
    cat > frontend/src/ui/TestComponent.test.tsx << 'EOF'
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import TestComponent from './TestComponent';

test('should render component', () => {
  render(<TestComponent />);
  fireEvent.click(screen.getByText('Click me'));
});
EOF

    # Create mock package.json
    cat > frontend/package.json << 'EOF'
{
  "name": "test-frontend",
  "devDependencies": {
    "@testing-library/react": "^16.0.0"
  }
}
EOF

    # Source the utilities and test React fixes
    source ../ai-fix-utils.sh
    
    # Mock npm install (just create the directory) - simulate successful install
    mkdir -p frontend/node_modules/@testing-library/dom
    
    # Apply the fixes
    if apply_react_testing_fixes sample_errors.log; then
        # Check if imports were updated correctly (handle both single and double quotes)
        if (grep -q "import { render, screen } from ['\"]@testing-library/react['\"]" frontend/src/ui/TestComponent.test.tsx &&
           grep -q "import { fireEvent } from ['\"]@testing-library/dom['\"]" frontend/src/ui/TestComponent.test.tsx) ||
           (grep -q 'import { render, screen } from "@testing-library/react"' frontend/src/ui/TestComponent.test.tsx &&
           grep -q 'import { fireEvent } from "@testing-library/dom"' frontend/src/ui/TestComponent.test.tsx); then
            log_pass "React Testing Library imports updated correctly"
            return 0
        else
            log_fail "React Testing Library imports not updated as expected"
            echo "Expected: separate imports for render/screen and fireEvent"
            echo "Actual content:"
            cat frontend/src/ui/TestComponent.test.tsx
            return 1
        fi
    else
        log_fail "React Testing Library fix logic failed to execute"
        return 1
    fi
}

# Cleanup
cleanup() {
    log_test "Cleaning up test environment..."
    cd ..
    rm -rf "$TEST_DIR"
    log_pass "Cleanup completed"
}

# Main test runner
run_tests() {
    log_test "Starting AI fix utilities test suite..."
    
    # Change to the directory containing the script
    cd "$(dirname "${BASH_SOURCE[0]}")"
    
    # Make sure the ai-fix-utils.sh script exists
    if [ ! -f "ai-fix-utils.sh" ]; then
        log_fail "ai-fix-utils.sh not found in current directory"
        exit 1
    fi
    
    local test_count=0
    local pass_count=0
    
    # Run tests
    tests=(
        "setup_test_env"
        "test_error_extraction" 
        "test_fix_suggestions"
        "test_spring_boot_fixes"
        "test_react_testing_fixes"
    )
    
    for test in "${tests[@]}"; do
        test_count=$((test_count + 1))
        if $test; then
            pass_count=$((pass_count + 1))
        fi
    done
    
    cleanup
    
    # Print summary
    echo ""
    echo "================================="
    echo -e "${BLUE}Test Summary${NC}"
    echo "================================="
    echo -e "Total tests: ${test_count}"
    echo -e "Passed: ${GREEN}${pass_count}${NC}"
    echo -e "Failed: ${RED}$((test_count - pass_count))${NC}"
    
    if [ $pass_count -eq $test_count ]; then
        echo -e "${GREEN}All tests passed!${NC}"
        exit 0
    else
        echo -e "${RED}Some tests failed.${NC}"
        exit 1
    fi
}

# Run tests if script is executed directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    run_tests
fi