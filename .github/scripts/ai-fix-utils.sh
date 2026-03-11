#!/bin/bash
# ──────────────────────────────────────────────────────────────
# AI Code Fixing Utilities – Generic, AI-powered error fixing
# Sends real errors + real source code to Gemini and applies
# the returned fixes. No hard-coded file patterns.
# ──────────────────────────────────────────────────────────────
# NOTE: No 'set -e' at top level — this file is sourced by the
# workflow, so errexit would bleed into the caller.

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

# Count UNIQUE compile errors by file:line — ignores Maven boilerplate
# like "[ERROR] -> [Help 1]", "[ERROR] Re-run Maven...", etc.
# This prevents the cascade-reveal problem where fixing one error
# unmasks hidden errors, temporarily increasing raw [ERROR] count.
count_unique_errors() {
    local log_file="$1"
    # Match lines like: [ERROR] /path/File.java:[42,10] message
    # The { grep ... || true; } ensures exit-code 0 even when grep finds
    # nothing, which is critical under set -e / set -o pipefail.
    { grep -oE '\[ERROR\] [^ ]+\.java:\[[0-9]+,[0-9]+\]' "$log_file" 2>/dev/null || true; } \
        | sort -u | wc -l | tr -d ' '
}

# ──────────────────────────────────────────────────────────────
# 1. Extract errors from any build / test log
# ──────────────────────────────────────────────────────────────
extract_all_errors() {
    local log_file="$1" output_file="$2"
    log_info "Extracting errors from build output..."
    : > "$output_file"

    # Maven compilation errors
    grep -A4 "cannot find symbol\|package.*does not exist\|incompatible types\|COMPILATION ERROR\|method.*cannot be applied" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # Maven / JUnit test failures
    grep -A10 "<<< FAILURE\|<<< ERROR\|Tests run:.*Failures:\|Tests run:.*Errors:" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # HTTP errors surfacing in integration tests
    grep -A6 "HttpServerErrorException\|HttpClientErrorException\|500.*Internal Server\|404.*Not Found" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # NPM / TypeScript / Jest
    grep -A6 "npm ERR\|TS[0-9]\+:\|Cannot find module\|FAIL.*\.test\.\|SyntaxError" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # Generic [ERROR] lines
    grep "^\[ERROR\]\|^Error:" "$log_file" >> "$output_file" 2>/dev/null || true

    local n; n=$(wc -l < "$output_file" | tr -d ' ')
    log_info "Extracted $n lines of error context"
}

# ──────────────────────────────────────────────────────────────
# 2. Discover which source files are affected
# ──────────────────────────────────────────────────────────────
extract_affected_files() {
    local error_file="$1"
    local files=""

    # Absolute paths in Maven output  (/home/runner/.../File.java:[line,col])
    files=$(grep -oE '/[^ ]*\.java' "$error_file" 2>/dev/null \
        | sed 's|.*/AI-SDLC/||' | sort -u || true)

    # Relative paths (backend/src/...)
    local rel; rel=$(grep -oE 'backend/src/[^ ]*\.java' "$error_file" 2>/dev/null | sort -u || true)
    files=$(printf '%s\n%s' "$files" "$rel" | sort -u | grep -v '^$' || true)

    # Fully-qualified class names → file paths
    local classes; classes=$(grep -oE 'be(\.[a-zA-Z_]+)+' "$error_file" 2>/dev/null | sort -u || true)
    for cls in $classes; do
        local p; p=$(echo "$cls" | tr '.' '/')
        for base in backend/src/main/java backend/src/test/java; do
            if [ -f "$base/$p.java" ]; then
                files=$(printf '%s\n%s' "$files" "$base/$p.java")
            fi
        done
    done

    # De-duplicate and keep only files that actually exist on disk
    local result=""
    while IFS= read -r f; do
        [ -n "$f" ] && [ -f "$f" ] && result=$(printf '%s\n%s' "$result" "$f")
    done <<< "$(echo "$files" | sort -u)"
    echo "$result" | sed '/^$/d'
}

