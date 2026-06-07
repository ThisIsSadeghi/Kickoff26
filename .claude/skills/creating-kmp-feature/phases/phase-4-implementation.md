# Phase 4: Orchestrated Implementation

**Purpose**: Invoke specialized agents to implement the feature layers.

**Prerequisites**: PRD and tasks confirmed by user.

---

## Checklist

```
Implementation Progress:
- [ ] Step 4.0: Read Platform Profile → select agent set
- [ ] Step 4.1: Choose execution strategy (Sequential or Parallel)
- [ ] Step 4.2: Invoke specialized agents
      ↳ (design-aware only) passthrough: icons · images · XTheme · typography · motion · shared-state screens (4d)
- [ ] Step 4.2v: (design-aware only) Verify shared-state screens — re-read stitch-project.json; assert sharedStateScreens.{state}.codeImplemented == true for EVERY selected state (else STOP and run 4d)
- [ ] Step 4.3: Verify spec generation
- [ ] Step 4.4: Platform follow-ups (iOS-Swift bridge route, if flagged)
```

---

## Step 4.0: Agent Set by Platform Profile (Rule 14)

Read the PRD's **Platform Profile & Capabilities** section (set in Phase 2, Step 2.1b). The tag selects which agents run:

| Tag | data-layer-agent | **platform-agent** | ui-layer-agent | integration-agent |
|-----|:---:|:---:|:---:|:---:|
| `network` | ✅ REST | — | ✅ | ✅ |
| `platform-capability` | — | ✅ provider | ✅ | ✅ (+`platformModule`) |
| `native-view` | — | ✅ if a capability backs the view¹ | ✅ (+ `expect/actual` composable) | ✅ (+`platformModule` **only if** ¹ a provider exists) |
| `mixed` | ✅ REST | ✅ provider | ✅ | ✅ (+`platformModule`) |

¹ A pure display view with no device data (e.g. a static WebView) may need no provider — then `platform-agent` is skipped and `ui-layer-agent` writes only the `expect/actual` composable.

