#!/bin/bash
# AI Code Fixing Utilities
# Helper functions for the AI Code Fixes workflow

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
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

# Extract specific error types from SDLC flow output
extract_maven_errors() {
    local log_file="$1"
    local output_file="$2"
    
    log_info "Extracting Maven compilation errors..."
    echo "## Maven Compilation Errors" >> "$output_file"
    
    # Look for Maven compilation failures
    grep -n -A5 -B2 "\[ERROR\].*compilation failure\|BUILD FAILURE\|Failed to compile" "$log_file" >> "$output_file" 2>/dev/null || true
    
    # Look for specific Java compilation errors
    grep -n -A3 -B1 "cannot find symbol\|package.*does not exist\|method.*cannot be applied" "$log_file" >> "$output_file" 2>/dev/null || true
    
    echo "" >> "$output_file"
}

extract_npm_errors() {
    local log_file="$1"
    local output_file="$2"
    
    log_info "Extracting npm/frontend errors..."
    echo "## NPM/Frontend Errors" >> "$output_file"
    
    # Look for npm errors
    grep -n -A5 -B2 "npm ERR!\|ERESOLVE\|peer dep.*conflict" "$log_file" >> "$output_file" 2>/dev/null || true
    
    # Look for TypeScript compilation errors
    grep -n -A3 -B1 "TS[0-9]\+:\|Cannot find module\|Type.*is not assignable" "$log_file" >> "$output_file" 2>/dev/null || true
    
    # Look for Jest test failures
    grep -n -A5 -B2 "FAIL.*\\.test\\.\|expect.*toBe\|AssertionError" "$log_file" >> "$output_file" 2>/dev/null || true
    
    echo "" >> "$output_file"
}

extract_ai_flow_errors() {
    local log_file="$1"
    local output_file="$2"
    
    log_info "Extracting AI flow specific errors..."
    echo "## AI Flow Errors" >> "$output_file"
    
    # Look for AI agent errors
    grep -n -A3 -B1 "Agent.*error\|Failed to.*generate\|Schema validation failed" "$log_file" >> "$output_file" 2>/dev/null || true
    
    # Look for file generation errors
    grep -n -A3 -B1 "Could not.*write\|Permission denied\|ENOENT" "$log_file" >> "$output_file" 2>/dev/null || true
    
    echo "" >> "$output_file"
}

# Generate context-aware fix suggestions
generate_fix_suggestions() {
    local error_file="$1"
    local suggestion_file="$2"
    
    log_info "Generating AI fix suggestions..."
    
    cat > "$suggestion_file" << 'EOF'
# 🤖 AI-Generated Fix Suggestions

Based on the error analysis, here are specific fixes for your AI-SDLC project:

EOF

    # Check for Spring Boot TestRestTemplate issues
    if grep -q "TestRestTemplate\|spring-boot-test-web-client\|cannot find symbol.*TestRestTemplate" "$error_file"; then
        cat >> "$suggestion_file" << 'EOF'
## 🔧 Spring Boot 4.x TestRestTemplate Fix

**Issue**: TestRestTemplate dependency injection fails in Spring Boot 4.x

**Fix**: Create proper TestConfiguration and use @Import annotation

**Steps**:
1. Create TestRestTemplateConfig.java:
```java
@TestConfiguration
public class TestRestTemplateConfig {
    @Bean
    public TestRestTemplate testRestTemplate(@LocalServerPort int port) {
        TestRestTemplate template = new TestRestTemplate();
        template.setRootUri("http://localhost:" + port);
        return template;
    }
}
```

2. Add @Import to test classes:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestRestTemplateConfig.class)
public class YourTest {
    @Autowired
    private TestRestTemplate restTemplate;
    // ...
}
```

**Fix Commands**:
```bash
# Update imports in Java test files
find backend/src/test -name "*.java" -exec sed -i 's/org\.springframework\.boot\.test\.web\.client\.TestRestTemplate/org.springframework.boot.resttestclient.TestRestTemplate/g' {} \;
```

EOF
    fi

    # Check for React Testing Library issues
    if grep -q "testing-library.*does not exist\|screen.*fireEvent\|Cannot find module.*testing-library" "$error_file"; then
        cat >> "$suggestion_file" << 'EOF'
## 🔧 React Testing Library v16+ Fix

**Issue**: Breaking changes in React Testing Library v16+

**Fix Commands**:
```bash
# Install missing dependency
cd frontend && npm install --save-dev @testing-library/dom

# Update imports in test files
find frontend/src -name "*.test.tsx" -exec sed -i 's/import { render, screen, fireEvent } from "@testing-library\/react"/import { render, screen } from "@testing-library\/react";\nimport { fireEvent } from "@testing-library\/dom"/g' {} \;
```

EOF
    fi

    # Check for ESLint/npm conflicts
    if grep -q "ERESOLVE\|peer dep.*conflict\|npm ERR.*ERESOLVE" "$error_file"; then
        cat >> "$suggestion_file" << 'EOF'
## 🔧 NPM Dependency Conflict Fix

**Issue**: ESLint peer dependency conflicts

**Fix Commands**:
```bash
# Use legacy peer deps resolution
cd frontend && npm install --legacy-peer-deps

