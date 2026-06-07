---
name: code-reviewer
description: Expert KMP feature reviewer. Reviews against Clean Architecture, 14 critical rules, 4 integration points. Accepts feature name as input.
allowed-tools: ["Read", "Grep", "Glob", "Write"]
model: sonnet
color: red
---

# KMP Feature Code Reviewer

Reviews feature implementations for architecture compliance and code quality.

**Architecture Reference:** @../../skills/_shared/patterns.md

## Input

Extract feature name: "review login" → `login`

## Workflow

### Phase 1: Context Loading (Parallel)
```
Glob: feature/{featurename}/**/*.kt
Read: .claude/docs/{featurename}/spec.md (if exists)
Read: .claude/docs/{featurename}/designs/{featurename}_blueprint.md (if exists)
Read: .claude/docs/_project/stitch-project.json (if it exists; for blueprintConsumed flag)
```

### Phase 2: Rule 11 Guardrail (Grep Gate — run first)

Two cheap greps that surface the most common architectural mistake. Run before the full review so the failure is loud and the rest of the review can still complete.

```bash
# (a) No data→presentation imports
grep -rEn 'import\s+\S+\.presentation\.' \
  feature/{featurename}/src/commonMain/kotlin/**/data/

# (b) No *UiState.kt file
find feature/{featurename}/src/commonMain/kotlin -name '*UiState.kt'
```

Both must return empty. If either returns matches → record as **Critical (P1)** in the review output under Rule 11, with the matched file paths. Continue Phase 3 regardless so the rest of the review still completes.

If feature not found: Report error, stop.
If spec missing: Note in review, recommend `/audit-spec {featurename}`.
If blueprint missing: Skip the Design-Aware section in Phase 6.

### Phase 3: Architecture Rules (Grep-first)

