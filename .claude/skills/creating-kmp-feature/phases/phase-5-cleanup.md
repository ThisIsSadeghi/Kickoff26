# Phase 5: Cleanup Ephemeral Artifacts

**Purpose**: Remove temporary planning files, keep spec.md as source of truth.

**Prerequisites**: All agents completed successfully, spec.md generated.

---

## Checklist

```
Cleanup Progress:
- [ ] Step 5.1: Verify spec.md exists
- [ ] Step 5.2: Guardrails (grep gate): Rule 11 + first-feature Welcome handoff
- [ ] Step 5.3: Remove ephemeral artifacts
- [ ] Step 5.4: Generate final report
```

---

## Step 5.1: Verify spec.md Exists

```bash
ls -la .claude/docs/{featurename}/spec.md
```

**If spec.md exists**: Proceed to cleanup

**If spec.md missing**: Do NOT proceed. Check integration agent output.

---

## Step 5.2: Guardrail Checks (Grep Gate)

Mechanical checks that the feature follows architectural conventions. All must pass.

```bash
# (a) No data→presentation imports (Rule 11)
grep -rEn 'import\s+\S+\.presentation\.' \
  feature/{featurename}/src/commonMain/kotlin/**/data/ \
  && echo "❌ Rule 11 violation: data layer imports from presentation" && exit 1

# (b) No *UiState.kt file (Rule 11)
find feature/{featurename}/src/commonMain/kotlin -name '*UiState.kt' \
  | grep . && echo "❌ Rule 11 violation: *UiState.kt found; collapse into *UiModel.kt" && exit 1

# (c) @Preview exists in UI files
grep -rn "@Preview" feature/{featurename}/src/commonMain/kotlin \
  | grep -v "^Binary" | grep . \
  || echo "⚠️ No @Preview composables found in feature/{featurename}. Add previews per patterns.md § Previews."

# (d) First-feature Welcome handoff completed (Integration Point 4a).
#     The install.sh placeholder must be gone once the FIRST feature is wired.
#     The build still compiles with a leftover Welcome, so this gate catches it.
find composeApp/src/commonMain/kotlin -name 'WelcomeScreen.kt' | grep . \
  && echo "❌ Welcome handoff incomplete: WelcomeScreen.kt still present — delete it and repoint startDestination (see architecture/integration.md § 4a)" && exit 1
grep -rn "WelcomeRoute" composeApp/src/commonMain/kotlin | grep . \
  && echo "❌ Welcome handoff incomplete: WelcomeRoute still referenced in the nav host — set startDestination = {Feature}Route and drop the WelcomeRoute composable" && exit 1
```

**If (a), (b), or (d) fails**: Stop. Surface the violation to the user. Do NOT proceed to artifact cleanup. For (d), re-run the integration agent's first-feature handoff (architecture/integration.md § 4a).

**If (c) finds no previews**: Surface the warning but do NOT block cleanup — previews are required but a missing preview does not break the build or architecture. The user may choose to add them via `/modifying-kmp-feature`.

**If all pass**: Proceed to Step 5.3.

---

## Step 5.3: Remove Ephemeral Artifacts

Remove temporary planning files:

```bash
rm -f .claude/docs/{featurename}/prd.md
rm -f .claude/docs/{featurename}/tasks.md
rm -f .claude/docs/{featurename}/task-*.md
```

### What Gets Deleted

| File | Purpose | Why Delete |
|------|---------|------------|
| `prd.md` | Planning document | Superseded by spec.md |
| `tasks.md` | Task summary | Work complete |
| `task-*.md` | Individual tasks | Work complete |

### What Remains

| File | Purpose | Permanent |
|------|---------|-----------|
| `spec.md` | Living specification | ✅ Source of truth |
| `review.md` | Code review results | ✅ If exists |
| `fixes.md` | Applied fixes | ✅ If exists |

---

## Step 5.4: Generate Final Report

```markdown
## Feature Complete: {FeatureName}

### Implementation Summary
✅ Data layer implemented
✅ UI layer implemented
✅ Integration complete
✅ Build passing + ktlint formatted

### Documentation
✅ Living spec: `.claude/docs/{featurename}/spec.md`
✅ Ephemeral artifacts cleaned

### Files Created

#### Feature Module
- `feature/{featurename}/build.gradle.kts`
- `feature/{featurename}/src/commonMain/kotlin/{PKG_PATH}/{featurename}/`
  - `data/model/*.kt`
  - `data/remote/*.kt`
  - `data/datasource/*.kt`
  - `data/repository/*.kt`
  - `presentation/*.kt`
  - `presentation/ui/*.kt`
  - `presentation/navigation/*.kt`
  - `di/*.kt`

#### Integration Points Modified
- `settings.gradle.kts`
- `composeApp/build.gradle.kts`
- `{INIT_KOIN_PATH}`
- `{NAV_HOST_PATH}`

### What's next
- Test navigation: `navController.navigate({FeatureName}Route)`
- Review spec: `.claude/docs/{featurename}/spec.md`

---

> **Next step —** run `/clear` to free the context window (the spec at `.claude/docs/{featurename}/spec.md` and the design blueprint are durable artifacts — the next skill re-reads them fresh, so clearing loses nothing), then `/feature-review {featurename}` to validate against Clean Architecture guidelines{, or `/verify-ui {featurename}` to audit against the Stitch design if this feature was built design-aware}.
```

---

## Output

Feature creation workflow complete:
- All code implemented
- Build passing
- spec.md is the permanent source of truth
- Ephemeral artifacts cleaned up
- Feature ready for testing
