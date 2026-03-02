# 🤖 AI Code Fixes Workflow

This GitHub Actions workflow provides automated AI-powered code fixing for your AI-SDLC project. When code issues are detected during the SDLC flow, the workflow attempts to automatically diagnose and fix common problems.

## How It Works

1. **Trigger**: Runs on push/PR to main branches or can be manually triggered
2. **SDLC Flow**: Executes your existing `./ai/flow.sh` script
3. **Error Detection**: If the flow fails, analyzes logs for common error patterns
4. **AI Analysis**: Generates fix suggestions using GitHub Copilot (if available) and pattern matching
5. **Automated Fixes**: Applies fixes for known issues automatically
6. **Verification**: Re-runs the SDLC flow to verify fixes work
7. **Pull Request**: Creates a PR with the fixes if changes were made

## Supported Fix Types

### ✅ Spring Boot 4.x Issues
- **TestRestTemplate**: Updates imports from `org.springframework.boot.test.web.client` to `org.springframework.boot.resttestclient`
- **Missing Annotations**: Adds `@AutoConfigureTestRestTemplate` to integration tests
- **Dependencies**: Ensures `spring-boot-resttestclient` is in pom.xml

### ✅ React Testing Library Issues
- **v16+ Breaking Changes**: Separates `screen`/`fireEvent` imports from different packages
- **Missing Dependencies**: Installs `@testing-library/dom` if needed
- **Import Updates**: Fixes import statements in test files

### ✅ NPM Dependency Issues
- **ERESOLVE Conflicts**: Uses `--legacy-peer-deps` to resolve ESLint conflicts
- **Cache Issues**: Clears npm cache when needed
- **Version Conflicts**: Handles peer dependency issues

## Configuration

Edit [`.github/ai-fix-config.env`](.github/ai-fix-config.env) to customize behavior:

```bash
# Feature ID for testing
FEATURE_ID=feature-001-support-ticket

# Enable specific fix types
ENABLE_SPRING_BOOT_FIXES=true
ENABLE_REACT_TESTING_FIXES=true
ENABLE_NPM_DEPENDENCY_FIXES=true

# PR settings
CREATE_FIX_PRS=true
AUTO_MERGE_SUCCESSFUL_FIXES=false
FIX_PR_LABELS=ai-generated,automated-fix
```

## Manual Usage

You can also use the fix utilities manually:

```bash
# Analyze errors from a log file
.github/scripts/ai-fix-utils.sh extract-errors flow_output.log errors.md

# Generate fix suggestions
.github/scripts/ai-fix-utils.sh generate-fixes errors.md suggestions.md

# Apply automated fixes
.github/scripts/ai-fix-utils.sh apply-fixes flow_output.log
```

## Workflow Outputs

When the workflow runs, it creates:

### 📁 Artifacts
- `flow_output.log` - Original SDLC flow output
- `flow_output_fixed.log` - SDLC flow output after fixes
- `error_analysis.md` - Categorized error analysis
- `ai_suggestions.md` - AI-generated fix suggestions
- `fix_context.md` - Complete context for AI analysis

### 🔄 Pull Requests
If fixes are applied, a PR is created with:
- **Title**: `🤖 Automated AI Code Fixes - YYYY-MM-DD`
- **Labels**: `ai-generated`, `automated-fix`, `needs-review`
- **Description**: Detailed analysis of errors and fixes applied

## Prerequisites

### Required Secrets
- `GITHUB_TOKEN` - Default token (automatically provided)
- `GEMINI_API_KEY` - For AI agent functionality (if using Gemini)

### Optional Extensions
- GitHub Copilot CLI extension (enhances AI suggestions)

### Dependencies
The workflow automatically installs:
- Node.js 22
- Java 21 (Temurin)
- npm packages for AI agents and validators

## Safety Features

### 🛡️ Loop Prevention
- Skips if commit message contains `[ai-fix]`
- Won't run on bot commits
- Limits concurrent executions

### 🔍 Smart Detection
- Only applies fixes when specific error patterns are detected
- Verifies fixes by re-running the SDLC flow
- Creates PRs only when actual changes are made

### 👥 Review Process
- All fixes require manual review (unless `AUTO_MERGE_SUCCESSFUL_FIXES=true`)
- Assigns PR to commit author
- Includes detailed explanation of changes

## Troubleshooting

### Workflow Not Triggering
- Check branch filters in workflow file
- Ensure you're not pushing bot commits
- Verify workflow permissions

### Fixes Not Working
- Check error patterns in `ai-fix-utils.sh`
- Review workflow logs for debugging
- Run fix utilities manually to test

### Permission Issues
- Ensure `GITHUB_TOKEN` has required permissions
- Check workflow permissions settings
- Verify branch protection rules allow automated PRs

## Contributing

To add support for new error types:

1. **Add Error Pattern**: Update `extract_*_errors()` functions in `ai-fix-utils.sh`
2. **Add Fix Logic**: Create new `apply_*_fixes()` function
3. **Add Suggestion**: Update `generate_fix_suggestions()` with new fix type
4. **Test**: Run workflow with intentional errors to verify

## Examples

### Successful Fix PR
```markdown
## 🤖 Automated AI Code Fixes

### Original Errors
- TestRestTemplate import errors in integration tests
- React Testing Library import conflicts

### Fixes Applied
- Updated Spring Boot TestRestTemplate imports 
- Added @testing-library/dom dependency
- Fixed test file imports

### ✅ Status: Fixes Verified
The SDLC flow now passes successfully with these changes.
```

### Manual Testing
```bash
# Test the workflow manually
gh workflow run "AI Code Fixes" --ref main

# Check workflow status
gh run list --workflow="AI Code Fixes"

# View workflow logs
gh run view [RUN_ID] --log
```

## Integration with Existing CI

This workflow complements your existing CI workflow:
- **Regular CI**: Runs on all commits, focuses on validation
- **AI Fixes**: Runs on failures, focuses on automated remediation
- **No Conflicts**: Uses different concurrency groups to avoid interference

The AI fixes workflow will only trigger when your main SDLC flow fails, making it a safety net that helps maintain development velocity.