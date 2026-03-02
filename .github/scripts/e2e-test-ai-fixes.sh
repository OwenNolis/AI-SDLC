#!/bin/bash
# End-to-end test for AI Code Fixes workflow
# This creates real errors and tests the complete fixing pipeline

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[E2E-TEST]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[E2E-SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[E2E-WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[E2E-ERROR]${NC} $1"
}

# Test directory
TEST_DIR="e2e_ai_fixes"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Backup original files
backup_files() {
    log_info "Creating backups of original files..."
    
    mkdir -p backups
    
    # Backup backend test files if they exist
    if [ -f "../../backend/src/test/java/be/ap/student/tickets/TicketControllerIT.java" ]; then
        cp "../../backend/src/test/java/be/ap/student/tickets/TicketControllerIT.java" backups/
        log_info "Backed up TicketControllerIT.java"
    fi
    
    # Backup frontend test files if they exist
    if [ -f "../../frontend/src/ui/TicketForm.test.tsx" ]; then
        cp "../../frontend/src/ui/TicketForm.test.tsx" backups/
        log_info "Backed up TicketForm.test.tsx"
    fi
    
    # Backup frontend package.json
    if [ -f "../../frontend/package.json" ]; then
        cp "../../frontend/package.json" backups/
        log_info "Backed up frontend package.json"
    fi
}

# Introduce intentional errors
introduce_spring_boot_errors() {
    log_info "Introducing Spring Boot TestRestTemplate errors..."
    
    # Create or modify integration test with old imports
    mkdir -p "../../backend/src/test/java/be/ap/student/tickets"
    cat > "../../backend/src/test/java/be/ap/student/tickets/TicketControllerIT.java" << 'EOF'
package be.ap.student.tickets;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TicketControllerIT {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testCreateTicket() {
        // This will fail due to old TestRestTemplate import
        String result = restTemplate.getForObject("/api/tickets", String.class);
        // Test implementation
    }
}
EOF
    
    log_success "Spring Boot errors introduced"
}

introduce_react_testing_errors() {
    log_info "Introducing React Testing Library errors..."
    
    # Create or modify React test with old imports
    mkdir -p "../../frontend/src/ui"
    cat > "../../frontend/src/ui/TicketForm.test.tsx" << 'EOF'
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import TicketForm from './TicketForm';

describe('TicketForm', () => {
  test('renders form fields', () => {
    render(<TicketForm />);
    
    expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
  });
  
  test('handles form submission', () => {
    const mockSubmit = jest.fn();
    render(<TicketForm onSubmit={mockSubmit} />);
    
    // This will fail due to import issues in newer versions
    fireEvent.click(screen.getByText(/submit/i));
    
    expect(mockSubmit).toHaveBeenCalled();
  });
});
EOF
    
    log_success "React Testing Library errors introduced"
}

introduce_npm_dependency_errors() {
    log_info "Introducing npm dependency conflicts..."
    
    # Modify package.json to create dependency conflicts
    if [ -f "../../frontend/package.json" ]; then
        # Create a version that will cause ERESOLVE conflicts
        cat > "../../frontend/package.json" << 'EOF'
{
  "name": "ai-sdlc-frontend",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "lint": "eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "preview": "vite preview",
    "test": "jest"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1"
  },
  "devDependencies": {
    "@testing-library/react": "^16.0.1",
    "@types/react": "^18.3.3",
    "@types/react-dom": "^18.3.0",
    "@typescript-eslint/eslint-plugin": "^7.15.0",
    "@typescript-eslint/parser": "^7.15.0",
    "@vitejs/plugin-react": "^4.3.1",
    "eslint": "^7.32.0",
    "eslint-plugin-react-hooks": "^4.6.2",
    "eslint-plugin-react-refresh": "^0.4.7",
    "jest": "^29.7.0",
    "jest-environment-jsdom": "^29.7.0",
    "typescript": "^5.2.2",
    "vite": "^5.3.4"
  }
}
EOF
        log_success "npm dependency conflicts introduced (ESLint version mismatch)"
    fi
}

# Run the SDLC flow and capture errors
run_sdlc_with_errors() {
    log_info "Running SDLC flow to capture errors..."
    
    cd ../..
    
    set +e  # Don't exit on error
    ./ai/flow.sh feature-001-support-ticket 2>&1 | tee .github/scripts/e2e_errors.log
    exit_code=$?
    set -e
    
    cd .github/scripts
    
    if [ $exit_code -ne 0 ]; then
        log_success "SDLC flow failed as expected (exit code: $exit_code)"
        return 0
    else
        log_warning "SDLC flow unexpectedly passed"
        return 1
    fi
}

# Test AI error analysis
test_error_analysis() {
    log_info "Testing AI error analysis..."
    
    if [ ! -f "e2e_errors.log" ]; then
        log_error "Error log not found"
        return 1
    fi
    
    # Use our AI utilities to analyze errors
    ./ai-fix-utils.sh extract-errors e2e_errors.log e2e_error_analysis.md
    ./ai-fix-utils.sh generate-fixes e2e_error_analysis.md e2e_suggestions.md
    
    # Check if analysis found the expected errors
    if grep -q "Spring Boot\|TestRestTemplate\|Maven" e2e_error_analysis.md; then
        log_success "Spring Boot errors detected"
    else
        log_warning "Spring Boot errors not detected"
    fi
    
    if grep -q "testing-library\|React\|npm" e2e_error_analysis.md; then
        log_success "React Testing Library errors detected"
    else
        log_warning "React Testing Library errors not detected"
    fi
    
    if [ -s "e2e_suggestions.md" ]; then
        log_success "AI suggestions generated"
    else
        log_error "AI suggestions not generated"
        return 1
    fi
    
    return 0
}