# ──────────────────────────────────────────────────────────────
# 3. Build a source-context block for Gemini
# ──────────────────────────────────────────────────────────────
build_source_context() {
    local file_list="$1" ctx=""
    while IFS= read -r f; do
        [ -z "$f" ] && continue
        ctx="${ctx}
--- FILE: ${f} ---
$(cat "$f")
--- END FILE ---
"
    done <<< "$file_list"
    echo "$ctx"
}

# ──────────────────────────────────────────────────────────────
# 4. Call Gemini API  (errors + source code → JSON fixes)
# ──────────────────────────────────────────────────────────────
call_gemini() {
    local error_content="$1" source_context="$2" out_file="$3"

    [ -z "$GEMINI_API_KEY" ] && { log_error "GEMINI_API_KEY not set"; return 1; }

    log_info "Calling Gemini AI with errors + source code …"

    # ── build prompt ────────────────────────────────────
    cat > /tmp/ai_prompt.txt << 'PROMPT'
You are an expert Java / Spring Boot developer.
Analyse the ERRORS below together with the SOURCE CODE of the affected files
and return a JSON object with concrete fixes.

RULES:
• Each fix must contain the COMPLETE file content (package, imports, class body).
  Never use placeholders like "// … rest of code".
• Only touch files that are actually broken.  Keep working code intact.
• NEVER delete or remove source files. Always use action "modify" to fix them.
  Deleting a file breaks the project. Fix the code inside the file instead.
• For missing imports → add the right import.
• For undefined classes/methods used in production code → remove the broken
  usage or replace it with a minimal working alternative.
• For test failures caused by missing endpoints → create the endpoint or fix
  the test expectation so the test passes.
• Return ONLY valid JSON. No markdown fences, no explanation outside the JSON.

RESPONSE FORMAT (strict JSON):
{
  "analysis": "one-paragraph summary",
  "fixes": [
    {
      "file": "backend/src/main/java/com/example/Foo.java",
      "issue": "what was wrong",
      "action": "modify",
      "content": "full file content …"
    }
  ]
}
PROMPT

    printf '\nERRORS:\n'            >> /tmp/ai_prompt.txt
    head -200 <<< "$error_content"  >> /tmp/ai_prompt.txt
    printf '\nSOURCE CODE:\n'       >> /tmp/ai_prompt.txt
    head -600 <<< "$source_context" >> /tmp/ai_prompt.txt

    # Escape for JSON
    local escaped
    escaped=$(python3 -c "import sys,json; print(json.dumps(sys.stdin.read()))" < /tmp/ai_prompt.txt)

    # ── API call with retry ─────────────────────────────
    local model="${GEMINI_MODEL:-gemini-2.5-flash}"
    local url="https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=$GEMINI_API_KEY"
    local body="{
      \"contents\":[{\"parts\":[{\"text\":$escaped}]}],
      \"generationConfig\":{\"temperature\":0.1,\"maxOutputTokens\":16384,\"responseMimeType\":\"application/json\"}
    }"

    local resp="" attempt=0 max_attempts=4 wait_secs=5
    while [ $attempt -lt $max_attempts ]; do
        attempt=$((attempt+1))
        log_info "  Gemini request attempt $attempt/$max_attempts (model: $model)"
        resp=$(timeout 180 curl -s -X POST "$url" \
            -H 'Content-Type: application/json' \
            -d "$body" 2>&1) || true

        # Check for retryable errors (503, 429, empty)
        if [ -z "$resp" ]; then
            log_warning "  Empty response – retrying in ${wait_secs}s …"
        elif echo "$resp" | jq -e '.error' > /dev/null 2>&1; then
            local http_code; http_code=$(echo "$resp" | jq -r '.error.code // 0' 2>/dev/null)
            local api_err; api_err=$(echo "$resp" | jq -r '.error.message // empty' 2>/dev/null)
            if [ "$http_code" = "503" ] || [ "$http_code" = "429" ]; then
                log_warning "  Retryable API error ($http_code): ${api_err} – retrying in ${wait_secs}s …"
            else
                log_warning "  API error ($http_code): ${api_err}"
                break  # non-retryable API error
            fi
        elif ! echo "$resp" | jq -e '.candidates[0]' > /dev/null 2>&1; then
            log_warning "  Unexpected response format – retrying in ${wait_secs}s …"
        else
            break   # got a real response with candidates
        fi

        [ $attempt -lt $max_attempts ] && sleep $wait_secs
        wait_secs=$((wait_secs * 2))
    done

    [ -z "$resp" ] && { log_warning "Gemini API returned nothing after $max_attempts attempts"; rm -f /tmp/ai_prompt.txt; return 1; }

    # Check for API-level errors
    local err; err=$(echo "$resp" | jq -r '.error.message // empty' 2>/dev/null)
    [ -n "$err" ] && { log_warning "Gemini error: $err"; rm -f /tmp/ai_prompt.txt; return 1; }

    # Extract text from Gemini response
    local finish_reason; finish_reason=$(echo "$resp" | jq -r '.candidates[0].finishReason // "STOP"' 2>/dev/null)
    if [ "$finish_reason" = "MAX_TOKENS" ]; then
        log_warning "  Gemini response was TRUNCATED (hit output token limit)"
    fi

    local raw; raw=$(echo "$resp" | jq -r '.candidates[0].content.parts[0].text // empty' 2>/dev/null)
    [ -z "$raw" ] && { log_warning "Empty candidate text"; rm -f /tmp/ai_prompt.txt; return 1; }

    # Strip optional markdown fences
    local clean; clean=$(echo "$raw" | sed 's/^```json//;s/^```//;s/```$//' | sed '/^[[:space:]]*$/d')

    if echo "$clean" | jq . > /dev/null 2>&1; then
        echo "$clean" > "$out_file"
    else
        # Last-ditch: use Python json.JSONDecoder for robust extraction of first JSON object
        clean=$(python3 -c "
import sys, json
text = sys.stdin.read()
# Try the whole text first
try:
    o = json.loads(text)
    print(json.dumps(o))
    sys.exit(0)
except: pass
# Find the first { and use raw_decode
idx = text.find('{')
if idx >= 0:
    try:
        decoder = json.JSONDecoder()
        o, _ = decoder.raw_decode(text, idx)
        print(json.dumps(o))
        sys.exit(0)
    except: pass
sys.exit(1)
" <<< "$raw" 2>/dev/null)
        if [ -n "$clean" ] && echo "$clean" | jq . > /dev/null 2>&1; then
            echo "$clean" > "$out_file"
        else
            log_warning "Could not extract valid JSON from Gemini"
            log_info "First 500 chars: $(head -c500 <<< "$raw")"
            rm -f /tmp/ai_prompt.txt
            return 1
        fi
    fi

    local cnt; cnt=$(jq '.fixes|length' "$out_file" 2>/dev/null || echo 0)
    log_success "Gemini returned $cnt fix(es)"
    rm -f /tmp/ai_prompt.txt
    return 0
}