**Division of labor on the platform path** (no overlap):
- **platform-agent** (provider-only): `commonMain` DataSource interface + per-platform `actual` classes (android/ios/**desktop**) + `expect/actual val platformModule`. Writes **no** `@Composable`.
- **ui-layer-agent**: the `expect @Composable PlatformX` + `AndroidView`/`UIKitView`/desktop-fallback actuals under `components/`, plus the normal ViewModel/UiModel/Screen. Loads [architecture/platform.md](../architecture/platform.md) → "Pattern C".
- **integration-agent**: pulls `platformModule` into `{featurename}Module` via `includes(platformModule)`, lists `{featurename}Module` in `initKoin`'s `modules(...)`, and wires any `androidContext()` the Android actual needs.

**Module-scaffold owner (CRITICAL — build breaks if no one does it)**: `feature/{featurename}/build.gradle.kts` + the module dir structure are normally created by `data-layer-agent`. When `data-layer-agent` is **skipped** (pure `platform-capability` / `native-view`), the **first agent in the set** scaffolds the module instead, from [build-gradle-template.md](../architecture/build-gradle-template.md):

| Profile | Module-scaffold owner |
|---------|------------------------|
| `network`, `mixed` | `data-layer-agent` (unchanged) |
| `platform-capability`, `native-view` **with** a capability | `platform-agent` |
| `native-view` **without** a capability (no provider) | `ui-layer-agent` |

Tell that agent explicitly: *"You are the module-scaffold owner — create `build.gradle.kts` from the template first."* In **parallel** mode the scaffold owner must finish (or the orchestrator must pre-create `build.gradle.kts`) before the other agent edits it, to avoid a write race.

Pass the **tag + chosen sourcing option + module-scaffold-owner flag** to every agent you invoke below.

---

## Step 4.1: Execution Strategy

Ask user for preference:

| Strategy | Flow | When to Use |
|----------|------|-------------|
| **Sequential** | Data → UI → Integration | Safer, traditional |
| **Parallel** (Recommended) | Data + UI simultaneously → Integration | Faster |

---

## Step 4.2: Invoke Specialized Agents

### Design-Aware Blueprint Passthrough

If in **design-aware mode** (Phase 1 detected an unconsumed blueprint):

1. **Before UI agent**: Read the blueprint's **Pre-Implementation Contract** → extract XTheme missing roles **and the Typography Updates Required** (font swap + type-scale role overrides). Also read the manifests at:
   - `.claude/docs/{featurename}/designs/extracted/icons.json` (Material Symbols, from `/ui-designer` sub-step 5)
   - `.claude/docs/{featurename}/designs/extracted/images.json` (`<img>` assets, from `/ui-designer` sub-step 6)
   - `.claude/docs/{featurename}/designs/extracted/fonts.json` (design typeface, from `/ui-designer` sub-step 6b)

   Every entry in both manifests has `download_status: "pending"`.
2. **Materialize Material Symbols XML drawables** by running the shared downloader **without** `--manifest-only` so it actually downloads, applies the JetBrains-required KMP cleanup pass, extends `DesignSystemResources.kt` for any chrome additions, and inline-migrates any stale `feature/X/drawable/{ident}.xml` plus their Kotlin imports for icons that just promoted from domain to chrome:

   ```bash
   python3 .claude/skills/_shared/download_assets.py \
     --type icons \
     --feature {featurename} \
     --project-root {repo_root} \
     --html .claude/docs/{featurename}/designs/extracted/stitch_success.html \
     [--html .claude/docs/_shared/designs/extracted/stitch_loading.html  if needsLoading] \
     [--html .claude/docs/_shared/designs/extracted/stitch_failed.html   if needsFailed] \
     [--html .claude/docs/{featurename}/designs/extracted/stitch_empty.html if needsEmpty]
   ```

   - **Pass `--html` for the same selected states** the manifest covers (`needsLoading`/`needsFailed`/`needsEmpty` flags from `stitch-project.json.features[{featurename}].states`).
   - **Why before XTheme**: icons are a deterministic script with no LLM reasoning; running it first surfaces network/404 failures early before any UI work begins. If any download fails, the script reports `http-404` etc. in its summary — fix the failing icon name in the design, re-run `/ui-designer` to refresh the manifest, then retry.
   - **What the script does in full mode** (matches the manifest's predictions):
     - Downloads each XML from `google/material-design-icons` master, applies the cleanup pass (strip `android:tint`, strip `android:autoMirrored`, translate **every** `@android:color/*` reference — in any color attribute — to its literal ARGB hex), writes to the path the manifest declared.
     - For chrome icons: extends `core/designsystem/.../DesignSystemResources.kt` idempotently (`val {ident} = Res.drawable.{ident}` inside `object drawable`).
     - For promoted icons (those whose `users` set includes another feature): deletes the stale `feature/{other}/.../drawable/{ident}.xml` AND rewrites every `.kt` under `feature/{other}/src/` from `Res.drawable.{ident}` → `DesignSystemResources.drawable.{ident}` (imports fixed). Doc-artifact promotion of other features' manifests was already done by `/ui-designer`; this step just brings their source in sync.
   - **Idempotent**: existing XMLs are skipped, existing `DesignSystemResources` entries are skipped, Kotlin files that no longer reference `Res.drawable.{ident}` are skipped. Safe to re-run after a failed mid-flight.
   - After this step, run `./gradlew :core:designsystem:assembleAndroidMain` to confirm `DesignSystemResources.kt` additions compile cleanly.
3. **Materialize `<img>` assets** by running the same downloader with `--type images`:

   ```bash
   python3 .claude/skills/_shared/download_assets.py \
     --type images \
     --feature {featurename} \
     --project-root {repo_root} \
     --html .claude/docs/{featurename}/designs/extracted/stitch_success.html \
     [--html .claude/docs/_shared/designs/extracted/stitch_loading.html  if needsLoading] \
     [--html .claude/docs/_shared/designs/extracted/stitch_failed.html   if needsFailed] \
     [--html .claude/docs/{featurename}/designs/extracted/stitch_empty.html if needsEmpty]
   ```

   - Same skill-ownership model as icons, but **only `delivery: "bundled"` entries are materialized**: the script downloads each from its Stitch CDN URL, detects extension from `Content-Type` (PNG / JPEG / WebP), places files at the manifest-declared path, extends `DesignSystemResources.kt` for chrome assets, inline-migrates stale references. **`delivery: "remote"` entries are skipped entirely** (no download, no drawable, no `DesignSystemResources` entry) — they render at runtime via `AsyncImage`. The script honors any `delivery_locked: true` choice the user confirmed in `/ui-designer`.
   - Idempotent: existing files are skipped via `skip-exists`.
   - **Rendering follows each entry's `delivery`** (blueprint already emits this):
     - `bundled` → `Image(painter = painterResource({res_reference}))` exactly as declared.
     - `remote` → `AsyncImage(url = {data_binding}, loadingResId = DesignSystemResources.drawable.ds_image_placeholder, …)` (design-system `AsyncImage`, `url=`). Bind `url` to a data-layer field — **never** the Stitch CDN URL. Each remote image carries a **Post-Implementation Checklist** item to add/wire that URL field on the relevant DTO/`*UiModel` (the data-layer agent exposes it from the PRD/API; Rule 11 still holds). If the runtime URL is not yet available, leave the binding as the suggested field name and keep the checklist item open.
4. **XTheme update**: Add all missing M3 roles from the contract to **both** `XLightColors` and `XDarkColors` in `XTheme.kt`. Verify build: `./gradlew :core:designsystem:assembleAndroidMain`
4b. **Typography update**: Read the blueprint's **Typography Updates Required** (and `fonts.json`). Typography is app-global — these edits land in `:core:designsystem`, like the XTheme color step.
   - **Font swap** (only if the contract has a *Font swap* row): materialize the typeface and rewire the theme:
     ```bash
     python3 .claude/skills/_shared/download_font.py \
       --project-root {repo_root} \
       --html .claude/docs/{featurename}/designs/extracted/stitch_success.html \
       --manifest .claude/docs/{featurename}/designs/extracted/fonts.json
     ```
     This downloads the `.ttf` set into `core/designsystem/.../composeResources/font/` and prints the exact `Font(Res.font.*)` lines + any required imports. Replace `XFontFamily()`'s body in `XTheme.kt` with the printed `FontFamily(...)` (add `import androidx.compose.ui.text.font.FontVariation` when the route is variable). On a download failure the script prints a manual fallback — follow it. Re-run is idempotent (existing `.ttf` skipped).
   - **Type-scale role overrides** (only if the contract lists any): these are applied per-node in the **feature** by the UI agent as `style = MaterialTheme.typography.{role}.copy(...)` — pass the override table to the UI agent in step 6. No theme edit.
   - When neither sub-table is present, the design font matches the theme and all nodes use stock roles → **skip 4b entirely**.
   - Verify build after a font swap: `./gradlew :core:designsystem:assembleAndroidMain`
4c. **Motion files** (only if the blueprint has a `## Motion` table; skip entirely for a static design). Motion needs **no** asset download — it is pure Compose code in dedicated `motion/` files (see [`_shared/motion.md`](../../_shared/motion.md)). Two parts:
   - **The generic DS motion primitives already ship in the template** at `core/designsystem/.../motion/` (`XMotion.kt` + `expect/actual rememberReducedMotion()`, `Modifier.shimmer()`, `PulseDot`, `AmbientMeshBackground`, `BokehCanvas`, `Modifier.pulseGlow()`, `RevealOnAppear`). **Verify they're present** (`ls core/designsystem/src/commonMain/kotlin/**/designsystem/motion/`); for any `## Motion` row targeting "DS `motion/`" whose primitive is **missing** from the shipped set (rare), add it there using the canonical names/signatures from `_shared/motion.md` — if it needs `rememberReducedMotion()` it's already the `expect/actual`; a new platform-dependent primitive needs `.android`/`.ios`/`.desktop` actuals or the build breaks. Build android **and** desktop only when you added a new primitive: `./gradlew :core:designsystem:assembleAndroidMain :core:designsystem:desktopMainClasses`. **Do not recreate primitives that already exist.**
   - **Feature-specific motion** (rows targeting `feature motion/`) is written by the UI agent into `feature/{featurename}/.../presentation/ui/motion/{Feature}Motion.kt` — pass the `## Motion` table (incl. the **Magnitude** column) to it in step 6. The agent **reuses** the shipped DS primitives, passing each row's magnitude as a **parameter** (e.g. `PulseDot(scaleTo = 1.2f, minAlpha = 0.5f)`); durations/easings via `XMotion` tokens (never ad-hoc `tween(<literal>)`); magnitudes copied verbatim from the table (never invented); never inline motion in `Screen.kt`/components; every kept row gated by `rememberReducedMotion()`; no interaction/hover motion.
4d. **Shared state screen code implementation (MANDATORY — orchestrator-owned; never delegate to an agent, never skip in design-aware mode)** — rewrites `AppLoadingState.kt` / `AppErrorState.kt` in `core/designsystem/.../app/` to match the approved Stitch design. The shared state screens carry their **own** design (one per project) and are **not** part of the feature blueprint — so if this step is dropped they silently keep the neutral `install.sh` default and the Stitch redesign never reaches code. This is the single most-skipped step; the Step 4.2v gate exists to catch a miss.

   For each state in `[loading, failed]` where `features[{featurename}].states.{state} == true` AND `sharedStateScreens.{state}.codeImplemented` **is not `true`** (i.e. it is `false` **or the key is absent** — legacy `stitch-project.json` files predate the field; **treat an absent key as `false` and implement**):
   1. Resolve the `app/` dir from `stitch-project.json.designSystem.xthemePath`: replace the filename with nothing, append `app/`. Example: `core/designsystem/src/commonMain/kotlin/com/example/designsystem/XTheme.kt` → `core/designsystem/src/commonMain/kotlin/com/example/designsystem/app/`.
   2. Read `.claude/docs/_shared/designs/extracted/tokens_{state}.md`.
   3. Rewrite `AppLoadingState.kt` (loading) or `AppErrorState.kt` (failed) to match the token inventory. Use only generic design-system primitives (`XCircularProgressIndicator`, `Placeholder`, `XButton`, `XIcon`, `XText`, etc.); never import from `designsystem.app` itself. Preserve the existing function signature, `modifier` param, and KDoc.
   4. Set `stitch-project.json.sharedStateScreens.{state}.codeImplemented = true` (add the key if it was absent). Update top-level `updatedAt`. Write the file.

   **Verify (Step 4.2v gate — do NOT proceed past Phase 4 until it passes):** re-read `stitch-project.json` and, for every selected state, assert `sharedStateScreens.{state}.codeImplemented == true`. If any selected state is still not `true`, the rewrite did not happen — STOP and complete it. Then build: `./gradlew :core:designsystem:assembleAndroidMain`.
5. **X-Component Constraint Check**: Collect the unique set of design system source files needed by the blueprint's Component Tree (one file may define many composables — e.g. `XButton.kt` defines `XButton`, `XOutlinedButton`, `XIconButton`, `XTextIconButton`, `XOutlinedIconButton`). Read each file in full and catalog **every composable defined in it**, not just the one the blueprint named. For each composable, extract:
   - `defaultMinSize` constraints (e.g. `XButton` enforces `minWidth=100.dp, minHeight=44.dp`)
   - Default parameter values that differ from the blueprint's intent (e.g. `XIconButton` defaults to a visible `surface` background)
   - Hardcoded internal padding that overrides `contentPadding` (e.g. `XTextField` hardcodes `top=10.dp, bottom=10.dp`)
   - Any internal `Modifier` applied via `.then(...)` that the caller cannot override

   Reading the whole file matters: the UI agent may legitimately reach for a sibling composable in the same file, and it needs those constraints too.

   For each conflict, decide the resolution before the UI agent writes any code:
   - Override via modifier: `Modifier.defaultMinSize(Dp.Unspecified)`
   - Override via parameter: explicit `colors`, `shape`, or `contentPadding`
   - Accept as architectural limitation: note it in the agent prompt

   **Pass the conflict list to the UI agent** as additional context alongside the blueprint.
6. **Pass blueprint to UI agent**: Include the blueprint path, design screenshots, constraint conflict list, **the now-materialized icons and images manifests, the Typography Scale `M3 Role` mapping + any *Type-scale role overrides*, and the `## Motion` table (when present)** as context. For motion: the DS generic primitives are already created (step 4c); the agent writes only the feature-specific rows into `presentation/ui/motion/{Feature}Motion.kt`, calls the DS primitives for generic rows, gates every kept row with `rememberReducedMotion()`, and implements **no** interaction/hover motion. The UI agent emits every text node as `style = MaterialTheme.typography.{role}` (or an `XTextDefaults` preset) — never raw `fontSize`/`fontWeight` except where an override row applies (then `…typography.{role}.copy(...)`). The font itself is already wired globally in step 4b — the agent never sets `fontFamily`. The blueprint's Component Tree references icons and images by `res_reference`/`delivery` (e.g. `Res.drawable.qr_code_scanner`, `DesignSystemResources.drawable.arrow_back` for icons; for images: `bundled` → `Res.drawable.{name}`, `remote` → an `AsyncImage(url = {data_binding})` slot); the UI agent emits `XIcon(painter = painterResource({res_reference}))` for icons, `Image(painter = painterResource({res_reference}))` for `bundled` images, and `AsyncImage(url = {data_binding}, loadingResId = DesignSystemResources.drawable.ds_image_placeholder)` for `remote` images — exactly as declared. The blueprint's Component Tree is the primary source for UI implementation; design screenshots are visual cross-reference only.

### Option A: Sequential Execution

#### Step 1: Data Layer
```
Invoke data-layer-agent with:
- Feature name: {featurename}
- Task files: .claude/docs/{featurename}/task-*-data-*.md
- Project context:
  - PKG_PREFIX, PKG_PATH
  - CORE_COMMON_PKG, CORE_DATA_PKG
  - CORE_MODULES, CORE_DESIGNSYSTEM_PKG
- Expected: Data layer complete + build validation
```

**Wait for completion** → Verify success

> For a **pure `platform-capability` / `native-view`** feature, **skip Step 1** (no REST) and run Step 1b instead. For `mixed`, run both Step 1 and Step 1b.

#### Step 1b: Platform Layer (tag ≠ `network`)
```
Invoke platform-agent with:
- Feature name: {featurename}
- Platform Profile tag + chosen sourcing option (from PRD Step 2.1b)
- Capabilities to implement (e.g. current-location GPS)
- Project context: PKG_PREFIX, PKG_PATH, CORE_COMMON_PKG, CORE_MODULES
- Expected: commonMain DataSource interface + actuals (android/ios/desktop)
  + expect/actual val platformModule; provider-only (no composables);
  flags any iOS-Swift-bridge follow-up
```

**Wait for completion** → Verify success. If the agent flagged an iOS-Swift dependency, carry it to Step 4.4.

#### Step 2: UI Layer
```
Invoke ui-layer-agent with:
- Feature name: {featurename}
- Task files: .claude/docs/{featurename}/task-*-ui-*.md
- Project context:
  - PKG_PREFIX, PKG_PATH
  - CORE_COMMON_PKG, CORE_DESIGNSYSTEM_PKG
- Design-aware context (if applicable):
  - Blueprint: .claude/docs/{featurename}/designs/{featurename}_blueprint.md
  - Success screenshot: .claude/docs/{featurename}/designs/{featurename}.png
  - State coverage: read `features[{featurename}].states` from `.claude/docs/_project/stitch-project.json`.
    Include shared screenshots only for selected states (`.claude/docs/_shared/designs/loading.png` if `states.loading`, `.claude/docs/_shared/designs/failed.png` if `states.failed`).
    Include `.claude/docs/{featurename}/designs/{featurename}_empty.png` if `states.empty`. Skipped states have no screenshot; the blueprint marks them "Skipped" so the agent uses generic handling.
- Localization (Rule 12): create `composeResources/values/strings.xml`; ALL display text via `stringResource(Res.string.*)` — no hardcoded literals. If a blueprint is present, use its String Inventory keys.
- Motion (design-aware): if the blueprint has a `## Motion` table, implement each feature-specific row in `presentation/ui/motion/{Feature}Motion.kt`, call the DS `motion/` primitives (already created in step 4c) for generic rows, gate every row with `rememberReducedMotion()`, never inline in `Screen.kt`/components, never implement interaction/hover motion. See `_shared/motion.md`.
- Native-view (Rule 14, tag = `native-view`/`mixed`): write the `expect @Composable PlatformX` + `AndroidView`/`UIKitView`/desktop-fallback actuals under `components/`; `{Feature}Content` calls it and stays pure Compose. Load architecture/platform.md → "Pattern C". Consume the DataSource interface from platform-agent (do NOT write the provider).
- Expected: UI layer complete (incl. strings.xml) + build validation
```

**Wait for completion** → Verify success

#### Step 3: Integration
```
Invoke integration-agent with:
- Feature name: {featurename}
- Task files: .claude/docs/{featurename}/task-*-integration-*.md
- Project context:
  - PKG_PREFIX, PKG_PATH, PROJECT_NAMESPACE
  - CORE_COMMON_PKG, CORE_DATA_PKG, CORE_DESIGNSYSTEM_PKG
  - INIT_KOIN_PATH, NAV_HOST_PATH, CORE_MODULES
- Bottom-bar tab: read the PRD Navigation section — if the feature is a top-level tab, pass its label/icon/order (Integration Point 5); otherwise it is a pushed screen (skip point 5). **Contradiction check**: if the design blueprint Component Tree contains a tab nav bar note (`[App-shell chrome — Integration Point 5...]`) BUT the PRD says "pushed screen", STOP — do not proceed. Fix the PRD Navigation section to say "top-level tab" first. The design is authoritative; a pushed-screen default in the PRD is the error.
- **First-feature (Welcome) handoff (MANDATORY for the first feature)**: as part of Integration Point 4, check both markers — `WelcomeScreen.kt` exists under `composeApp/src/commonMain/kotlin/**` AND `{NAV_HOST_PATH}` has `startDestination = WelcomeRoute`. If both, replace `startDestination` with `{Feature}Route`, drop the `composable<WelcomeRoute> { WelcomeScreen() }` line + its imports, and `rm -f` the `WelcomeScreen.kt` file. See architecture/integration.md → "4a. First-feature (Welcome) Handoff".
- Platform module (Rule 14, tag ≠ `network`): pull `platformModule` (expect/actual) into `{featurename}Module` via `includes(platformModule)` and provide `androidContext()` if an Android actual needs it
- Expected: integration points 1–4 (+ point 5 if a tab) + first-feature Welcome handoff (if applicable) + full build + ktlint + spec.md
```

**Wait for completion** → Verify success

---

### Option B: Parallel Execution (Recommended)

#### Step 1: Launch Data + UI Agents in Parallel

> **Platform features (tag ≠ `network`)**: launch **platform-agent** alongside ui-layer-agent (and data-layer-agent too, only for `mixed`). The platform agent (provider) and ui-layer-agent (composable + ViewModel) touch disjoint files, so they parallelize cleanly. Pass each the Platform Profile tag + sourcing option. ui-layer-agent gets the DataSource interface name so its wiring matches.

**In ONE message**, invoke the agents in this feature's set (per Step 4.0) simultaneously — 2 for `network`, 2–3 for platform profiles:

```
1. data-layer-agent (network / mixed only) with:
   - Feature name: {featurename}
   - Project context: PKG_PREFIX, PKG_PATH, CORE_COMMON_PKG,
     CORE_DATA_PKG, CORE_MODULES, CORE_DESIGNSYSTEM_PKG

2. ui-layer-agent with:
   - Feature name: {featurename}
   - Project context: PKG_PREFIX, PKG_PATH, CORE_COMMON_PKG,
     CORE_DESIGNSYSTEM_PKG
   - Design-aware context (if applicable):
     - Blueprint: .claude/docs/{featurename}/designs/{featurename}_blueprint.md
     - Success screenshot: .claude/docs/{featurename}/designs/{featurename}.png
     - State coverage: read `features[{featurename}].states` from `.claude/docs/_project/stitch-project.json` and include shared/empty screenshots only for selected states (loading/failed live under `.claude/docs/_shared/designs/`; empty under `.claude/docs/{featurename}/designs/{featurename}_empty.png`). Skipped states have no screenshot.
   - Localization (Rule 12): create `composeResources/values/strings.xml`; ALL display text via `stringResource(Res.string.*)` — no hardcoded literals. If a blueprint is present, use its String Inventory keys.
   - Motion (design-aware): if the blueprint has a `## Motion` table, implement each feature-specific row in `presentation/ui/motion/{Feature}Motion.kt`, call the DS `motion/` primitives (created in step 4c) for generic rows, gate every row with `rememberReducedMotion()`, never inline, never implement interaction/hover motion. See `_shared/motion.md`.
   - Native-view (Rule 14, tag = `native-view`/`mixed`): write the `expect @Composable PlatformX` + `AndroidView`/`UIKitView`/desktop-fallback actuals under `components/`; consume platform-agent's DataSource interface; load architecture/platform.md → "Pattern C".

3. platform-agent (tag ≠ `network`) with:
   - Feature name + Platform Profile tag + sourcing option
   - Capabilities to implement
   - Project context: PKG_PREFIX, PKG_PATH, CORE_COMMON_PKG, CORE_MODULES
   - Provider-only: DataSource interface + actuals (android/ios/desktop) + platformModule; flags any iOS-Swift-bridge follow-up
```

Each agent works in isolated context window.

**Wait for ALL launched agents to complete** → Verify each succeeded

#### Step 2: Launch Integration Agent
```
Invoke integration-agent with:
- Feature name: {featurename}
- Project context: PKG_PREFIX, PKG_PATH, PROJECT_NAMESPACE, CORE_COMMON_PKG,
  CORE_DATA_PKG, CORE_DESIGNSYSTEM_PKG, INIT_KOIN_PATH,
  NAV_HOST_PATH, CORE_MODULES
- Bottom-bar tab: read the PRD Navigation section — if a top-level tab, pass label/icon/order (point 5); else pushed screen. **Contradiction check**: if the design blueprint Component Tree contains a tab nav bar note (`[App-shell chrome — Integration Point 5...]`) BUT the PRD says "pushed screen", STOP — fix the PRD first. Design is authoritative.
- **First-feature (Welcome) handoff (MANDATORY for the first feature)**: part of Integration Point 4 — if `WelcomeScreen.kt` exists AND `{NAV_HOST_PATH}` has `startDestination = WelcomeRoute`, switch `startDestination` to `{Feature}Route`, drop the `composable<WelcomeRoute> { WelcomeScreen() }` line + imports, and `rm -f` `WelcomeScreen.kt`. See architecture/integration.md → "4a. First-feature (Welcome) Handoff".
- Platform module (Rule 14, tag ≠ `network`): pull `platformModule` into `{featurename}Module` via `includes(platformModule)`; provide `androidContext()` if needed
- Integrates data, platform, and UI layers
- Completes integration points 1–4 (+ point 5 if a tab) + first-feature Welcome handoff (if applicable)
- Final validation + formatting
- Generates spec.md
```

**Wait for completion** → Verify success

---

### Post-Agent: Design-Aware Finalization

If in **design-aware mode**, after all agents complete successfully:

1. **Verify Post-Implementation Checklist** from the blueprint
2. **Set `blueprintConsumed: true`** in `.claude/docs/_project/stitch-project.json` under `features[{featurename}]`

---

## Step 4.3: Verify Spec Generation

After all agents complete, verify the living specification was generated:

```bash
ls -la .claude/docs/{featurename}/spec.md
```

**Expected**: The spec.md file should exist and contain the complete specification.

**If spec.md exists** → Proceed to Phase 5 (Cleanup)

**If spec.md missing** → Check integration agent output, may need to re-invoke

---

## Step 4.4: Platform Follow-ups (iOS-Swift bridge route)

If `platform-agent` (or `ui-layer-agent` for a native view) flagged that an **iOS `actual` needs Swift**, the Kotlin side is complete but the iOS implementation is a stub. Skills never call each other — **surface a route to the user** as part of the completion report:

```
> iOS note — {Feature}'s iOS actual needs a Swift implementation.
> Run `/bridging-swift-kotlin` for {Feature}Bridge to complete the iOS side.
```

Do **not** invoke `/bridging-swift-kotlin` yourself. Android + desktop builds pass without it; the iOS framework links once the user completes the bridge. If no Swift dependency was flagged, omit this step entirely.

---

## Agent Context Passing

When invoking each agent, include the full project context from Phase 0:

```markdown
## Project Context

- PKG_PREFIX: {value}
- PKG_PATH: {value}
- CORE_COMMON_PKG: {value}
- CORE_DATA_PKG: {value}
- CORE_DESIGNSYSTEM_PKG: {value}
- INIT_KOIN_PATH: {value}
- NAV_HOST_PATH: {value}

## Feature

- Name: {featurename}
- Docs: .claude/docs/{featurename}/
```

---

## Error Handling

| Error | Action |
|-------|--------|
| Agent build failure | Agent loads troubleshooting, fixes, retries |
| Agent reports failure | Review output, fix issues, re-invoke |
| Timeout | Check agent status, may need to restart |

---

## Output

After all agents complete:
- Data layer implemented and validated
- UI layer implemented and validated
- Integration complete (4 points + first-feature Welcome handoff if applicable)
- Build passing + ktlint formatted
- spec.md generated
- (Design-aware) blueprintConsumed set to true in stitch-project.json
- Ready to proceed to **Phase 5: Cleanup**
