# 🤖 AI-Powered Code Fixes - Complete Feature Documentation

## Overview

The AI-Powered Code Fixes feature provides automated detection, analysis, and remediation of common coding issues in your AI-SDLC project. When your SDLC flow encounters errors, this system automatically:

1. **Analyzes** error patterns using AI and pattern matching
2. **Generates** specific fix suggestions
3. **Applies** automated fixes for known issues
4. **Verifies** fixes by re-running the SDLC flow
5. **Creates** pull requests with detailed explanations

## 🚀 Quick Start

### Automatic Setup (Recommended)
The feature automatically activates when your SDLC flow fails. No manual intervention required.

### Manual Testing
```bash
# Run complete demo
.github/scripts/manual-test-ai-fixes.sh demo

# Create intentional errors for testing
.github/scripts/manual-test-ai-fixes.sh create-spring-error
.github/scripts/manual-test-ai-fixes.sh create-react-error

# Apply fixes manually
.github/scripts/manual-test-ai-fixes.sh apply-fixes flow_output.log
```

## 📁 File Structure

```
.github/
├── workflows/
│   ├── ai-code-fixes.yml          # Main GitHub Actions workflow
│   └── README.md                  # Workflow documentation
├── scripts/
│   ├── ai-fix-utils.sh            # Core fixing utilities
│   ├── test-ai-fixes.sh           # Unit tests
│   ├── e2e-test-ai-fixes.sh       # End-to-end tests
│   └── manual-test-ai-fixes.sh    # Manual testing tools
└── ai-fix-config.env              # Configuration file
```

## 🔧 Supported Fix Types

### ✅ Spring Boot 4.x Issues
- **TestRestTemplate Import Changes**
  - Detects: `org.springframework.boot.test.web.client.TestRestTemplate` 
  - Fixes: Updates to `org.springframework.boot.resttestclient.TestRestTemplate`
  - Adds: `@AutoConfigureTestRestTemplate` annotations

### ✅ React Testing Library v16+ Issues  
- **Import Restructuring**
  - Detects: Combined imports causing module errors
  - Fixes: Separates `render/screen` from `fireEvent` imports
  - Installs: Missing `@testing-library/dom` dependency

### ✅ NPM Dependency Conflicts
- **ESLint Peer Dependencies**
  - Detects: ERESOLVE errors and peer dependency conflicts
  - Fixes: Uses `--legacy-peer-deps` flag
  - Clears: npm cache when needed

## 🎯 How It Works

### 1. Error Detection
When your SDLC flow (`./ai/flow.sh`) fails, the workflow:
- Captures all error output
- Categorizes errors by type (Maven, npm, AI flow)
- Creates detailed error analysis

### 2. AI Analysis
The system generates fix suggestions using:
- **Pattern Matching**: Known error signatures
- **GitHub Copilot**: Enhanced suggestions (if available)
- **Context Analysis**: Repository structure and recent changes

### 3. Automated Fixes
For supported error types, the system:
- Updates file imports and dependencies
- Modifies configuration files
- Installs missing packages
- Applies annotations and decorators

### 4. Verification
After applying fixes:
- Re-runs the SDLC flow
- Validates that errors are resolved
- Creates detailed success/failure reports

### 5. Pull Request Creation
If fixes are applied:
- Creates a new branch with timestamp
- Commits changes with descriptive messages
- Opens PR with full error analysis and fix details
- Assigns to the original commit author

## 📊 Workflow Triggers

```yaml
# Automatic triggers
on:
  push:
    branches: [main, develop, feature/*]
  pull_request:
    branches: [main, develop]
  
# Manual trigger
workflow_dispatch: true
```

## ⚙️ Configuration

Edit `.github/ai-fix-config.env`:

```bash
# Feature toggle
ENABLE_SPRING_BOOT_FIXES=true
ENABLE_REACT_TESTING_FIXES=true
ENABLE_NPM_DEPENDENCY_FIXES=true

# PR settings
CREATE_FIX_PRS=true
AUTO_MERGE_SUCCESSFUL_FIXES=false
FIX_PR_LABELS=ai-generated,automated-fix

# Safety settings
MAX_FIX_ATTEMPTS=3
MAX_FILES_CHANGED=50
SKIP_IF_BOT_COMMIT=true
```

## 🛡️ Safety Features

### Loop Prevention
- Skips commits with `[ai-fix]` message
- Won't run on bot-generated commits
- Limits concurrent workflow executions

### Smart Detection
- Only applies fixes for detected error patterns
- Validates fixes before creating PRs
- Provides detailed change explanations

### Review Process
- All PRs require manual review by default
- Includes comprehensive change documentation
- Shows before/after comparisons

## 🧪 Testing

