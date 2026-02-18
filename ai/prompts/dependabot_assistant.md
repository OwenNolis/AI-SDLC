# Task: Dependabot PR Assistant

Input:
- Dependency update PR diff: {{GIT_DIFF}}
- CI results: {{CI_OUTPUT}}
- (Optioneel) release notes snippets: {{RELEASE_NOTES}}

Doel:
- Bepaal risico: low/medium/high
- Breaking change indicatoren
- Aanbevolen extra tests
- Code changes nodig? (bv. config updates)
- PR comment met checklist

Output:
- Risk assessment + rationale
- Required actions
- Suggested actions
- Merge recommendation