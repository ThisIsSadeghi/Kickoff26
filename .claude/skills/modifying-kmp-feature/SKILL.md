---
description: Modify existing KMP features with spec-first workflow. Invoke with /modifying-kmp-feature.
allowed-tools: ["Task", "Read", "Write", "Edit", "Glob", "Grep", "Bash(./gradlew:*)", "Bash(mkdir:*)", "Bash(touch:*)", "Bash(rm -f /tmp/.claude-kmpilot-skill-active)", "AskUserQuestion"]
---

# Modifying KMP Features

Apply changes to existing features using spec-first workflow.

**Architecture Reference:** @../_shared/patterns.md

## Hook Marker (Required)

Before editing any feature files, activate the skill marker so the PreToolUse hook allows edits:
```bash
touch /tmp/.claude-kmpilot-skill-active
```
After completion (or on any early exit), remove it:
```bash
rm -f /tmp/.claude-kmpilot-skill-active
```

## Workflow

**Parse** → **Spec Check** → **Design Artifact Detection** → **Understand** → **Plan** → **Draft Spec** → [USER APPROVES] → **Activate marker** → **Implement** → **Validate** → **Update Spec** → **Remove marker** → Done

### Step 1: Parse Feature Name
Extract from request: "add sorting to productlist" → `productlist`
Validate: `ls feature/{featurename}/src/commonMain/kotlin/`

### Step 2: Spec Check
Load `.claude/docs/{featurename}/spec.md`

If missing, **stop and instruct the user**:

```
No spec found for '{featurename}'. Please run /audit-spec {featurename} first
to generate one, then re-invoke /modifying-kmp-feature.
```

Do NOT auto-invoke `/audit-spec` — skills do not call each other; the user controls the pipeline.

### Step 3: Design Artifact Detection

Check for a Stitch design blueprint:

1. **Check blueprint exists**: `.claude/docs/{featurename}/designs/{featurename}_blueprint.md`
2. **Check stitch-project.json**: `.claude/docs/_project/stitch-project.json` — read `features[featurename].blueprintConsumed`
3. **Determine mode**:

| Blueprint exists? | `blueprintConsumed` | Mode |
|-------------------|---------------------|------|
| Yes | `false` | **Design-aware mode** — blueprint drives UI implementation |
| Yes | `true` | Normal mode — blueprint already consumed |
| No | N/A | Normal mode — no design artifact |

If entering **design-aware mode**, log:
```
Design artifact detected: .claude/docs/{featurename}/designs/{featurename}_blueprint.md
Entering design-aware mode. Blueprint will drive UI implementation.
```

### Step 4: Understand Current Implementation
Read spec sections: Requirements, Architecture, State Management, Navigation

### Step 5: Plan Changes

**Platform Profile check (Rule 14)**: read the spec's **Platform Profile & Capabilities** field. If this change **introduces** a device capability or native view (map, camera, GPS, BLE, biometrics, WebView) that the feature didn't have, set/confirm the tag (`platform-capability` / `native-view` / `mixed`) — ask once with `AskUserQuestion` if ambiguous — and load `platform.md`. A change that stays `network` skips this.

Determine affected layers and load architecture as needed:
- Data changes: @../creating-kmp-feature/architecture/data.md
- UI changes: @../creating-kmp-feature/architecture/ui.md
- **Platform capability / native-view changes (Rule 14)**: @../creating-kmp-feature/architecture/platform.md
- Integration changes: @../creating-kmp-feature/architecture/integration.md
- Bottom-bar tab changes ("add/remove bottom-bar tab", "make this a tab", "show in bottom nav"): @../creating-kmp-feature/architecture/integration.md → "5. Bottom-Bar Tab (Optional)"

