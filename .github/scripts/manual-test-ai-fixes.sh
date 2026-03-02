#!/bin/bash
# Manual testing and debugging script for AI Code Fixes
# Allows testing individual components and creating controlled scenarios

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[MANUAL-TEST]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Show available commands
show_help() {
    echo "AI Code Fixes Manual Testing Script"
    echo ""
    echo "Usage: $0 <command> [options]"
    echo ""
    echo "Commands:"
    echo "  create-spring-error    - Create intentional Spring Boot TestRestTemplate error"
    echo "  create-react-error     - Create intentional React Testing Library error"
    echo "  create-npm-error       - Create intentional npm dependency conflict"
    echo "  analyze-errors <file>  - Analyze errors from a log file"
    echo "  generate-fixes <file>  - Generate fix suggestions from error analysis"
    echo "  apply-fixes <file>     - Apply automated fixes based on error log"
    echo "  test-workflow-syntax   - Validate GitHub Actions workflow syntax"
    echo "  simulate-ai-fixes      - Run a complete simulation without real errors"
    echo "  check-dependencies     - Check if all required dependencies are available"
    echo "  validate-scripts       - Validate all AI fixing scripts"
    echo "  demo                   - Run a complete demo of the AI fixing process"
    echo ""
    echo "Examples:"
    echo "  $0 demo                                    # Run complete demo"
    echo "  $0 create-spring-error                     # Create Spring Boot error"
    echo "  $0 analyze-errors flow_output.log         # Analyze errors from log"
    echo "  $0 apply-fixes flow_output.log            # Apply fixes based on log"
}

# Create Spring Boot error
create_spring_error() {
    log_info "Creating Spring Boot TestRestTemplate error..."
    
    mkdir -p ../../backend/src/test/java/be/ap/student/tickets
    cat > ../../backend/src/test/java/be/ap/student/tickets/TestControllerIT.java << 'EOF'
package be.ap.student.tickets;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestControllerIT {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testEndpoint() {
        String result = restTemplate.getForObject("/api/test", String.class);
        // This will fail due to old TestRestTemplate import package
    }
}
EOF
    
    log_success "Spring Boot error created in backend/src/test/java/be/ap/student/tickets/TestControllerIT.java"
    log_info "This file uses the old TestRestTemplate import that was moved in Spring Boot 4.x"
}

# Create React Testing Library error
create_react_error() {
    log_info "Creating React Testing Library error..."
    
    mkdir -p ../../frontend/src/ui
    cat > ../../frontend/src/ui/TestComponent.test.tsx << 'EOF'
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

const TestComponent = () => <button>Click me</button>;

describe('TestComponent', () => {
  test('handles click', () => {
    render(<TestComponent />);
    
    // This will fail in React Testing Library v16+ due to import changes
    fireEvent.click(screen.getByText('Click me'));
  });
});
EOF
    
    log_success "React Testing Library error created in frontend/src/ui/TestComponent.test.tsx"
    log_info "This file uses the old combined import that doesn't work in v16+"
}

# Create npm dependency error
create_npm_error() {
    log_info "Creating npm dependency conflict..."
    
    if [ ! -f "../../frontend/package.json.backup" ]; then
        cp ../../frontend/package.json ../../frontend/package.json.backup
        log_info "Backed up original package.json"
    fi
    
    # Add conflicting ESLint versions
    cd ../../frontend
    npm install eslint@7.32.0 --save-dev --legacy-peer-deps 2>/dev/null || true
    cd - >/dev/null
    
    log_success "npm dependency conflict created (ESLint version mismatch)"
    log_info "This will cause ERESOLVE conflicts with @typescript-eslint packages"
}

# Analyze errors from a log file
analyze_errors() {
    local log_file="$1"
    
    if [ -z "$log_file" ]; then
        log_error "Please provide a log file to analyze"
        echo "Usage: $0 analyze-errors <log_file>"
        return 1
    fi
    
    # Handle relative paths from project root
    if [ ! -f "$log_file" ] && [ -f "../../$log_file" ]; then
        log_file="../../$log_file"
    fi
    
    if [ ! -f "$log_file" ]; then
        log_error "Log file not found: $log_file"
        log_info "Current directory: $(pwd)"
        log_info "Looking for: $log_file"
        return 1
    fi
    
    log_info "Analyzing errors from $log_file..."
    
    ./ai-fix-utils.sh extract-errors "$log_file" "manual_error_analysis.md"
    
    if [ -s "manual_error_analysis.md" ]; then
        log_success "Error analysis completed"
        echo ""
        echo "=== ERROR ANALYSIS ==="
        cat manual_error_analysis.md
        echo ""
        echo "Analysis saved to: $(pwd)/manual_error_analysis.md"
    else
        log_warning "No errors found or analysis failed"
    fi
}