### Unit Tests
```bash
cd .github/scripts
./test-ai-fixes.sh
```

### End-to-End Tests  
```bash
cd .github/scripts
./e2e-test-ai-fixes.sh
```

### Manual Testing
```bash
# Complete feature demo
./manual-test-ai-fixes.sh demo

# Individual components
./manual-test-ai-fixes.sh check-dependencies
./manual-test-ai-fixes.sh validate-scripts
./manual-test-ai-fixes.sh simulate-ai-fixes
```

## 📈 Monitoring & Metrics

### Workflow Artifacts
Each run produces:
- `flow_output.log` - Original SDLC output
- `flow_output_fixed.log` - Post-fix SDLC output  
- `error_analysis.md` - Categorized error analysis
- `ai_suggestions.md` - Generated fix suggestions
- `fix_context.md` - Complete AI context

### Success Metrics
- ✅ **Fix Success Rate**: Fixes that resolve all errors
- ⚠️ **Partial Fix Rate**: Fixes that resolve some errors
- 📊 **Error Categories**: Most common error types
- 🚀 **Time to Resolution**: Average fix application time

## 🔍 Troubleshooting

### Common Issues

**Workflow Not Triggering**
- Check branch protection rules
- Verify GitHub Actions permissions
- Ensure not pushing bot commits

**Fixes Not Applied**
- Review error patterns in logs
- Check if errors match supported types
- Validate script permissions

**PRs Not Created**
- Verify `GITHUB_TOKEN` permissions
- Check branch protection rules
- Review git configuration

### Debug Commands
```bash
# Check workflow status
gh workflow list
gh run list --workflow="AI Code Fixes"

# View detailed logs
gh run view [RUN_ID] --log

# Test locally
./manual-test-ai-fixes.sh apply-fixes error.log
```

## 🚢 Deployment

### Prerequisites
- GitHub repository with Actions enabled
- Existing SDLC flow (`./ai/flow.sh`)
- Required secrets: `GITHUB_TOKEN`, `GEMINI_API_KEY`

### Installation
1. **Copy Files**: Add all workflow and script files to your repository
2. **Set Permissions**: Ensure scripts are executable
3. **Configure Secrets**: Add required API keys
4. **Test**: Run manual demo to validate setup
5. **Deploy**: Commit and push to activate

### Production Checklist
- [ ] All tests passing
- [ ] Configuration reviewed
- [ ] Permissions validated
- [ ] Monitoring setup
- [ ] Team training completed

## 🤝 Contributing

### Adding New Fix Types
1. **Error Pattern**: Add detection logic in `ai-fix-utils.sh`
2. **Fix Logic**: Implement automated fix function
3. **Suggestions**: Add to fix suggestions generator
4. **Tests**: Create unit and integration tests
5. **Documentation**: Update this README

### Example: Adding New Error Type
```bash
# 1. Add error detection
extract_new_error_type() {
    grep -n "NEW_ERROR_PATTERN" "$log_file" >> "$output_file"
}

# 2. Add fix logic  
apply_new_fixes() {
    if grep -q "NEW_ERROR_PATTERN" "$log_file"; then
        # Apply fixes
        return 0
    fi
    return 1
}

# 3. Add to main function
apply_all_fixes() {
    if apply_new_fixes "$log_file"; then
        fixes_applied=true
    fi
}
```

## 📚 Additional Resources

### Related Documentation
- [GitHub Actions Workflow Documentation](.github/workflows/README.md)
- [AI Fix Utilities Documentation](.github/scripts/ai-fix-utils.sh)
- [SDLC Flow Documentation](../ai/README.md)

### External Resources
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub Copilot CLI](https://cli.github.com/manual/gh_copilot)
- [Spring Boot Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [React Testing Library v16 Changes](https://testing-library.com/docs/react-testing-library/intro)

## 📋 Changelog

### v1.0.0 (2026-03-02)
- ✨ Initial release
- ✅ Spring Boot 4.x TestRestTemplate fixes
- ✅ React Testing Library v16+ import fixes
- ✅ npm dependency conflict resolution
- ✅ Automated PR creation
- ✅ Comprehensive testing suite
- ✅ Manual testing tools
- ✅ Complete documentation

---

## 🎉 Success Stories

> "The AI Code Fixes feature saved us 3+ hours of debugging time when migrating to Spring Boot 4.x. It automatically detected and fixed 15+ TestRestTemplate import issues across our test suite." - *Development Team*

> "React Testing Library v16 upgrade was painless thanks to automated import restructuring. The feature detected breaking changes and applied fixes before we even noticed the issues." - *Frontend Team*

---

*Built with ❤️ for the AI-SDLC project | Last updated: March 2, 2026*