# Test automated fixes
test_automated_fixes() {
    log_info "Testing automated fixes..."
    
    # Apply fixes
    source ./ai-fix-utils.sh
    
    if apply_all_fixes e2e_errors.log; then
        log_success "Automated fixes applied"
    else
        log_warning "No automated fixes applied"
        return 1
    fi
    
    # Check if files were actually modified
    local fixes_verified=true
    
    # Check Spring Boot fixes
    if [ -f "../../backend/src/test/java/be/ap/student/tickets/TicketControllerIT.java" ]; then
        if grep -q "org.springframework.boot.resttestclient.TestRestTemplate" "../../backend/src/test/java/be/ap/student/tickets/TicketControllerIT.java"; then
            log_success "Spring Boot imports fixed"
        else
            log_warning "Spring Boot imports not fixed"
            fixes_verified=false
        fi
    fi
    
    # Check React Testing Library fixes
    if [ -f "../../frontend/src/ui/TicketForm.test.tsx" ]; then
        if grep -q 'import { render, screen } from "@testing-library/react"' "../../frontend/src/ui/TicketForm.test.tsx" &&
           grep -q 'import { fireEvent } from "@testing-library/dom"' "../../frontend/src/ui/TicketForm.test.tsx"; then
            log_success "React Testing Library imports fixed"
        else
            log_warning "React Testing Library imports not fixed"
            fixes_verified=false
        fi
    fi
    
    if [ "$fixes_verified" = true ]; then
        return 0
    else
        return 1
    fi
}

# Test fixes by running SDLC flow again
test_fixes_verification() {
    log_info "Verifying fixes by re-running SDLC flow..."
    
    cd ../..
    
    set +e
    ./ai/flow.sh feature-001-support-ticket 2>&1 | tee .github/scripts/e2e_fixed.log
    exit_code=$?
    set -e
    
    cd .github/scripts
    
    if [ $exit_code -eq 0 ]; then
        log_success "SDLC flow now passes! Fixes verified successfully."
        return 0
    else
        log_warning "SDLC flow still failing after fixes (exit code: $exit_code)"
        log_info "Check e2e_fixed.log for remaining issues"
        return 1
    fi
}

# Restore original files
restore_files() {
    log_info "Restoring original files..."
    
    if [ -d "backups" ]; then
        # Restore backend files
        if [ -f "backups/TicketControllerIT.java" ]; then
            cp "backups/TicketControllerIT.java" "../../backend/src/test/java/be/ap/student/tickets/"
            log_info "Restored TicketControllerIT.java"
        fi
        
        # Restore frontend files
        if [ -f "backups/TicketForm.test.tsx" ]; then
            cp "backups/TicketForm.test.tsx" "../../frontend/src/ui/"
            log_info "Restored TicketForm.test.tsx"
        fi
        
        if [ -f "backups/package.json" ]; then
            cp "backups/package.json" "../../frontend/"
            log_info "Restored frontend package.json"
        fi
        
        rm -rf backups
        log_success "All files restored"
    fi
    
    # Clean up test artifacts
    rm -f e2e_errors.log e2e_fixed.log e2e_error_analysis.md e2e_suggestions.md
}

# Main test runner
run_e2e_test() {
    log_info "Starting End-to-End AI Code Fixes test..."
    
    cd "$(dirname "${BASH_SOURCE[0]}")"
    
    local test_count=0
    local pass_count=0
    
    # Test phases
    phases=(
        "backup_files"
        "introduce_spring_boot_errors"
        "introduce_react_testing_errors" 
        "introduce_npm_dependency_errors"
        "run_sdlc_with_errors"
        "test_error_analysis"
        "test_automated_fixes"
        "test_fixes_verification"
    )
    
    for phase in "${phases[@]}"; do
        test_count=$((test_count + 1))
        log_info "Running phase: $phase"
        
        if $phase; then
            pass_count=$((pass_count + 1))
            log_success "Phase $phase completed successfully"
        else
            log_error "Phase $phase failed"
        fi
    done
    
    # Always restore files
    restore_files
    
    # Print summary
    echo ""
    echo "============================================="
    echo -e "${BLUE}End-to-End Test Summary${NC}"
    echo "============================================="
    echo -e "Total phases: ${test_count}"
    echo -e "Passed: ${GREEN}${pass_count}${NC}"
    echo -e "Failed: ${RED}$((test_count - pass_count))${NC}"
    
    if [ $pass_count -eq $test_count ]; then
        echo -e "${GREEN}🎉 All E2E tests passed! AI Code Fixes workflow is fully functional.${NC}"
        exit 0
    else
        echo -e "${RED}❌ Some E2E tests failed. Review the output above.${NC}"
        exit 1
    fi
}

# Run E2E test if script is executed directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    run_e2e_test
fi