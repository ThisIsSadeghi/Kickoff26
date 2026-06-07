---
description: Review a KMP feature against architecture patterns and spec
allowed-tools: ["Task", "Read", "Glob", "Grep", "Write"]
---

# Review Feature Implementation

Review a KMP feature against Clean Architecture, 14 critical rules, and 4 integration points.

**Architecture Reference:** @../skills/_shared/patterns.md

## Usage

```bash
/feature-review {featurename}
```

## Process

1. **Validate**: `ls feature/{featurename}/src/commonMain/kotlin/`
2. **Spawn Agent**: Delegate to `code-reviewer` agent
3. **Generate Reports**: `.claude/docs/{featurename}/review.md` and `fixes.md`

## What Gets Checked

### Architecture Rules (14)
1. Interface + Impl pairs
2. Either<T> returns
3. setState usage
4. 4 UI states
5. X-components (Material3 *components* forbidden — `MaterialTheme.colorScheme/typography` access is allowed because `XTheme` wraps MaterialTheme)
6. ImmutableList
7. Lowercase packages
8. DI binding pattern
9. No UseCases
10. Callback parameters
11. Single UiModel + DTO-wrapped UiState (no `*UiState.kt`; `UiState<T>` wraps DTOs from `data/model/`; no `presentation` imports in `data/`)
12. No hardcoded user-facing strings — all display text via `stringResource(Res.string.*)` / `DesignSystemResources` / `UiText`; `composeResources/values/strings.xml` exists. Grep `(text|label|placeholder|contentDescription) = "` and `X(Text|Button)("` in `presentation/ui/`; every hit must resolve from resources. Allowed: `@Preview` fixtures, control sentinels, single-glyph symbols (`$`/`₿`/`%`/`✓`), repository data (names/dates/tickers).
13. Single app-shell Scaffold — feature screens use `XScreen`, never a `Scaffold`/`XScaffold`; no `contentWindowInsets`/`safeDrawing`/`statusBarsPadding`/`imePadding` in feature UI (the app shell owns them); the only inset a feature touches is the bottom nav-bar inset on its own bottom bar / scroll list.
14. Platform capability / native view (Rule 14) — only when Platform Profile is `platform-capability`/`native-view`/`mixed` (N/A if `network` or field absent): capability behind a `commonMain` DataSource → `Either<DTO>` with actuals for **all** targets incl. desktop; native view via `expect @Composable` (`AndroidView`/`UIKitView`) under `components/`; `platformModule` (expect/actual) pulled into `{featurename}Module` via `includes(platformModule)`; no platform types in ViewModel/Repository.

### Integration Points (4)
1. settings.gradle.kts
2. composeApp/build.gradle.kts
3. initKoin.kt
4. BaseAppNavHost.kt

### Spec Compliance (if spec exists)
- Data Models, Interfaces, State, Navigation

### Design-Aware Compliance (if blueprint exists)
- Blueprint present at `.claude/docs/{featurename}/designs/{featurename}_blueprint.md`
- `blueprintConsumed: true` in `.claude/docs/_project/stitch-project.json` under `features.{featurename}`
- A `false` flag with a blueprint present means implementation skipped the design pipeline

### UI File Organization
- `{Feature}Screen.kt` allowlist (nothing else): `{Feature}Screen`, `{Feature}ScreenRoot`, and optionally `EmptyContent` — Loading/Failed must route to the shared `AppLoadingState`/`AppErrorState` (`{PKG_PREFIX}.designsystem.app`), never private shells; `EmptyContent` appears only when the design specifies a dedicated empty screen
- Every other composable, **including `{Feature}Content`** and its sub-components, lives in `presentation/ui/components/{Name}.kt` — one file per component
- **Utilities** (non-`@Composable` helpers: formatters, validators) live in `presentation/ui/{Feature}Utils.kt`, never under `components/`
- **`@Preview` composables** live in the **same file** as the composable they preview (marked `private`), and are exempt from the allowlist
- Preview import must be `androidx.compose.ui.tooling.preview.Preview` (CMP 1.11.0+); the older `org.jetbrains.compose...` is deprecated
- Reference: `patterns.md` "UI File Organization" section

## Output

| Status | Meaning |
|--------|---------|
| **PASS** | All rules and integrations pass |
| **PASS WITH WARNINGS** | Minor issues, non-blocking |
| **FAIL** | Critical violations found |

## After Review

Reports are saved to:
- `.claude/docs/{featurename}/review.md` — full review
- `.claude/docs/{featurename}/fixes.md` — actionable fixes

Optional: run `/audit-spec {featurename} --compare` to check spec drift.

Pick the matching literal footer based on the review status and emit it as the very last line of output.

**If status is PASS:**

```
---

> **Next step —** run `/clear` to free the context window, then `/feature-test {featurename}` to generate comprehensive tests for the feature.
```

**If status is PASS WITH WARNINGS or FAIL:**

```
---

> **Next step —** run `/clear` to free the context window, then `/modifying-kmp-feature {featurename} apply fixes from @.claude/docs/{featurename}/fixes.md` to address the review findings.
```