# ──────────────────────────────────────────────────────────────
# 5. Apply fixes from the JSON file
# ──────────────────────────────────────────────────────────────
apply_fixes() {
    local json_file="$1"
    [ ! -s "$json_file" ] && { log_error "No fixes file"; return 1; }

    local n; n=$(jq '.fixes|length' "$json_file" 2>/dev/null || echo 0)
    [ "$n" -eq 0 ] && { log_warning "Zero fixes in JSON"; return 1; }

    log_info "Applying $n fix(es) …"
    local ok=0
    for ((i=0;i<n;i++)); do
        local fp; fp=$(jq -r ".fixes[$i].file"    "$json_file")
        local act; act=$(jq -r ".fixes[$i].action" "$json_file")
        local iss; iss=$(jq -r ".fixes[$i].issue"  "$json_file")
        local con; con=$(jq -r ".fixes[$i].content" "$json_file")

        [ "$fp" = "null" ] || [ -z "$fp" ] && continue
        [ "$con" = "null" ] && con=""

        log_info "  [$((i+1))/$n] $act $fp — $iss"

        case "$act" in
            create|modify)
                mkdir -p "$(dirname "$fp")"
                printf '%s\n' "$con" > "$fp"
                log_success "  Written $fp"
                ok=$((ok+1))
                ;;
            delete|remove)
                # Safety: never delete source files — skip with warning
                if echo "$fp" | grep -qE '\.(java|kt|ts|tsx|js|jsx|py|go|rs)$'; then
                    log_warning "  SKIPPED deletion of source file $fp (safety guard)"
                else
                    [ -f "$fp" ] && { rm -f "$fp"; log_success "  Deleted $fp"; ok=$((ok+1)); }
                fi
                ;;
        esac
    done
    log_success "Applied $ok of $n fix(es)"
}

