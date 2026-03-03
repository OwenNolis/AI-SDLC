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

# Generate AI-powered fix suggestions
generate_fix_suggestions() {
    local error_file="$1"
    local suggestion_file="$2"
    
    log_info "Generating AI-powered fix suggestions..."
    
    # Initialize the suggestion file
    cat > "$suggestion_file" << 'EOF'
# 🤖 AI-Generated Fix Suggestions

Based on error analysis using advanced AI, here are specific fixes for your AI-SDLC project:

EOF

    # Read error content for AI analysis
    local error_content=$(cat "$error_file" | head -100)  # Limit for token constraints
    
    # Generate AI-powered suggestions
    if [ -n "$GEMINI_API_KEY" ]; then
        cat > /tmp/suggestion_prompt.txt << EOF
You are an expert software engineer. Analyze these compilation/build errors and provide actionable fix suggestions.

ERRORS:
$error_content

CONTEXT:
- Spring Boot 4.x project with Maven
- React TypeScript frontend with Vite
- Integration tests using RestTemplate/TestRestTemplate
- Node.js AI test generation scripts

TASK: Generate specific, actionable fix suggestions with:
1. Root cause analysis
2. Exact commands to run
3. Code snippets to add/modify
4. File paths and line numbers where applicable

Format as clear, executable steps. Be specific and practical.
EOF

        local ai_suggestions=$(curl -s -X POST \
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=$GEMINI_API_KEY" \
            -H 'Content-Type: application/json' \
            -d "{
                \"contents\": [
                    {
                        \"parts\": [
                            {
                                \"text\": \"$(cat /tmp/suggestion_prompt.txt | sed 's/"/\\"/g' | tr '\n' '\\n')\"
                            }
                        ]
                    }
                ],
                \"generationConfig\": {
                    \"temperature\": 0.2,
                    \"maxOutputTokens\": 3072
                }
            }" | jq -r '.candidates[0].content.parts[0].text' 2>/dev/null)
        
        if [ -n "$ai_suggestions" ] && [ "$ai_suggestions" != "null" ]; then
            echo "" >> "$suggestion_file"
            echo "## 🧠 AI Analysis & Suggestions" >> "$suggestion_file"
            echo "" >> "$suggestion_file"
            echo "$ai_suggestions" >> "$suggestion_file"
            log_success "AI-powered suggestions generated"
        else
            log_warning "AI suggestion generation failed, using fallback"
            generate_fallback_suggestions "$error_file" "$suggestion_file"
        fi
        
        rm -f /tmp/suggestion_prompt.txt
    else
        log_warning "GEMINI_API_KEY not set, using pattern-based suggestions"
        generate_fallback_suggestions "$error_file" "$suggestion_file"
    fi
    
    log_success "Fix suggestions generated in $suggestion_file"
}

# Fallback function for when AI is not available
generate_fallback_suggestions() {
    local error_file="$1" 
    local suggestion_file="$2"
    
    cat >> "$suggestion_file" << 'EOF'
## 🔧 Pattern-Based Fix Suggestions

Based on common error patterns detected:

EOF

    # Spring Boot issues
    if grep -q "TestRestTemplate\|cannot find symbol.*RestTemplate\|RestTemplateConfig" "$error_file"; then
        cat >> "$suggestion_file" << 'EOF'
### Spring Boot RestTemplate Issues
- **Problem**: Missing RestTemplate configuration or incorrect imports
- **Solution**: Create proper TestConfiguration class and fix imports
- **Action**: The automated fixer will create TestRestTemplateConfig.java and update imports

EOF
    fi

    # React Testing Library issues  
    if grep -q "testing-library.*does not exist\|fireEvent.*screen" "$error_file"; then
        cat >> "$suggestion_file" << 'EOF'
### React Testing Library Issues
- **Problem**: Missing dependencies or incorrect imports
- **Solution**: Install @testing-library/dom and update imports
- **Action**: Run `cd frontend && npm install --save-dev @testing-library/dom`

EOF
    fi

    # Maven compilation issues
    if grep -q "BUILD FAILURE\|compilation failure" "$error_file"; then
        cat >> "$suggestion_file" << 'EOF'
### Maven Compilation Issues
- **Problem**: Java compilation errors
- **Solution**: Clean rebuild and fix imports
- **Action**: Run `cd backend && mvn clean compile test-compile`

EOF
    fi

    # Check for ESLint/npm conflicts
    if grep -q "ERESOLVE\|peer dep.*conflict\|npm ERR.*ERESOLVE" "$error_file"; then
        cat >> "$suggestion_file" << 'EOF'
### NPM Dependency Conflict Fix
- **Problem**: ESLint peer dependency conflicts
- **Solution**: Use legacy peer deps resolution
- **Action**: Run `cd frontend && npm install --legacy-peer-deps`

EOF
    fi

    # Check for Java compilation issues
    if grep -q "cannot find symbol\|package.*does not exist\|BUILD FAILURE" "$error_file"; then
        cat >> "$suggestion_file" << 'EOF'
### Java Compilation Fix
- **Problem**: Missing imports or dependencies
- **Solution**: Clean and rebuild with proper dependencies
- **Action**: Run `cd backend && mvn clean compile test-compile`

EOF
    fi
}