**Design-aware branch**: If in design-aware mode, read the blueprint's **Pre-Implementation Contract** section **and the `## Motion` table** (if present). Plan XTheme color updates (missing M3 roles) **and Typography Updates Required** (font swap + type-scale role overrides) first — both are app-global `:core:designsystem` edits. The DS `motion/` primitives already ship (verify present, reuse — see 1c); plan only feature-specific motion + any genuinely-missing DS primitive. Include the blueprint component tree, the Typography Scale `M3 Role` mapping, and the `## Motion` rows in the UI plan.

### Step 6: Draft Spec Changes
Propose updates using diff format:
```markdown
## Proposed Spec Changes: {featurename}
**Current Version:** X.Y.Z → **Proposed:** X.Y+1.Z

### Section N: {Name}
```diff
  existing content
+ added content
- removed content
```
### Rationale
{Why these changes are needed}
```

### Step 7: Review Gate (REQUIRED)
Present changes to user with:
- [ ] **Approve** - Proceed with implementation
- [ ] **Modify** - Request changes
- [ ] **Reject** - Do not proceed

**Never skip this step.**

### Step 8: Implement Changes
Follow patterns from @../_shared/patterns.md

For UI changes: Load @../using-design-system/references/component-mappings.md

**Strings (Rule 12)**: any new user-facing text → a key in the feature's `composeResources/values/strings.xml`, referenced via `stringResource(Res.string.*)` (or `UiText` for ViewModel-origin messages). Never add a hardcoded display literal. If the feature has no `strings.xml` yet, create it. See `@../_shared/patterns.md` → "Strings & Localization (Rule 12)".

