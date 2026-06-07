# Spec Changelog Entry Template

Use this template when updating the spec after modifications.

## Format

Add this section at the top of the updated spec:

```markdown
## Last Updated
- {YYYY-MM-DD} - {Brief description of change}
- {previous entries...}
```

---

## Guidelines

### Brief Description Format

| Change Type | Format | Example |
|-------------|--------|---------|
| Added | "Added {capability}" | "Added sorting to product list" |
| Modified | "Updated {component/behavior}" | "Updated login error handling" |
| Fixed | "Fixed {issue}" | "Fixed navigation callback in orders" |
| Refactored | "Refactored {layer/component}" | "Refactored data layer to use new API" |

### Rules

- **One line per update** - Keep it concise
- **Date format**: YYYY-MM-DD
- **Preserve history** - Keep all previous entries below new one

---

## Complete Example

```markdown
## Last Updated
- 2025-01-04 - Added filtering by category and price range
- 2025-01-02 - Fixed loading state handling in ViewModel
- 2024-12-28 - Updated API endpoint to v2
- 2024-12-20 - Initial implementation
```

---

## Workflow

> Spec updates are **incremental edits**, not full regenerations
> (see `modifying-kmp-feature/SKILL.md` Step 9). Apply approved changes
> in-place to the existing spec.

1. **Before applying changes:**
   - Locate the existing "Last Updated" section in the current spec

2. **After applying approved changes:**
   - Add a new entry at the top of "Last Updated" with today's date
   - Keep previous entries below it in chronological order (newest first)