# Generate fix suggestions
generate_fixes() {
    local error_file="$1"
    
    if [ -z "$error_file" ]; then
        if [ -f "manual_error_analysis.md" ]; then
            error_file="manual_error_analysis.md"
            log_info "Using existing error analysis file"
        else
            log_error "Please provide an error analysis file"
            echo "Usage: $0 generate-fixes <error_file>"
            return 1
        fi
    fi
    
    if [ ! -f "$error_file" ]; then
        log_error "Error file not found: $error_file"
        return 1
    fi
    
    log_info "Generating fix suggestions from $error_file..."
    
    ./ai-fix-utils.sh generate-fixes "$error_file" "manual_suggestions.md"
    
    if [ -s "manual_suggestions.md" ]; then
        log_success "Fix suggestions generated"
        echo ""
        echo "=== FIX SUGGESTIONS ==="
        cat manual_suggestions.md
        echo ""
        echo "Suggestions saved to: manual_suggestions.md"
    else
        log_warning "No fix suggestions generated"
    fi
}

# Apply automated fixes
apply_fixes() {
    local log_file="$1"
    
    if [ -z "$log_file" ]; then
        log_error "Please provide a log file to analyze for fixes"
        echo "Usage: $0 apply-fixes <log_file>"
        return 1
    fi
    
    # Handle relative paths from project root  
    if [ ! -f "$log_file" ] && [ -f "../../$log_file" ]; then
        log_file="../../$log_file"
    fi
    
    if [ ! -f "$log_file" ]; then
        log_error "Log file not found: $log_file"
        log_info "Current directory: $(pwd)"
        log_info "Looking for: $log_file"
        return 1
    fi
    
    log_info "Applying automated fixes based on $log_file..."
    
    # Change to project root for fixes to work correctly
    cd ../../
    
    source .github/scripts/ai-fix-utils.sh
    
    if apply_all_fixes "$log_file"; then
        log_success "Automated fixes applied successfully"
        echo ""
        echo "Files that may have been modified:"
        echo "- backend/src/test/**/*.java (Spring Boot fixes)"
        echo "- frontend/src/**/*.test.tsx (React Testing Library fixes)"
        echo "- frontend/package.json and node_modules (npm fixes)"
        echo ""
        echo "Check git status to see actual changes:"
        git status --porcelain 2>/dev/null || echo "No git repository or no changes"
    else
        log_warning "No automated fixes were applied"
        echo "This could mean:"
        echo "- No errors matching our fix patterns were found"
        echo "- The errors require manual intervention"
        echo "- The fix logic needs to be updated"
    fi
    
    # Return to scripts directory
    cd .github/scripts/
}

# Validate workflow syntax
test_workflow_syntax() {
    log_info "Validating GitHub Actions workflow syntax..."
    
    local workflow_file="../workflows/ai-code-fixes.yml"
    
    if [ ! -f "$workflow_file" ]; then
        log_error "Workflow file not found: $workflow_file"
        return 1
    fi
    
    # Basic YAML syntax check
    if command -v python3 >/dev/null 2>&1; then
        if python3 -c "import yaml" >/dev/null 2>&1; then
            python3 -c "import yaml; yaml.safe_load(open('$workflow_file'))" 2>/dev/null
            if [ $? -eq 0 ]; then
                log_success "Workflow YAML syntax is valid"
            else
                log_error "Workflow YAML syntax is invalid"
                return 1
            fi
        else
            log_warning "PyYAML not available, using basic validation"
            # Basic check for common YAML issues
            if grep -q $'^\t' "$workflow_file"; then
                log_error "Found tabs in YAML file (should use spaces)"
                return 1
            fi
            log_success "Basic YAML validation passed"
        fi
    else
        log_warning "Python3 not available, skipping YAML validation"
    fi
    
    # Check for common GitHub Actions issues
    if grep -q "@v6" "$workflow_file"; then
        log_warning "Found @v6 action versions (may not exist)"
    fi
    
    if grep -q "ai/\*/package-lock.json" "$workflow_file"; then
        log_warning "Found glob pattern in cache paths (may cause issues)"
    fi
    
    log_success "Workflow validation completed"
}

# Simulate the complete AI fixes process
simulate_ai_fixes() {
    log_info "Running AI Fixes simulation..."
    
    # Create a sample error log
    cat > simulation_errors.log << 'EOF'
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile
[ERROR] /backend/src/test/java/TicketControllerIT.java:[15,47] package org.springframework.boot.test.web.client does not exist
[ERROR] symbol: class TestRestTemplate
npm ERR! code ERESOLVE
npm ERR! ERESOLVE unable to resolve dependency tree
npm ERR! peer dep missing: eslint@^8.0.0, required by @typescript-eslint/parser@6.0.0
FAIL frontend/src/ui/TicketForm.test.tsx
  ● Test suite failed to run
    Cannot find module '@testing-library/dom'
    screen and fireEvent import issues
EOF
    
    log_success "Sample error log created"
    
    # Test each phase
    phases=(
        "analyze_errors simulation_errors.log"
        "generate_fixes manual_error_analysis.md"
    )
    
    for phase in "${phases[@]}"; do
        log_info "Simulating: $phase"
        if $phase; then
            log_success "✅ $phase completed"
        else
            log_error "❌ $phase failed"
        fi
        echo ""
    done
    
    # Cleanup
    rm -f simulation_errors.log manual_error_analysis.md manual_suggestions.md
    
    log_success "Simulation completed successfully!"
}