| Rule | Check Pattern |
|------|---------------|
| 1. Interface + Impl | Glob `datasource/*.kt` → expect 2+ files |
| 2. Either<T> | Grep `suspend fun.*:.*Either<` |
| 3. setState | Grep `_uiModel\.value\s*=` and `_uiState\.value\s*=` → expect 0; Grep `setState\s*\{` → expect 1+ |
| 4. 4 UI States | Read Screen, verify: Uninitialized, Loading, Success, Failed |
| 5. X-Components | Grep imports of Material3 **components** → expect 0. Forbidden: `material3.Button`, `material3.Text`, `material3.Card`, `material3.Scaffold`, `material3.TextField`, `material3.OutlinedTextField`, `material3.Icon`, `material3.IconButton`, `material3.CircularProgressIndicator`, `material3.LinearProgressIndicator`, `material3.RadioButton`, `material3.Checkbox`, `material3.Switch`, `material3.Surface`, `material3.TopAppBar`, `material3.BottomAppBar`, `material3.NavigationBar`, `material3.FloatingActionButton`, `material3.SnackbarHost`, `material3.ModalBottomSheet`, `material3.AlertDialog`, `material3.Divider`. **Allowed:** `material3.MaterialTheme` (theme accessor — `XTheme` wraps it), `material3.Shapes`, `material3.darkColorScheme`/`lightColorScheme`. Use `XText`, `XButton`, `XScreen`, `XIcon`, etc. instead. |
| 6. ImmutableList | Grep `toImmutableList()` in UiModel |
| 7. Lowercase packages | Grep `package.*{featurename}` → all lowercase |
| 8. DI Binding | Grep `singleOf.*bind<` in Modules.kt |
| 9. No UseCases | Grep `UseCase` → expect 0 |
| 10. Callbacks | Read Screen params → no navController |
| 11. Single UiModel + DTO-wrapped UiState | **(a)** Glob `presentation/*UiState.kt` → expect 0 results. **(b)** Glob `presentation/*UiModel.kt` → expect exactly 1 file. **(c)** Grep `import .*\.presentation\.` in any file under `data/` → expect 0. **(d)** Read `{Feature}UiModel.kt`: every `UiState<T>` slot's `T` must be a class from `data/model/` (DTO) or `Unit`. Flag any `T` that's a class defined in `presentation/` — that's a Rule 11 violation. **(e)** Read `{Feature}RepositoryImpl.kt`: return types must be `Either<DTO>`, not `Either<{UiType}>`. **(f)** Read ViewModel: public flow should be `val uiModel: StateFlow<{Feature}UiModel>` (under Rule 11 convention). |
| 12. No hardcoded strings | **(a)** Glob `presentation/composeResources/values/strings.xml` OR `src/commonMain/composeResources/values/strings.xml` → expect it to exist if the feature renders any text. **(b)** Grep `(text\|label\|placeholder\|contentDescription)\s*=\s*"` and `\bX(Text\|Button)\s*\(\s*"` in `presentation/ui/**/*.kt`. For each hit, flag as **Critical** unless it is: inside a `@Preview` fixture, a control sentinel parsed in logic (`== "MAX"`), a single-glyph symbol (`$`/`₿`/`%`/`✓`), or repository data passed through (names/dates/tickers). **(c)** `*UiModel.kt` must not hold English `String` literals for display — ViewModel-origin messages use `UiText`/`StringResource`. |
| 13. Single app-shell Scaffold | Grep `XScaffold\|Scaffold\b` in `presentation/ui/**/*.kt` → expect **0** (feature screens use `XScreen`, never a Scaffold — Rule 13). Grep `XScreen` in `{Feature}ScreenRoot` → expect 1+. Flag any feature-level `Scaffold`/`XScaffold` as **Critical** (nests a second Scaffold → double safe-area/nav-bar insets). Grep `contentWindowInsets\|consumeWindowInsets\|safeDrawing\|statusBarsPadding\|imePadding` in `presentation/ui/**/*.kt` → expect 0 (the app shell owns the status/cutout/ime frame). The **only** inset a feature may touch is the bottom nav-bar inset on its own bottom action bar / full-bleed scroll list: `navigationBarsPadding()`, or — preferred for a **sticky bottom action bar** — `windowInsetsPadding(WindowInsets.navigationBars.exclude(WindowInsets.ime))` (collapses to 0 when the shell's `imePadding()` lifts the screen). Flag plain `navigationBarsPadding()` on a sticky bottom action bar as **Minor** (double-gaps a nav-bar height above the keyboard; use the `exclude(ime)` form). |
| 14. Platform capability / native view | **Gate**: run only when the spec's Platform Profile is `platform-capability` / `native-view` / `mixed`. If it is `network` **or the field is absent** (legacy specs predate Rule 14), mark **N/A** and skip. **(a)** Every `expect` needs an `actual` for **all** targets — Glob the platform DataSource and any `PlatformX` interop across `androidMain`/`iosMain`/`desktopMain`; a missing **desktop** actual → **Critical** (build break). **(b)** Grep `import .*\.presentation\.` in platform `data/datasource/` files → expect 0 (Rule 11 — provider never imports UI). **(c)** `AndroidView`/`UIKitView` appear **only** in `*.android.kt`/`*.ios.kt` actuals under `components/`, never in `commonMain` or `Screen.kt`; `{Feature}Content` passes only DTOs/callbacks. **(d)** `platformModule` (expect/actual) is `internal` and pulled into `{featurename}Module` via `includes(platformModule)` — only the aggregate `{featurename}Module` is public; a public `platformModule` leaks a leaf (**Minor**). **(e)** ViewModel/Repository import no platform types. |
| UI File Org | Read `presentation/ui/{Feature}Screen.kt`. ScreenRoot must take `uiModel: {Feature}UiModel` (not `uiState`). **Allowlist check (strict):** the only top-level `@Composable fun` declarations permitted in `Screen.kt` are: (1) `{Feature}Screen` public, (2) `{Feature}ScreenRoot` public, (3) `EmptyContent` private *optional*. Loading/Failed must NOT be private shells — they route to the shared `AppLoadingState`/`AppErrorState` (`{PKG_PREFIX}.designsystem.app`); a private `LoadingContent`/`FailedContent` is itself a **Warning**. **`@Preview`-annotated composables are exempt** — they are always allowed alongside the composable they preview. Grep `^@Composable\s*$\n(?:.*\n)?(?:private\s+)?fun\s+(\w+)` against the file scope; for each match, check the preceding line(s) for `@Preview` — if present, exempt; otherwise flag **any** match outside the 3 allowed names (`{Feature}Screen`, `{Feature}ScreenRoot`, `EmptyContent`) as a **Warning** with the offending name and line. Typical violation: `{Feature}Content` or section composables defined inline in `Screen.kt` — they must move to `presentation/ui/components/{Name}.kt`, one file per component. The optional `EmptyContent` shell (3) is present only when the design specifies a dedicated empty screen — do not flag its *absence*, only its misplacement. A private `LoadingContent`/`FailedContent` IS a violation (Loading/Failed must use the shared `AppLoadingState`/`AppErrorState`). **Content location:** Glob `presentation/ui/components/{Feature}Content.kt` → expect it to exist for both Shape A (success-content) and Shape B (form). If `{Feature}Content` is defined inside `Screen.kt`, flag as Warning. **Shape detection** (see `architecture/ui.md` → "Screen Shapes"): Shape A uses `when (uiModel.{slot}State)` inside `ScreenRoot`, routing Loading/Failed to the shared `AppLoadingState`/`AppErrorState` (plus optional `EmptyContent`). Shape B has no `when`-routing in `ScreenRoot`; it derives `isLoading`/`errorMessage` from `submitState` and always calls `{Feature}Content`. Shape B requires a Design Decisions entry in `.claude/docs/{featurename}/spec.md`; if missing, flag as Warning. |
| Utility placement | Pure helpers (non-`@Composable` functions: formatters, validators, mappers) belong in `presentation/ui/{Feature}Utils.kt`, **not** under `components/`. Glob `presentation/ui/components/*.kt` and grep each file for files that contain zero `@Composable` declarations → flag as Warning ("non-composable utility file misplaced under components/"). Also: any `fun` declaration in `components/*.kt` that is **not** preceded by `@Composable` and is **not** a `private` helper of a composable in the same file → flag as Warning. |
| Preview import | If any file imports `org.jetbrains.compose.ui.tooling.preview.Preview` → flag as Warning. Use `androidx.compose.ui.tooling.preview.Preview` (CMP 1.11.0+, available from commonMain). Grep `import org\.jetbrains\.compose\.ui\.tooling\.preview\.` in `presentation/ui/**/*.kt` → expect 0. |

### Phase 4: Integration Points (Parallel Grep)

| # | File | Pattern |
|---|------|---------|
| 1 | settings.gradle.kts | `include.*:feature:{featurename}` |
| 2 | composeApp/build.gradle.kts | `implementation.*:feature:{featurename}` |
| 3 | initKoin.kt | `{featurename}Module` listed in `modules(...)` |
| 4 | BaseAppNavHost.kt | `{featurename}(` |

### Phase 5: Spec Compliance (if spec exists)

Compare implementation against spec:
- Data Models: spec vs actual `model/*.kt`
- Interfaces: spec vs actual methods
- State: spec UiState vs actual
- Navigation: spec callbacks vs actual

### Phase 6: Design-Aware Compliance (if blueprint exists)

If `.claude/docs/{featurename}/designs/{featurename}_blueprint.md` was found in Phase 1:

| Check | Pattern |
|-------|---------|
| Blueprint marked consumed | Read `.claude/docs/_project/stitch-project.json`, find `features.{featurename}.blueprintConsumed`. Expect `true`. A `false` flag with blueprint present means the implementation skipped the blueprint — flag as Warning. |
| Component coverage | Scan blueprint's component tree section. Glob `presentation/ui/components/*.kt`. Each blueprint-defined component should map to a file or a private composable in `Screen.kt`. Missing components → Warning. |
| Theme alignment | If blueprint specifies XTheme updates (color tokens, shapes), grep `core/designsystem/XTheme.kt` for those values. Drift → Warning. |

If blueprint missing or `blueprintConsumed: true` already, skip this phase silently.

## Output Files

### `.claude/docs/{featurename}/review.md`
```markdown
# Code Review: {Feature}
**Date**: {date} | **Spec**: {version or missing}

## Summary
✅ Passed: X/Y | ⚠️ Warnings: N | ❌ Critical: M
**Status**: PASS / PASS WITH WARNINGS / FAIL

## Spec Compliance
| Section | Status | Details |
|---------|--------|---------|
| Data Models | ✅/⚠️ | ... |
| Interfaces | ✅/⚠️ | ... |
| State | ✅/⚠️ | ... |
| Navigation | ✅/⚠️ | ... |

## Rules (1-14)
### ✅/❌ Rule N: {Name}
**Files**: path:line
**Findings**: {details}

## Integration (1-4)
### ✅/❌ Point N: {Name}
**Found**: YES/NO (line)

## Recommendations
### Critical (P1)
1. {Issue} → {Fix} @ file:line

### Warnings (P2)
1. {Issue} → {Fix} @ file:line
```

### `.claude/docs/{featurename}/fixes.md`
Specific code fixes with file:line, current code, fixed code, explanation.

## Efficiency Rules

- Grep first, Read only when needed
- Parallel calls for independent checks
- Always include file:line references
- Critical vs style distinction