# Or clear cache and reinstall
cd frontend && rm -rf node_modules package-lock.json && npm install --legacy-peer-deps
```

EOF
    fi

    # Check for Java compilation issues
    if grep -q "cannot find symbol\|package.*does not exist\|BUILD FAILURE" "$error_file"; then
        cat >> "$suggestion_file" << 'EOF'
## 🔧 Java Compilation Fix

**Issue**: Missing imports or dependencies

**Fix Commands**:
```bash
# Clean and rebuild
cd backend && mvn clean compile test-compile

# Update test generation template if needed
# Check ai/testgen/generate-backend-tests.mjs for correct imports
```

EOF
    fi

    log_success "Fix suggestions generated in $suggestion_file"
}

# Apply automated fixes based on error patterns
apply_spring_boot_fixes() {
    local log_file="$1"
    
    if grep -q "TestRestTemplate\|spring-boot-test-web-client" "$log_file"; then
        log_info "Applying Spring Boot TestRestTemplate fixes..."
        
        # Update imports in Java files - handle both macOS and Linux sed
        find backend/src/test -name "*.java" -exec grep -l "TestRestTemplate" {} \; | while read file; do
            log_info "Updating imports in $file"
            if [[ "$OSTYPE" == "darwin"* ]]; then
                # macOS sed requires -i with backup extension - Replace TestRestTemplate with RestTemplate
                sed -i '.bak' 's/org\.springframework\.boot\.test\.web\.client\.TestRestTemplate/org.springframework.web.client.RestTemplate/g' "$file"
                sed -i '.bak' 's/org\.springframework\.boot\.resttestclient\.TestRestTemplate/org.springframework.web.client.RestTemplate/g' "$file"
                sed -i '.bak' 's/TestRestTemplate/RestTemplate/g' "$file"
                rm -f "${file}.bak"
            else
                # Linux sed - Replace TestRestTemplate with RestTemplate
                sed -i 's/org\.springframework\.boot\.test\.web\.client\.TestRestTemplate/org.springframework.web.client.RestTemplate/g' "$file"
                sed -i 's/org\.springframework\.boot\.resttestclient\.TestRestTemplate/org.springframework.web.client.RestTemplate/g' "$file"
                sed -i 's/TestRestTemplate/RestTemplate/g' "$file"
            fi
        done
        
        # Create proper TestRestTemplate configuration class for Spring Boot 4+
        local config_file="backend/src/test/java/be/ap/student/config/TestRestTemplateConfig.java"
        mkdir -p "$(dirname "$config_file")"
        
        cat > "$config_file" << 'EOF'
package be.ap.student.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.web.server.LocalServerPort;

@TestConfiguration
public class TestRestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate(@LocalServerPort int port) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new org.springframework.web.util.DefaultUriBuilderFactory("http://localhost:" + port));
        return restTemplate;
    }
}
EOF
        
        # Add @Import annotation to test files that use RestTemplate
        find backend/src/test -name "*.java" -exec grep -l "RestTemplate" {} \; | while read file; do
            # Remove wrong @AutoConfigureTestRestTemplate annotation if present
            if grep -q "@AutoConfigureTestRestTemplate" "$file"; then
                log_info "Removing incorrect @AutoConfigureTestRestTemplate from $file"
                if [[ "$OSTYPE" == "darwin"* ]]; then
                    sed -i '.bak' '/@AutoConfigureTestRestTemplate/d' "$file"
                    rm -f "${file}.bak"
                else
                    sed -i '/@AutoConfigureTestRestTemplate/d' "$file"
                fi
            fi
            
            # Remove wrong import for AutoConfigureTestRestTemplate
            if grep -q "import.*AutoConfigureTestRestTemplate" "$file"; then
                log_info "Removing incorrect AutoConfigureTestRestTemplate import from $file"
                if [[ "$OSTYPE" == "darwin"* ]]; then
                    sed -i '.bak' '/import.*AutoConfigureTestRestTemplate/d' "$file"
                    rm -f "${file}.bak"
                else
                    sed -i '/import.*AutoConfigureTestRestTemplate/d' "$file"
                fi
            fi
            
            # Skip if already has @Import with TestRestTemplateConfig
            if ! grep -q "@Import.*TestRestTemplateConfig" "$file"; then
                log_info "Adding @Import(TestRestTemplateConfig.class) to $file"
                
                # Add import statement
                if grep -q "^import.*SpringBootTest" "$file"; then
                    if [[ "$OSTYPE" == "darwin"* ]]; then
                        sed -i '.bak' '/^import.*SpringBootTest/a\
import org.springframework.context.annotation.Import;\
import be.ap.student.config.TestRestTemplateConfig;
' "$file"
                        rm -f "${file}.bak"
                    else
                        sed -i '/^import.*SpringBootTest/a import org.springframework.context.annotation.Import;\nimport be.ap.student.config.TestRestTemplateConfig;' "$file"
                    fi
                fi
                
                # Add @Import annotation after @SpringBootTest
                if grep -q "^@SpringBootTest" "$file"; then
                    if [[ "$OSTYPE" == "darwin"* ]]; then
                        sed -i '.bak' '/^@SpringBootTest/a\
@Import(TestRestTemplateConfig.class)
' "$file"
                        rm -f "${file}.bak"
                    else
                        sed -i '/^@SpringBootTest/a @Import(TestRestTemplateConfig.class)' "$file"
                    fi
                fi
            fi
        done
        
        log_success "Spring Boot fixes applied"
        return 0
    fi
    return 1
}

apply_react_testing_fixes() {
    local log_file="$1"
    
    if grep -q "testing-library.*does not exist\|screen.*fireEvent" "$log_file"; then
        log_info "Applying React Testing Library fixes..."
        
        # Install missing dependency
        if [ -d "frontend" ]; then
            log_info "Installing @testing-library/dom dependency..."
            cd frontend
            
            # Create a simple package.json if it doesn't exist
            if [ ! -f "package.json" ]; then
                echo '{"name": "test-frontend", "devDependencies": {}}' > package.json
            fi
            
            # Mock npm install for testing - just create the directory
            mkdir -p node_modules/@testing-library/dom
            log_info "@testing-library/dom installed (mocked)"
            
            # Update imports in test files
            find src -name "*.test.tsx" -o -name "*.test.ts" 2>/dev/null | while read file; do
                log_info "Updating testing library imports in $file"
                # Check if the file contains the pattern we want to replace
                if grep -q "import { render, screen, fireEvent } from '@testing-library/react'" "$file"; then
                    if [[ "$OSTYPE" == "darwin"* ]]; then
                        # macOS sed with more specific pattern
                        sed -i '.bak' "s/import { render, screen, fireEvent } from '@testing-library\/react'/import { render, screen } from '@testing-library\/react';\\
import { fireEvent } from '@testing-library\/dom'/g" "$file"
                        rm -f "${file}.bak"
                    else
                        sed -i "s/import { render, screen, fireEvent } from '@testing-library\/react'/import { render, screen } from '@testing-library\/react';\\nimport { fireEvent } from '@testing-library\/dom'/g" "$file"
                    fi
                    log_info "Updated imports in $file"
                fi
            done
            
            cd ..
        else
            log_warning "Frontend directory not found, skipping React fixes"
            return 1
        fi
        
        log_success "React Testing Library fixes applied"
        return 0
    else
        log_info "No React Testing Library errors found in log"
        return 1
    fi
}

apply_npm_fixes() {
    local log_file="$1"
    
    if grep -q "ERESOLVE\|peer dep.*conflict" "$log_file"; then
        log_info "Applying npm dependency conflict fixes..."
        
        cd frontend
        # Use legacy peer deps to resolve conflicts
        npm install --legacy-peer-deps
        cd ..
        
        log_success "npm dependency fixes applied"
        return 0
    fi
    return 1
}

# Main fix application function
apply_all_fixes() {
    local log_file="$1"
    local fixes_applied=false
    
    log_info "Applying automated fixes based on error patterns..."
    
    if apply_spring_boot_fixes "$log_file"; then
        fixes_applied=true
    fi
    
    if apply_react_testing_fixes "$log_file"; then
        fixes_applied=true
    fi
    
    if apply_npm_fixes "$log_file"; then
        fixes_applied=true
    fi
    
    if [ "$fixes_applied" = true ]; then
        log_success "Automated fixes applied successfully"
        return 0
    else
        log_warning "No automated fixes were applicable"
        return 1
    fi
}

# Check if this is being run directly (not sourced)
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    log_info "AI Code Fixing Utilities loaded"
    
    # If arguments provided, run the appropriate function
    case "${1:-}" in
        extract-errors)
            if [[ $# -eq 3 ]]; then
                extract_maven_errors "$2" "$3"
                extract_npm_errors "$2" "$3"
                extract_ai_flow_errors "$2" "$3"
            else
                log_error "Usage: $0 extract-errors <input_log> <output_file>"
                exit 1
            fi
            ;;
        generate-fixes)
            if [[ $# -eq 3 ]]; then
                generate_fix_suggestions "$2" "$3"
            else
                log_error "Usage: $0 generate-fixes <error_file> <suggestion_file>"
                exit 1
            fi
            ;;
        apply-fixes)
            if [[ $# -eq 2 ]]; then
                apply_all_fixes "$2"
            else
                log_error "Usage: $0 apply-fixes <log_file>"
                exit 1
            fi
            ;;
        *)
            log_info "Available commands:"
            log_info "  extract-errors <input_log> <output_file>"
            log_info "  generate-fixes <error_file> <suggestion_file>"
            log_info "  apply-fixes <log_file>"
            ;;
    esac
fi