# ──────────────────────────────────────────────────────────────
# 6. Orchestrator – iterative fix loop
# ──────────────────────────────────────────────────────────────
run_fix_pipeline() {
    local error_file="$1"
    local analysis="/tmp/ai_analysis.json"
    local max=5 iter=0
    local regression_detected=false

    log_info "╔══════════════════════════════════════╗"
    log_info "║   AI Fix Pipeline  (max $max iterations)  ║"
    log_info "╚══════════════════════════════════════╝"

    while [ $iter -lt $max ]; do
        iter=$((iter+1))
        log_info "── iteration $iter/$max ──────────────────────"

        # Make sure we have fresh error content
        local errs; errs=$(cat "$error_file" 2>/dev/null | head -200 || true)
        if [ -z "$errs" ] || [ "$(wc -l <<< "$errs" | tr -d ' ')" -lt 2 ]; then
            log_info "Error file thin – running build to capture errors …"

            local cc=0
            (cd backend && mvn test-compile -q > /tmp/_compile.log 2>&1) || cc=$?

            if [ $cc -ne 0 ]; then
                extract_all_errors /tmp/_compile.log "$error_file"
            else
                local tc=0
                (cd backend && mvn test > /tmp/_test.log 2>&1) || tc=$?
                if [ $tc -ne 0 ]; then
                    extract_all_errors /tmp/_test.log "$error_file"
                else
                    log_success "Build + tests pass – nothing to fix!"
                    return 0
                fi
            fi
            errs=$(cat "$error_file" | head -200 || true)
        fi

        # Discover affected files & read their source
        local files; files=$(extract_affected_files "$error_file")
        [ -z "$files" ] && files=$(grep -oE 'backend/src/[^ ]*\.java' "$error_file" 2>/dev/null | sort -u || true)
        log_info "Affected files: $(echo $files | tr '\n' ', ')"

        local src; src=$(build_source_context "$files")

        # Ask Gemini
        if ! call_gemini "$errs" "$src" "$analysis"; then
            log_error "Gemini call failed on iteration $iter"
            if [ $iter -gt 1 ]; then
                log_warning "Continuing with fixes from previous iterations"
                break
            fi
            return 1
        fi

        # ── Regression guard: snapshot & count errors BEFORE fixes ──
        local savepoint
        savepoint=$(git stash create 2>/dev/null || true)
        local errors_before=0
        (cd backend && mvn test-compile > /tmp/_pre_fix.log 2>&1) || true
        errors_before=$(count_unique_errors /tmp/_pre_fix.log)
        log_info "Unique error count before iteration $iter fixes: $errors_before"

        # Apply
        apply_fixes "$analysis"

        # ── Verify & regression check ──
        local cc2=0
        (cd backend && mvn test-compile > /tmp/_post_fix.log 2>&1) || cc2=$?
        local errors_after=0
        errors_after=$(count_unique_errors /tmp/_post_fix.log)
        log_info "Unique error count after iteration $iter fixes: $errors_after"

        # If AI made things WORSE, revert this iteration and stop
        if [ "$errors_after" -gt "$errors_before" ] && [ "$errors_before" -gt 0 ]; then
            log_error "🚨 REGRESSION: errors increased from $errors_before → $errors_after"
            log_error "Reverting iteration $iter fixes to prevent further damage"
            git checkout -- . 2>/dev/null || true
            git clean -fd 2>/dev/null || true
            if [ -n "$savepoint" ]; then
                git stash apply "$savepoint" 2>/dev/null || true
            fi
            regression_detected=true
            break
        fi

        if [ $cc2 -eq 0 ]; then
            log_success "Compilation OK after iteration $iter"

            # Now check tests
            local tc2=0
            (cd backend && mvn test > /tmp/_retest.log 2>&1) || tc2=$?

            if [ $tc2 -eq 0 ]; then
                log_success "All tests pass after iteration $iter!"
                return 0
            else
                log_info "Tests still failing – feeding new errors to next iteration"
                extract_all_errors /tmp/_retest.log "$error_file"
            fi
        else
            log_info "Compilation still broken – feeding new errors to next iteration"
            (cd backend && mvn test-compile > /tmp/_recompile.log 2>&1) || true
            extract_all_errors /tmp/_recompile.log "$error_file"
        fi
    done

    # Final check: even if we hit max iterations or Gemini failed on a later
    # iteration, previous iterations may have fixed things.
    # But if regression was detected and no prior iterations succeeded, fail hard.
    if [ "$regression_detected" = "true" ] && [ $iter -le 1 ]; then
        log_error "Regression detected on first iteration — no useful fixes to apply"
        return 1
    fi

    log_info "Running final compilation check..."
    local final_cc=0
    (cd backend && mvn test-compile -q > /dev/null 2>&1) || final_cc=$?

    if [ $final_cc -eq 0 ]; then
        log_success "Compilation passes after $iter iteration(s)"
        local final_tc=0
        (cd backend && mvn test > /tmp/_final_test.log 2>&1) || final_tc=$?
        if [ $final_tc -eq 0 ]; then
            log_success "All tests pass!"
            return 0
        else
            log_warning "Compilation OK but some tests still fail"
            return 0  # still return success — fixes were applied, PR should be created
        fi
    else
        log_warning "Compilation still broken after $iter iteration(s)"
        # Check if ANY source files were changed — if so, still create a PR with partial fixes
        local changed; changed=$(git diff --name-only 2>/dev/null | grep -cE '\.(java|kt|ts|tsx|js|jsx|py|go|rs|xml)$' || echo 0)
        if [ "$changed" -gt 0 ]; then
            log_info "$changed source file(s) were modified — creating PR with partial fixes"
            return 0  # partial fix is better than nothing
        fi
        return 1
    fi
}

# ──────────────────────────────────────────────────────────────
# CLI
# ──────────────────────────────────────────────────────────────
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    set -e          # strict mode only when running standalone
    set -o pipefail
    case "${1:-}" in
        extract-errors)
            [[ $# -eq 3 ]] || { log_error "Usage: $0 extract-errors <log> <out>"; exit 1; }
            extract_all_errors "$2" "$3"
            ;;
        apply-ai-fixes)
            [[ $# -eq 2 ]] || { log_error "Usage: $0 apply-ai-fixes <error_file>"; exit 1; }
            run_fix_pipeline "$2"
            ;;
        *)
            log_info "AI Code Fixing Utilities (generic)"
            log_info "  extract-errors <log> <out>     Extract errors from build log"
            log_info "  apply-ai-fixes <error_file>    Full AI-powered fix pipeline"
            ;;
    esac
fi
