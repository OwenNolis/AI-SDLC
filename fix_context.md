# AI-SDLC Code Fixing Context

## Repository Structure
This is an AI-driven SDLC project with:
- Backend: Spring Boot 4.x with Maven
- Frontend: React with Vite, TypeScript, Jest
- AI Tools: Node.js scripts for test generation

## Current Error Analysis
## Maven Compilation Errors
```
[INFO] BUILD FAILURE
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/test/java/be/ap/student/config/TestRestTemplateConfig.java:[14,17] cannot find symbol
[ERROR]   symbol:   method setRootUri(java.lang.String)
[ERROR]   location: variable template of type org.springframework.boot.resttestclient.TestRestTemplate
[ERROR] 1 error
```

## NPM/Frontend Errors

## Recent Changes
f5050b4 Merge pull request #55 from OwenNolis/ai-fix/auto-fixes-20260303-104411
1527a5e [ai-fix] Automated code fixes for SDLC flow issues
69f5ebd Add final fixes for AI code fixer
371e8e3 Fix AI Code Fixes workflow - use proper Spring Boot 4+ TestRestTemplate configuration
65305fa Clean up test configuration - tests are intentionally broken to showcase AI fixes workflow
