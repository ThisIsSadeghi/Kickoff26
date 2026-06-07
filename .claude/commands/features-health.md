---
description: Show health status for all feature modules
allowed-tools: ["Bash(ls:*)", "Glob", "Grep", "Read"]
model: haiku
---

# Feature Health Dashboard

Display health status for all feature modules.

## Usage

```bash
/features-health
```

## Process

1. **Discover Features**: `ls -d feature/*/build.gradle.kts`
2. **Check Each Feature**:
   - Spec: `.claude/docs/{feature}/spec.md` exists?
   - Tests: `*Test.kt` files in commonTest?
   - Review: `.claude/docs/{feature}/review.md` exists?

## Output

```markdown
## Feature Health Report

| Feature | Spec | Tests | Review | Actions |
|---------|------|-------|--------|---------|
| home | ✅ | ✅ 7 files | ✅ | - |
| profile | ✅ | ⚠️ 2 files | ❌ | /feature-review profile |
| login | ❌ | ❌ 0 files | ❌ | /audit-spec login |

### Summary
- **Total:** 3 features
- **With Spec:** 2/3
- **With Tests:** 2/3
- **With Review:** 1/3
```

## Health Criteria

| Check | ✅ Pass | ⚠️ Warning | ❌ Fail |
|-------|---------|------------|---------|
| Spec | exists | - | missing |
| Tests | 5+ files | 1-4 files | 0 files |
| Review | exists | - | missing |

## After Health Check

Find the biggest gap across all features (the row with the most ❌ entries), then emit the matching literal footer as the very last line of output.

**If most features are missing a spec:**

```
---

> **Next step —** run `/clear` to free the context window, then `/audit-spec {feature}` to generate the missing spec for one of the flagged features.
```

**If most features are missing tests:**

```
---

> **Next step —** run `/clear` to free the context window, then `/feature-test {feature}` to generate tests for one of the flagged features.
```

**If most features are missing a review:**

```
---

> **Next step —** run `/clear` to free the context window, then `/feature-review {feature}` to validate one of the flagged features.
```

**If all rows pass:**

```
---

> **Next step —** run `/clear` to free the context window, then `/coverage` to see the full coverage report across all features.
```