# Check dependencies
check_dependencies() {
    log_info "Checking AI Fixes dependencies..."
    
    local all_good=true
    
    # Check required scripts
    if [ -f "./ai-fix-utils.sh" ]; then
        log_success "✅ ai-fix-utils.sh found"
    else
        log_error "❌ ai-fix-utils.sh not found"
        all_good=false
    fi
    
    # Check workflow file
    if [ -f "../workflows/ai-code-fixes.yml" ]; then
        log_success "✅ ai-code-fixes.yml found"
    else
        log_error "❌ ai-code-fixes.yml not found"
        all_good=false
    fi
    
    # Check GitHub CLI
    if command -v gh >/dev/null 2>&1; then
        log_success "✅ GitHub CLI available"
        gh_version=$(gh --version | head -1)
        log_info "  Version: $gh_version"
    else
        log_warning "⚠️ GitHub CLI not available (workflow will use fallback)"
    fi
    
    # Check project structure
    if [ -d "../../backend" ]; then
        log_success "✅ Backend directory found"
    else
        log_warning "⚠️ Backend directory not found"
    fi
    
    if [ -d "../../frontend" ]; then
        log_success "✅ Frontend directory found"
    else
        log_warning "⚠️ Frontend directory not found"
    fi
    
    if [ -f "../../ai/flow.sh" ]; then
        log_success "✅ AI flow script found"
    else
        log_error "❌ AI flow script not found"
        all_good=false
    fi
    
    if [ "$all_good" = true ]; then
        log_success "🎉 All dependencies check passed!"
        return 0
    else
        log_error "❌ Some dependencies are missing"
        return 1
    fi
}

# Validate all scripts
validate_scripts() {
    log_info "Validating AI Fixes scripts..."
    
    local all_valid=true
    
    # Test ai-fix-utils.sh
    if ./test-ai-fixes.sh >/dev/null 2>&1; then
        log_success "✅ ai-fix-utils.sh passes all tests"
    else
        log_error "❌ ai-fix-utils.sh has test failures"
        all_valid=false
    fi
    
    # Check script syntax
    bash -n ./ai-fix-utils.sh 2>/dev/null
    if [ $? -eq 0 ]; then
        log_success "✅ ai-fix-utils.sh syntax is valid"
    else
        log_error "❌ ai-fix-utils.sh has syntax errors"
        all_valid=false
    fi
    
    # Validate workflow
    test_workflow_syntax
    if [ $? -eq 0 ]; then
        log_success "✅ Workflow file is valid"
    else
        log_error "❌ Workflow file has issues"
        all_valid=false
    fi
    
    if [ "$all_valid" = true ]; then
        log_success "🎉 All scripts are valid!"
        return 0
    else
        log_error "❌ Some scripts have issues"
        return 1
    fi
}

# Run complete demo
run_demo() {
    log_info "Running complete AI Code Fixes demo..."
    
    echo ""
    echo "======================================"
    echo "🤖 AI Code Fixes Demo"
    echo "======================================"
    echo ""
    
    log_info "Phase 1: Dependency Check"
    check_dependencies
    echo ""
    
    log_info "Phase 2: Script Validation"
    validate_scripts
    echo ""
    
    log_info "Phase 3: Workflow Syntax Check"
    test_workflow_syntax
    echo ""
    
    log_info "Phase 4: AI Processing Simulation"
    simulate_ai_fixes
    echo ""
    
    log_success "🎉 Demo completed successfully!"
    echo ""
    echo "The AI Code Fixes feature is ready to use!"
    echo ""
    echo "Next steps:"
    echo "1. Commit the new workflow and scripts to your repository"
    echo "2. Push to trigger the workflow on errors"
    echo "3. Or manually trigger: gh workflow run 'AI Code Fixes'"
    echo "4. For testing: create intentional errors and test locally"
}

# Main command dispatcher
main() {
    cd "$(dirname "${BASH_SOURCE[0]}")"
    
    case "${1:-help}" in
        "create-spring-error")
            create_spring_error
            ;;
        "create-react-error")
            create_react_error
            ;;
        "create-npm-error")
            create_npm_error
            ;;
        "analyze-errors")
            analyze_errors "$2"
            ;;
        "generate-fixes")
            generate_fixes "$2"
            ;;
        "apply-fixes")
            apply_fixes "$2"
            ;;
        "test-workflow-syntax")
            test_workflow_syntax
            ;;
        "simulate-ai-fixes")
            simulate_ai_fixes
            ;;
        "check-dependencies")
            check_dependencies
            ;;
        "validate-scripts")
            validate_scripts
            ;;
        "demo")
            run_demo
            ;;
        "help"|*)
            show_help
            ;;
    esac
}

# Run main function with all arguments
main "$@"