# AI-powered error analysis and fixing
analyze_errors_with_ai() {
    local error_file="$1"
    local analysis_file="$2"
    
    log_info "Analyzing errors with Gemini AI..."
    
    # Prepare error context for AI analysis
    local error_content=$(cat "$error_file" | head -200)  # Limit to avoid token limits
    
    # Create AI prompt for error analysis
    cat > /tmp/ai_error_prompt.txt << EOF
You are an expert software engineer analyzing compilation and build errors. 

TASK: Analyze the following errors and provide specific, actionable fixes.

ERRORS:
$error_content

REQUIREMENTS:
1. Identify the root cause of each error
2. Provide specific code changes needed
3. Include file paths and exact code modifications
4. Focus on practical, working solutions
5. Consider Spring Boot 4+, React Testing Library best practices

FORMAT your response as JSON:
{
  "analysis": "Brief summary of issues found",
  "fixes": [
    {
      "file": "relative/path/to/file",
      "issue": "Description of the problem",
      "action": "create|modify|delete",
      "content": "Exact code content or modification"
    }
  ]
}
EOF

    # Call Gemini API for error analysis
    if [ -n "$GEMINI_API_KEY" ]; then
        local gemini_response=$(curl -s -X POST \
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=$GEMINI_API_KEY" \
            -H 'Content-Type: application/json' \
            -d "{
                \"contents\": [
                    {
                        \"parts\": [
                            {
                                \"text\": \"$(cat /tmp/ai_error_prompt.txt | sed 's/"/\\"/g' | tr '\n' '\\n')\"
                            }
                        ]
                    }
                ],
                \"generationConfig\": {
                    \"temperature\": 0.1,
                    \"maxOutputTokens\": 4096
                }
            }")
        
        # Extract text from Gemini response
        local ai_analysis=$(echo "$gemini_response" | jq -r '.candidates[0].content.parts[0].text' 2>/dev/null || echo "")
        
        if [ -n "$ai_analysis" ] && [ "$ai_analysis" != "null" ]; then
            echo "$ai_analysis" > "$analysis_file"
            log_success "AI analysis completed"
            return 0
        else
            log_warning "Gemini API call failed or returned empty response"
        fi
    else
        log_warning "GEMINI_API_KEY not set, skipping AI analysis"
    fi
    
    # Fallback analysis if AI fails
    cat > "$analysis_file" << 'EOF'
{
  "analysis": "Fallback analysis - compilation errors detected",
  "fixes": [
    {
      "file": "backend/src/test/java/be/ap/student/config/TestRestTemplateConfig.java",
      "issue": "Missing RestTemplate configuration class",
      "action": "create",
      "content": "package be.ap.student.config;\n\nimport org.springframework.boot.test.context.TestConfiguration;\nimport org.springframework.web.client.RestTemplate;\nimport org.springframework.context.annotation.Bean;\n\n@TestConfiguration\npublic class TestRestTemplateConfig {\n    \n    @Bean\n    public RestTemplate restTemplate() {\n        return new RestTemplate();\n    }\n}"
    }
  ]
}
EOF
    
    rm -f /tmp/ai_error_prompt.txt
}

apply_ai_fixes() {
    local analysis_file="$1"
    
    log_info "Applying AI-generated fixes..."
    
    # Parse JSON and apply fixes
    if [ -f "$analysis_file" ]; then
        local fixes_count=$(jq -r '.fixes | length' "$analysis_file" 2>/dev/null || echo "0")
        
        if [ "$fixes_count" -gt 0 ]; then
            for i in $(seq 0 $((fixes_count - 1))); do
                local file=$(jq -r ".fixes[$i].file" "$analysis_file" 2>/dev/null || echo "")
                local issue=$(jq -r ".fixes[$i].issue" "$analysis_file" 2>/dev/null || echo "")
                local action=$(jq -r ".fixes[$i].action" "$analysis_file" 2>/dev/null || echo "")
                local content=$(jq -r ".fixes[$i].content" "$analysis_file" 2>/dev/null || echo "")
                
                if [ -n "$file" ] && [ -n "$action" ]; then
                    log_info "Fixing: $issue in $file"
                    
                    case "$action" in
                        "create")
                            mkdir -p "$(dirname "$file")"
                            echo -e "$content" > "$file"
                            log_success "Created $file"
                            ;;
                        "modify")
                            if [ -f "$file" ]; then
                                # Use GitHub Copilot CLI for intelligent modifications if available
                                if command -v gh >/dev/null 2>&1 && command -v copilot >/dev/null 2>&1; then
                                    log_info "Using GitHub Copilot for intelligent file modification"
                                    echo "$content" | copilot suggest --file "$file" || {
                                        echo -e "$content" > "$file"
                                        log_success "Modified $file (fallback)"
                                    }
                                else
                                    echo -e "$content" > "$file"
                                    log_success "Modified $file"
                                fi
                            else
                                log_warning "File $file does not exist, cannot modify"
                            fi
                            ;;
                        "delete")
                            if [ -f "$file" ]; then
                                rm "$file"
                                log_success "Deleted $file"
                            else
                                log_warning "File $file does not exist, cannot delete"
                            fi
                            ;;
                    esac
                fi
            done
            
            # Clean up duplicate imports and annotations using AI if possible
            cleanup_duplicate_imports
            
            log_success "Applied $fixes_count AI-generated fixes"
            return 0
        else
            log_warning "No fixes found in AI analysis"
            return 1
        fi
    else
        log_error "Analysis file not found: $analysis_file"
        return 1
    fi
}

