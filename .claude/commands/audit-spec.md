---
description: Audit, generate, or compare specifications for existing KMP features
allowed-tools: ["Read", "Write", "Edit", "Glob", "Grep", "Bash(./gradlew:*)"]
---

# Audit Feature Specification

Audit, generate, or compare specifications for existing features.

**Architecture Reference:** @../skills/_shared/patterns.md
**Spec Template:** @../skills/_shared/spec-template.md

## Usage

```bash
/audit-spec {featurename}           # Generate spec from code
/audit-spec {featurename} --compare # Detect spec drift
```

## When to Use

| Scenario | Use? |
|----------|------|
| Legacy feature without spec | Yes |
| Check spec-code consistency | Yes (--compare) |
| New feature creation | No → `/creating-kmp-feature` |
| Modifying existing feature | No → `/modifying-kmp-feature` |

## Mode 1: Generate Spec (Default)

### Process

1. **Validate**: `ls feature/{featurename}/src/commonMain/kotlin/`
2. **Discover Package**: `grep "namespace" feature/{featurename}/build.gradle.kts`
3. **Analyze Implementation**:
   - `data/model/*.kt` - Data models
   - `data/datasource/*.kt` - DataSource interface + impl
   - `data/repository/*.kt` - Repository interface + impl
   - `presentation/*ViewModel.kt` - State management
   - `presentation/ui/*Screen.kt` - UI composition
   - `presentation/navigation/*.kt` - Routes and callbacks
   - `di/*Modules.kt` - DI bindings

4. **Check for PRD** (preserve WHY):
   ```bash
   cat .claude/docs/{featurename}/prd.md
   ```
   Copy: Goals, Non-Goals, Background & Rationale, Design Decisions

5. **Generate**: Create `.claude/docs/{featurename}/spec.md` using @../skills/_shared/spec-template.md

6. **Report** — emit the blockquote as the very last line of output:
   ```
   ## Specification Generated
   **Feature:** {featurename}
   **Output:** .claude/docs/{featurename}/spec.md

   ---

   > **Next step —** review `.claude/docs/{featurename}/spec.md` and fill any TODO sections, then run `/clear` to free the context window, followed by `/feature-review {featurename}` to validate the implementation against it.
   ```

## Mode 2: Compare/Drift Detection (--compare)

### Process

1. Load existing spec: `.claude/docs/{featurename}/spec.md`
2. Analyze current implementation (same as Mode 1)
3. Compare and generate drift report:

```markdown
## Spec Drift Report: {featurename}
**Spec Version:** {version}

| Category | Status |
|----------|--------|
| Data Models | ✅ In sync / ⚠️ N drifts |
| Interfaces | ✅ In sync / ⚠️ N drifts |
| State Management | ✅ In sync / ⚠️ N drifts |
| Navigation | ✅ In sync / ⚠️ N drifts |

### Drift Details
{Specific differences with file:line references}

### Proposed Spec Updates
{Diff format changes to apply}
```

## Notes

- Specs should be written BEFORE implementation (PRD-first)
- This command is for auditing existing code or documenting legacy features
- Generated specs have TODO markers for WHY sections
- Run with `--compare` periodically to detect drift