**Platform capability / native view (Rule 14)**: when the change adds a device capability or native view, follow `@../creating-kmp-feature/architecture/platform.md`:
- Capability → `commonMain` DataSource interface returning `Either<DTO>` + per-platform actuals (android/ios/**desktop** fallback) + `expect/actual val platformModule` pulled into `{featurename}Module` via `includes(platformModule)`.
- Native view → `expect @Composable PlatformX` + `AndroidView`/`UIKitView`/desktop actuals under `components/` (Shape C); `{Feature}Content` stays pure Compose.
- Update `build.gradle.kts` per-platform deps (build-gradle-template → "Platform-specific dependencies").
- **iOS actual needs Swift** → write the `iosMain` interface/stub and **stop**: emit *"Run `/bridging-swift-kotlin` for `{Feature}Bridge`"* in the completion report. Do not write Swift; skills never call each other.
- Bump the spec's **Platform Profile** field to the new tag.

**Bottom-bar tab (optional)**: if the change is "add/remove bottom-bar tab", follow `@../creating-kmp-feature/architecture/integration.md` → "5. Bottom-Bar Tab (Optional)". This edits only the **app module** (`App.kt`, `navigation/TopLevelDestination.kt`, `composeApp/composeResources/`) — NOT the feature module itself; the feature stays independent. **Add**: append one `TopLevelDestination` enum entry (or scaffold the shell if this is the first tab); the tab label lives in `composeApp/src/commonMain/composeResources/values/strings.xml` (key `tab_{featurename}`), the icon as a vector XML in `composeApp/src/commonMain/composeResources/drawable/` — both referenced via `{PROJECT_NAMESPACE}.composeapp.generated.resources.Res`. **Remove**: delete the enum entry (the route remains a valid pushed destination). No registry exists — orphaned entries/labels/icons must be removed by hand.

**UI file layout (strict allowlist)**: when adding or moving composables, respect the rules in `@../_shared/patterns.md` ("UI File Organization"):
- `{Feature}Screen.kt` accepts only the allowlist names (`Screen`, `ScreenRoot`, and optionally `EmptyContent`); Loading/Failed route to the shared `AppLoadingState`/`AppErrorState` (`{PKG_PREFIX}.designsystem.app`) — never private shells
- Every other composable, including `{Feature}Content`, lives one-per-file under `presentation/ui/components/`
- Non-composable helpers live in `presentation/ui/{Feature}Utils.kt`, never under `components/`

**Previews (mandatory for new components)**: when this modification **adds a new component**, you must also:

1. **Check feature build.gradle.kts** for preview deps:
   ```kotlin
   sourceSets.commonMain.dependencies { implementation(libs.compose.ui.tooling.preview) }
   dependencies { androidRuntimeClasspath(libs.compose.ui.tooling) }
   ```
   If either is missing, add it as part of this modification.

2. **Generate a `@Preview` composable** in the same file as the new component, marked `private`, wrapped in `XTheme`, with realistic sample data. Use the canonical import `androidx.compose.ui.tooling.preview.Preview` (CMP 1.11.0+ — common). Never use the deprecated `org.jetbrains.compose.ui.tooling.preview.Preview`.

   See `@../creating-kmp-feature/architecture/ui.md` → "Previews" for the full pattern, including `@PreviewParameter` for multi-variant previews.

**Shared-state screen gate (ALWAYS — runs in normal mode AND design-aware mode, before any code change):** Re-read `.claude/docs/_project/stitch-project.json`. For each state where `features[{featurename}].states.{state} == true`, check `sharedStateScreens.{state}.codeImplemented`. If `false` or absent — execute 1d inline NOW before touching any feature file:
  1. Resolve `app/` dir from `stitch-project.json.designSystem.xthemePath` (replace filename, append `app/`).
  2. Read `.claude/docs/_shared/designs/extracted/tokens_{state}.md`.
  3. Rewrite `AppLoadingState.kt` (loading) or `AppErrorState.kt` (failed) to match the token inventory. Use only generic DS primitives; never import from `designsystem.app`; preserve existing function signature, `modifier` param, and KDoc.
  4. Set `sharedStateScreens.{state}.codeImplemented = true`, update `updatedAt`, write the file.
  5. Build: `./gradlew :core:designsystem:assembleAndroidMain`. Re-read and assert `codeImplemented == true` — if still not true, repeat from step 3.
  Never ask the user. Never skip. Never delegate.

**Design-aware branch**: If in design-aware mode, implement in this order:
1. **XTheme update** — Add all missing M3 roles from the blueprint's Pre-Implementation Contract to **both** `XLightColors` and `XDarkColors` in `XTheme.kt`. Verify build: `./gradlew :core:designsystem:assembleAndroidMain`
1b. **Typography update** — Read the blueprint's **Typography Updates Required** + `fonts.json`. Typography is app-global (lands in `:core:designsystem`). **Font swap** (only if a *Font swap* row exists): run `python3 .claude/skills/_shared/download_font.py --project-root {repo_root} --html .claude/docs/{featurename}/designs/extracted/stitch_success.html --manifest .claude/docs/{featurename}/designs/extracted/fonts.json` → downloads the `.ttf` set and prints the `Font(Res.font.*)` lines; replace `XFontFamily()`'s body in `XTheme.kt` with them (add `import androidx.compose.ui.text.font.FontVariation` for a variable route; follow the printed manual fallback on download failure). Verify build: `./gradlew :core:designsystem:assembleAndroidMain`. **Type-scale role overrides** are applied per-node in the feature (sub-step 3), not the theme. Skip 1b entirely when neither sub-table is present.
1c. **Motion files** (only if the blueprint has a `## Motion` table; skip for a static design). Motion needs no asset download — pure Compose in dedicated `motion/` files (see `@../_shared/motion.md`). The generic DS primitives **already ship** in `core/designsystem/.../motion/` (`XMotion.kt` + `expect/actual rememberReducedMotion()`, `Modifier.shimmer()`, `PulseDot`, `AmbientMeshBackground`, `BokehCanvas`, `Modifier.pulseGlow()`, `RevealOnAppear`) — **verify present, do not recreate**. Only if a "DS `motion/`" row needs a primitive the shipped set lacks (rare), add it (canonical names from motion.md; a new platform-dependent one needs `.android`/`.ios`/`.desktop` actuals) and build `./gradlew :core:designsystem:assembleAndroidMain :core:designsystem:desktopMainClasses`. **Feature-specific rows** land in `feature/{featurename}/.../presentation/ui/motion/{Feature}Motion.kt` in sub-step 3, **reusing** the shipped DS primitives with the row's magnitude passed as a **parameter**. Durations/easings via `XMotion` tokens (never ad-hoc `tween(<literal>)`); magnitudes copied verbatim from the `## Motion` table (never invented); gate every kept row with `rememberReducedMotion()`; never inline in `Screen.kt`/components; never implement interaction/hover motion.
1d. **Shared state screen code implementation (MANDATORY — never skip in design-aware mode)** — rewrites `AppLoadingState.kt` / `AppErrorState.kt` in `core/designsystem/.../app/` to match the approved Stitch design. The shared state screens carry their **own** design (one per project) and are **not** part of the feature blueprint — so if this step is dropped they silently keep the neutral `install.sh` default and the Stitch redesign never reaches code.

   For each state in `[loading, failed]` where `stitch-project.json.features[{featurename}].states.{state} == true` AND `sharedStateScreens.{state}.codeImplemented` **is not `true`** (i.e. it is `false` **or the key is absent** — legacy `stitch-project.json` files predate the field; **treat an absent key as `false` and implement**):
   1. Resolve the `app/` dir from `stitch-project.json.designSystem.xthemePath`: replace the filename with nothing, append `app/`. Example: `core/designsystem/src/commonMain/kotlin/com/example/designsystem/XTheme.kt` → `core/designsystem/src/commonMain/kotlin/com/example/designsystem/app/`.
   2. Read `.claude/docs/_shared/designs/extracted/tokens_{state}.md`.
   3. Rewrite `AppLoadingState.kt` (loading) or `AppErrorState.kt` (failed) to match the token inventory. Use only generic design-system primitives (`XCircularProgressIndicator`, `Placeholder`, `XButton`, `XIcon`, `XText`, etc.); never import from `designsystem.app` itself. Preserve the existing function signature, `modifier` param, and KDoc.
   4. Set `stitch-project.json.sharedStateScreens.{state}.codeImplemented = true` (add the key if it was absent). Update top-level `updatedAt`. Write the file.

   **Verify (gate — do NOT consider the modification complete until it passes):** re-read `stitch-project.json` and, for every selected state, assert `sharedStateScreens.{state}.codeImplemented == true`. If any selected state is still not `true`, the rewrite did not happen — STOP and complete it. Then build: `./gradlew :core:designsystem:assembleAndroidMain`.
2. **X-Component Constraint Check** — Collect the unique set of design system source files needed by the blueprint's Component Tree (one file may define many composables — e.g. `XButton.kt` defines `XButton`, `XOutlinedButton`, `XIconButton`, `XTextIconButton`, `XOutlinedIconButton`). Read each file in full and catalog **every composable defined in it**, not just the one the blueprint named. For each composable, extract:
   - `defaultMinSize` constraints (e.g. `XButton` enforces `minWidth=100.dp, minHeight=44.dp`)
   - Default parameter values that differ from what the blueprint intends (e.g. `XIconButton` defaults to a visible `surface` background)
   - Hardcoded internal padding that overrides `contentPadding` (e.g. `XTextField` hardcodes `top=10.dp, bottom=10.dp`)
   - Any internal `Modifier` applied via `.then(...)` that the caller cannot override

   Reading the whole file matters: the implementation may legitimately reach for a sibling composable in the same file (e.g. use `XOutlinedButton` instead of `XButton` for a pill), and you need its constraints too.

   For each conflict found, decide the resolution **before writing any code**:
   - Override via modifier: `Modifier.defaultMinSize(Dp.Unspecified)` to remove a min-size floor
   - Override via parameter: pass explicit `colors`, `shape`, or `contentPadding` to win over the default
   - Accept as architectural limitation: note it — do not fight it with hacks

3. **Component implementation** — Implement UI from the blueprint's Component Tree. Use the blueprint as the primary source, design screenshots as visual cross-reference only. Apply constraint resolutions from sub-step 2. Every text node uses `style = MaterialTheme.typography.{role}` (or an `XTextDefaults` preset) — never raw `fontSize`/`fontWeight`, except a *Type-scale role override* row (`…typography.{role}.copy(...)`). Never set `fontFamily` (global, wired in 1b). **Motion** (if the blueprint has a `## Motion` table): write feature-specific rows into `presentation/ui/motion/{Feature}Motion.kt`, call the DS `motion/` primitives (from 1c) for generic rows, gate each with `rememberReducedMotion()` — never inline, never interaction/hover motion.
   - **Images** follow each `images.json` entry's `delivery`: `bundled` → `Image(painter = painterResource({res_reference}))` (materialize the raster first if absent: run `python3 .claude/skills/_shared/download_assets.py --type images --feature {featurename} --project-root {repo_root} --html …` **without** `--manifest-only` — it downloads `bundled` only and skips `remote`); `remote` → `AsyncImage(url = {data_binding}, loadingResId = DesignSystemResources.drawable.ds_image_placeholder, …)` (design-system `AsyncImage`, `url=`) bound to a data-layer field, **never** the Stitch CDN URL — add/wire that field on the DTO/`*UiModel` and keep its Post-Implementation Checklist item.
4. **Post-Implementation Checklist** — Verify every item in the blueprint's Post-Implementation Checklist:
   - All XTheme missing roles added to BOTH schemes
   - Font swap (if any) applied: `XFontFamily()` rewired, `:core:designsystem` builds
   - Every text node uses a `MaterialTheme.typography.{role}` — no raw `fontSize`/`fontWeight` except recorded overrides
   - Every component in blueprint exists in implementation
   - Every Modifier in blueprint is present in code
   - All colors use `MaterialTheme.colorScheme.{role}` — no raw `Color()` hex
   - Component override sizes/colors applied
   - Every `images.json` `bundled` image rendered via `painterResource`; every `remote` image rendered via `AsyncImage(url = data field)` with its `data_binding` wired on the DTO/`*UiModel` (no Stitch CDN URL in code)
   - Every `## Motion` row implemented in a `motion/` file (feature → `presentation/ui/motion/`, generic → DS `motion/`), reduced-motion gated, no inline/interaction/hover motion (n/a if no `## Motion` table)

### Step 9: Validate Build
```bash
./gradlew :feature:{featurename}:assembleAndroidMain
./gradlew :feature:{featurename}:ktlintFormat
```

### Step 10: Update Specification
Apply APPROVED changes (don't regenerate). Add changelog:
```markdown
## Last Updated
- {YYYY-MM-DD} - {Brief description}
```
Version bump: Patch (X.Y.Z+1) for fixes, Minor (X.Y+1.0) for features

**Design-aware branch**: Also:
- Add UI Design section to spec referencing the blueprint and design screenshots
- Set `"blueprintConsumed": true` in `.claude/docs/_project/stitch-project.json` under `features[{featurename}]`

## Error Handling

Build errors: Load @../creating-kmp-feature/troubleshooting/index.md
Design system: Activate `/using-design-system`

## Completion Checklist

- [ ] Spec changes drafted and USER APPROVED
- [ ] Build passes
- [ ] Code formatted (ktlint)
- [ ] Spec updated with approved changes
- [ ] Changelog entry added
- [ ] Version bumped
- [ ] (Design-aware) blueprintConsumed set to true in stitch-project.json

## What's next

Emit this blockquote as the very last line of output:

---

> **Next step —** run `/clear` to free the context window (the spec + design blueprint are durable artifacts — the next skill re-reads them fresh, so clearing loses nothing), then `/feature-review {featurename}` to validate the changes — or `/verify-ui {featurename}` if you applied a design.