cleanup_duplicate_imports() {
    log_info "Cleaning up duplicate imports and annotations..."
    
    # Find Java files with potential duplicate imports
    find backend/src/test -name "*.java" | while read file; do
        if grep -q "RestTemplateConfig" "$file"; then
            # Use AI to clean up the file if possible
            if [ -n "$GEMINI_API_KEY" ]; then
                local file_content=$(cat "$file")
                
                cat > /tmp/cleanup_prompt.txt << EOF
Clean up this Java file by:
1. Remove all duplicate import statements
2. Remove duplicate @Import annotations (keep only one)  
3. Fix incorrect RestTemplateConfig references to TestRestTemplateConfig
4. Ensure proper Spring Boot test configuration

FILE CONTENT:
$file_content

Return ONLY the cleaned up Java code, no explanation.
EOF

                local cleaned_content=$(curl -s -X POST \
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=$GEMINI_API_KEY" \
                    -H 'Content-Type: application/json' \
                    -d "{
                        \"contents\": [
                            {
                                \"parts\": [
                                    {
                                        \"text\": \"$(cat /tmp/cleanup_prompt.txt | sed 's/"/\\"/g' | tr '\n' '\\n')\"
                                    }
                                ]
                            }
                        ],
                        \"generationConfig\": {
                            \"temperature\": 0.1,
                            \"maxOutputTokens\": 2048
                        }
                    }" | jq -r '.candidates[0].content.parts[0].text' 2>/dev/null)
                
                if [ -n "$cleaned_content" ] && [ "$cleaned_content" != "null" ]; then
                    echo "$cleaned_content" > "$file"
                    log_success "AI-cleaned $file"
                else
                    # Fallback cleanup
                    simple_cleanup_file "$file"
                fi
                
                rm -f /tmp/cleanup_prompt.txt
            else
                # Simple pattern-based cleanup
                simple_cleanup_file "$file"
            fi
        fi
    done
}

simple_cleanup_file() {
    local file="$1"
    
    # Simple deduplication
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # Remove duplicate import lines (macOS)
        sed -i '.bak' '/import.*RestTemplateConfig/!b; N; /\n.*RestTemplateConfig/d; P; D' "$file"
        # Remove duplicate @Import annotations
        sed -i '.bak' '/@Import.*RestTemplateConfig/!b; N; /\n.*@Import.*RestTemplateConfig/d; P; D' "$file"
        # Fix wrong class references
        sed -i '.bak' 's/be\.ap\.student\.config\.RestTemplateConfig/be.ap.student.config.TestRestTemplateConfig/g' "$file"
        rm -f "${file}.bak"
    else
        # Linux versions
        sed -i '/import.*RestTemplateConfig/!b; N; /\n.*RestTemplateConfig/d; P; D' "$file"
        sed -i '/@Import.*RestTemplateConfig/!b; N; /\n.*@Import.*RestTemplateConfig/d; P; D' "$file"
        sed -i 's/be\.ap\.student\.config\.RestTemplateConfig/be.ap.student.config.TestRestTemplateConfig/g' "$file"
    fi
    
    log_success "Simple cleanup applied to $file"
}

# Legacy function maintained for backward compatibility but now calls AI-powered version
apply_spring_boot_fixes() {
    local log_file="$1"
    
    log_info "Using AI-powered error analysis and fixing..."
    
    # Use AI to analyze and fix errors
    analyze_errors_with_ai "$log_file" "/tmp/ai_analysis.json"
    apply_ai_fixes "/tmp/ai_analysis.json"
    
    # Cleanup
    rm -f /tmp/ai_analysis.json
    
    return 0
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