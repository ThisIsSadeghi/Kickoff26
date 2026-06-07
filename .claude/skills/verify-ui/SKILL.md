---
description: Verify UI implementation against Stitch design via a token audit (HTML ↔ Code, with X-component default-render checks) and an X-components compliance check.
argument-hint: [feature-name]
allowed-tools: Task, Read, Write, Edit, Glob, Grep, Bash(mkdir *), Bash(ls *), Bash(curl *), Bash(rm *), Bash(grep *), Bash(wc *), Bash(./gradlew *), Bash(python3 *), AskUserQuestion, mcp__stitch__get_screen, mcp__stitch__list_screens, mcp__stitch__get_project
---

# Verify UI

Verify a feature's UI implementation matches the Stitch design at the token level. Produces a two-source token audit (HTML ↔ Code) overlaid with an X-component default-render trap checklist, plus an X-components compliance check.

**Architecture Reference:** @../_shared/patterns.md
**Rationale & edge cases:** @RATIONALE.md *(read only when an edge case stumps you)*

## Prerequisites

- Feature implemented (build passes)
- `.claude/docs/_project/stitch-project.json` exists with a `features[{featurename}]` entry

## Workflow

```
[USER INVOKES] → Preflight → Acquire HTML (reuse or download) → Token Extraction → Catalog → Token Audit → Trap Checklist → Component Overrides Check → Motion Audit → X-Components Check → Present Results → Handle Mismatches → Cleanup → DONE
```

---

## Step 1: Preflight

1. Parse feature name from `$ARGUMENTS` or ask the user.
2. Verify required tooling — **Python 3** (used by Step 3 to run `.claude/skills/_shared/extract_tokens.py`):
   ```bash
   python3 --version
   ```
   If the command fails (`python3: command not found`, non-zero exit), **STOP** and tell the user:
   ```
   Python 3 is not installed (or `python3` is not on PATH). /verify-ui extracts
   design tokens from Stitch HTML via `.claude/skills/_shared/extract_tokens.py`
   and cannot proceed without it.

   Install Python 3, then re-invoke /verify-ui {featurename}:
     - macOS:   brew install python3
     - Linux:   sudo apt-get install python3   (or your distro's package manager)
     - Windows: https://www.python.org/downloads/   (or `winget install Python.Python.3`)

   Verify with: python3 --version
   ```
   Do not retry or work around the failure — wait for the user to install Python 3.
3. Verify files exist:
   - `.claude/docs/_project/stitch-project.json` with `features[{featurename}]` entry
   - `feature/{featurename}/src/commonMain/kotlin/**/presentation/ui/`
4. Verify build passes: `./gradlew :feature:{featurename}:assembleAndroidMain`.

Read `.claude/docs/_project/stitch-project.json` to load:
- `projectId` and shared state screen IDs (`sharedStateScreens.loading.screenId`, `sharedStateScreens.failed.screenId`)
- Per-feature screen IDs (`features[featurename].successScreenId`, `.emptyScreenId`)
- **Per-feature state selections** (`features[featurename].states = { loading, failed, empty }`). False flags mean the state was skipped at design time and is **not audited** below.
- **Backward compatibility**: if `states` is absent on a legacy feature entry, derive it from observable state — `{ loading: true, failed: true, empty: (emptyScreenId != null) }`. Pre-optional-states features had loading/failed always present and empty only when an `emptyScreenId` was recorded.

If any prerequisite is missing, stop and inform the user.

> The implementation blueprint is consumed by `/creating-kmp-feature` and `/modifying-kmp-feature` (design-aware mode). Verify-ui does **not** read it — the audit compares HTML directly against the implemented code, with the X-components catalog as the third source for default-render checks.

---

## Step 2: Acquire HTML (reuse or download)

`/ui-designer` Step 1.15 persists per-state HTML to `.claude/docs/{featurename}/designs/extracted/stitch_{state}.html`. Reuse those files when present — Stitch URLs are typically one-time use, so a fresh download can fail and there is no benefit to re-downloading the exact same design snapshot.

1. `mkdir -p .claude/docs/{featurename}/designs/extracted`
2. Build the **audit state list**: always include `success`. Include `loading`/`failed`/`empty` **only when `states.{state} == true`** (per Step 1's derived flags). Skipped states are not acquired and not audited.
3. For each state in the audit list:
   - **Reuse path**: If `.claude/docs/{featurename}/designs/extracted/stitch_{state}.html` exists and is non-empty (`wc -c` > 0), use it as-is. Skip the Stitch call. Note: `loading`/`failed` HTML lives in `.claude/docs/_shared/designs/extracted/stitch_{state}.html` (shared) — check there for those states.
   - **Download path** (only when the file is missing or empty):

     **For loading and failed states — screenId lookup:**
     - Use `stitch-project.json.sharedStateScreens.{state}.screenId` and `stitch-project.json.projectId`

     **For success and empty states — screenId lookup:**
     - Use `stitch-project.json.features[featurename].successScreenId` (or `emptyScreenId`) and `stitch-project.json.projectId`

     Call `mcp__stitch__get_screen` with all 3 required params.
     Download: `curl -sL -o .claude/docs/{featurename}/designs/extracted/stitch_{state}.html {htmlCode.downloadUrl}`
     Verify with `wc -c …` — if 0 bytes, call `mcp__stitch__get_screen` again to get a fresh URL and retry the curl once.
   - Download states **sequentially** (concurrent downloads can race the URL's single-use semantics).

---

## Step 3: Token Extraction

For each state **in the audit state list from Step 2** (skipped states are not extracted), reuse the inventory `/ui-designer` already produced when present; otherwise re-extract.

1. **Reuse path**: If `.claude/docs/{featurename}/designs/extracted/tokens_{state}.md` exists and is non-empty, use it as-is.
2. **Re-extract path** (only when the inventory is missing or you re-downloaded the HTML in Step 2):
   ```bash
   python3 .claude/skills/_shared/extract_tokens.py \
     .claude/docs/{featurename}/designs/extracted/stitch_{state}.html \
     > .claude/docs/{featurename}/designs/extracted/tokens_{state}.md
   ```
3. Read each `tokens_{state}.md`.
4. **Do not read the raw HTML.** Use only the script output.
5. Trust the auto-converted dp/sp/color values directly.

The inventories are the complete element/class checklist for Step 5 — Step 5.2 must inspect every visual element, but only mismatches are emitted in the audit.

---

## Step 4: X-Components Catalog

### 4.1 Read the catalog

Read `.claude/skills/_shared/X_COMPONENTS_CATALOG.md`. It is the **third source** for the audit (HTML, Code, Catalog). Don't re-read individual `X*.kt` files — the catalog is the source of truth for X-component default-render behaviour.

If the catalog is missing or stale, see `RATIONALE.md` → *Regenerating the catalog*.

### 4.2 Build the X-component instance map

```bash
grep -rnHE "\bX[A-Z][[:alnum:]]*\s*\(" feature/{featurename}/src/commonMain/**/presentation/ui/
```

- Filter out import lines and comments.
- **Preserve duplicates** (no `sort -u`).
- Output is the instance map — Step 5.3 (Trap Checklist) walks every entry that matches one of the seven trap classes.

Also note any `ButtonDefaults`, `OutlinedTextFieldDefaults`, etc. passed as parameters.

---

## Step 5: Token Audit

### 5.1 Sources

- **Element inventories**: `.claude/docs/{featurename}/designs/extracted/tokens_{state}.md`
  - Visual elements (the full per-class blocks) drive the audit rows.
  - Layout-only one-liners are still scanned for **structural mismatches** (`flex-row` vs `Column`, `justify-between` vs `Arrangement.Start`, etc.) but produce rows only when something disagrees.
- **Tailwind config overrides**: top of each inventory file.
- **Global styles**: in the inventory file under "Global Styles".
- **Code files**: `feature/{featurename}/src/commonMain/kotlin/**/presentation/ui/**/*.kt` only. Skip `ViewModel`, `UiState`, `UiModel`, `data/`, `di/`, `navigation/`.
- **X-components catalog**: from Step 4.1.
- **Icons manifest**: `.claude/docs/{featurename}/designs/extracted/icons.json` — drives Step 5.7. Skipped when absent.
- **Images manifest**: `.claude/docs/{featurename}/designs/extracted/images.json` — drives Step 5.8. Skipped when absent.
- **Font manifest / HTML font**: `.claude/docs/{featurename}/designs/extracted/fonts.json` (or the css2 `<link>` / `font-family` in the success HTML) + `XFontFamily()` in `XTheme.kt` — drives Step 5.4b. Skipped when the design uses the system font.

> **Blueprint usage is scoped.** The implementation blueprint already drove the code, so the audit's design ground truth is the HTML — re-reading the blueprint's Design Tokens / Typography / Spacing / Component Tree is redundant and was removed (see `RATIONALE.md`). The blueprint sections verify-ui consults are the `Component Overrides` table (Step 5.4) and the `Typography Updates Required` table (Step 5.4b) inside `Pre-Implementation Contract`, plus the `## Motion` table (Step 5.10) — all record concrete decisions that have no other source (motion intent can't be re-derived from static code).

### 5.2 Convert and compare — Success state

For every visual element in the inventory, convert each Tailwind class to its dp/sp/color/modifier value, then find the matching code implementation.

**Rule — effective rendered value.** The Code column shows what actually renders: the declared parameter **after** any X-component internal overrides from the catalog. Not just the declared param. *(Worked examples: `RATIONALE.md`.)*

**Output format — mismatches only, as blocks.** The audit does **not** list OK rows. Use one block per mismatch:

```markdown
## Token Audit: {FeatureName} — Success

**Audited: {visual element count} visual elements, {layout count} layout containers.**
**Mismatches: {N}.**

### {Severity} — {short title}
- **Where:** `{ComponentFile.kt:LINE}` (and HTML element index `[N]` if useful)
- **HTML:** {expected value, e.g. "no bg class — transparent"}
- **Code:** {actual rendered value, e.g. "XIconButton default `surface` = #181228 — visible circle"}
- **Fix:** {one-line code change}
```

When `N = 0`, write `**No mismatches.**` and stop. Do not produce a table.

**Verdicts** (used as the "{Severity}" tag in the block heading):
- **CRITICAL** — see Step 5.6.
- **MINOR** — see Step 5.6.
- (No OK blocks — they are summarised in the count line and never listed.)

### 5.3 Trap Checklist — Success state

The reverse sweep is a **fixed checklist** of the seven X-component default-render traps from `X_COMPONENTS_CATALOG.md` ("How to use this catalog"). For each trap, scan the instance map; if the trap applies and the feature code does NOT override AND the HTML has no class declaring the property, produce a CRITICAL block in the same format as Step 5.2.

**Checklist:**

| # | Trap | Trigger | Fix |
|---|------|---------|-----|
| 1 | `XIconButton` default `containerColor = surface` | Any `XIconButton(...)` call without an explicit `colors` param, where HTML has no `bg-*` class on the icon button | Pass `colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, …)` |
| 2 | `XTextField` `defaultMinSize(280dp × 48dp)` | Any `XTextField(...)` whose HTML container is narrower than 280dp or shorter than 48dp | Note as MINOR; usually unfixable without forking the component |
| 3 | `XTextField` extra `padding(top = 8.dp)` when `label != null` | `XTextField(... label = { ... })` | Either drop the label or absorb the extra 8dp into the surrounding spacing |
| 4 | `XTopAppBar` always center-aligned title | `XTopAppBar(...)` where HTML title sits at `ml-4` (left-aligned) | MINOR — design system behaviour is authoritative |
| 5 | `XDialog` always 90% width | Any `XDialog(...)` whose HTML mockup uses a different width fraction | MINOR — design system behaviour is authoritative |
| 6 | `XPrimaryScrollableTabRow` no divider by default | Any `XPrimaryScrollableTabRow(...)` whose HTML shows a divider line under the tabs | Pass an explicit `divider = { HorizontalDivider() }` |
| 7 | `XRadioButton` unselected colour = `primary` (not `outline`) | Any `XRadioButton(...)` whose HTML shows an outline-coloured unselected ring | Pass explicit `colors = RadioButtonDefaults.colors(unselectedColor = colorScheme.outline)` |

Skip a row entirely when the X-component isn't used in the feature. Do **not** walk catalog properties beyond this list — full sweeps were removed because they produced churn-y false positives without proportionate catch.

### 5.4 Component Overrides Check — feature-specific traps from the blueprint

The fixed checklist in 5.3 catches the seven traps that have caused real bugs across multiple features. Per-feature divergences (e.g. `XCard containerColor`, a non-default `XBadge` size) are recorded by `/ui-designer` Step 1.16 in the blueprint's **Component Overrides** table. Together with the **Typography Updates Required** table (Step 5.4b), these are the only blueprint sections verify-ui consults.

1. Read **only** the `### Component Overrides` table inside `## Pre-Implementation Contract` of `.claude/docs/{featurename}/designs/{featurename}_blueprint.md` (Step 5.4b separately reads `### Typography Updates Required`). Do not read any other blueprint section. If the blueprint is missing, skip 5.4 and note in the audit: `Blueprint not found — Component Overrides check skipped.`
2. For each row (`Component | Property | HTML Value | X-component Default | Override Required`):
   - Locate every instance of that `Component` in the feature's `presentation/ui/` (use the instance map from Step 4.2).
   - Check the parameter named in `Property` against `Override Required`.
   - If the override is missing → emit a **CRITICAL** block in the same format as Step 5.2:
     ```markdown
     ### CRITICAL — Missing component override: {Component}.{Property}
     - **Where:** `{File.kt:LINE}`
     - **HTML:** {HTML Value from blueprint}
     - **Code:** {actual rendered value — typically the X-component default named in the table}
     - **Fix:** {Override Required value}
     - **Source:** Blueprint Component Overrides
     ```
   - If the override is present and matches → silent pass (no row).
3. If the blueprint table is empty, skip 5.4 — the blueprint generator detected no per-feature divergences.

This step is **additive** to 5.3, not a replacement. The seven generic traps still run.

### 5.4b Typography Audit — font family + type-scale roles

Typography is app-global (see `_shared/patterns.md` → "Typography"). Two checks:

1. **Font family (one-time, global).** Parse the design typeface from the success-state HTML — the `font-family: '…'` declaration / tailwind `fontFamily` config / `fonts.googleapis.com/css2?family=…` link (or read `family` from `.claude/docs/{featurename}/designs/extracted/fonts.json` if present). Read `XFontFamily()` in `XTheme.kt` and resolve the family its `Font(Res.font.*)` resources belong to. If they differ → **CRITICAL**:
   ```markdown
   ### CRITICAL — Theme font does not match design typeface
   - **Where:** `core/designsystem/.../XTheme.kt` (XFontFamily)
   - **HTML:** {design family, e.g. Manrope}
   - **Code:** {theme family, e.g. Outfit}
   - **Fix:** run `download_font.py` for {family} and rewire `XFontFamily()` (the design-aware font swap was skipped)
   ```
   Skip this check if the HTML declares no custom font (system default).

2. **Type-scale role per text node.** For each text node, the code should set `style = MaterialTheme.typography.{role}` (or an `XTextDefaults` preset). Resolve that role's effective `fontSize`/`fontWeight` from the M3 type scale and compare to the node's inventory `font-size`/weight:
   - A raw `fontSize`/`fontWeight` on `XText` with **no** matching override row in the blueprint's *Typography Updates Required* → **MINOR** (typography should flow through a role), unless the value is also wrong vs the inventory → then **CRITICAL** per the size/weight rule in 5.6.
   - The role's stock size/weight diverges from the inventory by a noticeable step (e.g. inventory 24sp/Bold but `bodyMedium` = 14sp/Normal with no override) → **CRITICAL — wrong type-scale role** (fix: pick the closer role or add the recorded `.copy(...)`).

Emit blocks in the Step 5.2 format. Silence = OK.

### 5.5 Loading, Failed, and Empty states — brief checklist

Run this subsection **only for non-success states present in the audit state list** (Step 2). Skipped states are omitted from the audit report entirely.

These states are typically trivial (a spinner, an error illustration, or an empty-state placeholder). **Loading/Failed are rendered by the shared `{PKG_PREFIX}.designsystem.app.AppLoadingState`/`AppErrorState` (one per project, not per-feature)** — audit that shared component against the shared `_shared/designs/` inventory once; the feature only passes copy (`error_title`/`error_message`) + an optional secondary action. Empty is per-feature. Inspect the inventory and code for each present state:

- If everything matches → write `### {State}: no mismatches.` and stop.
- If there are mismatches → emit them as Step 5.2 blocks under a `### {State}` heading.

No "OK" bullets — silence means OK.

### 5.6 Classify mismatches

| Severity | Criteria | Action |
|----------|----------|--------|
| **Critical** | Spacing ≥4dp off, wrong color role, missing component, wrong font size/weight, **wrong type-scale role or theme font ≠ design typeface (5.4b)**, wrong corner radius, wrong icon size, wrong border, **any catalog trap caught in 5.3**, **any missing override caught in 5.4**, **any manifest-audit CRITICAL from 5.7 or 5.8** | Must fix |
| **Minor** | Spacing 1–3dp off, shadow omitted, letter-spacing off, **raw `fontSize`/`fontWeight` instead of a type-scale role when the value is otherwise correct (5.4b)**, minor decorative detail, design-system-authoritative trap (centre-aligned title, 90% dialog width), **any manifest-audit MINOR from 5.7 or 5.8**, **any Motion Audit finding from 5.10** (presence-only — missing row, family mismatch, inline placement, missing reduced-motion gate) | Report; fix if requested |
| **Data-only** | Different mock data text/values | Ignore |

This is a **reference table only**. Apply these labels inline as you produce mismatch blocks in 5.2–5.5 and 5.7–5.8. The actual save happens in 5.9 (after all audit sub-sections complete).

### 5.7 Icons Manifest Audit

**Purpose**: Verify the implementation matches the icons manifest produced by `/ui-designer` Step 1.15 sub-step 5 and materialized by `/creating-kmp-feature` / `/modifying-kmp-feature` (design-aware mode).

**Skip condition**: If `.claude/docs/{featurename}/designs/extracted/icons.json` does not exist OR `stitch-project.json.features[{featurename}].blueprintConsumed != true`, skip this step with: `### Icons Manifest Audit: skipped — feature not implemented in design-aware mode.`

**Inputs**:
- `.claude/docs/{featurename}/designs/extracted/icons.json` (manifest)
- Feature Kotlin sources (instance map already built in Step 4.2 — extend it with a generic grep over `painterResource(...)` calls and import lines)

**Per-entry checks** (CRITICAL severity unless noted):

For each entry in `icons.json.icons`:

1. **File presence** — confirm the XML file exists at `entry.drawable_path` (relative to repo root). If missing → CRITICAL:
   ```markdown
   ### CRITICAL — Icon XML file missing: {entry.drawable_name}
   - **Where:** `{entry.drawable_path}` (declared in icons.json)
   - **Code:** file does not exist on disk
   - **Fix:** Re-run `python3 .claude/skills/_shared/download_assets.py --type icons --feature {featurename} --project-root . --html …` (full mode) to materialize.
   - **Source:** icons.json
   ```

2. **Code reference present** — grep for the exact `entry.res_reference` literal in feature Kotlin. If zero matches → MINOR (the design declares the icon but the UI agent didn't wire it; could be intentional incremental work):
   ```markdown
   ### MINOR — Icon declared but not referenced in code: {entry.drawable_name}
   - **HTML:** icon present in design (state(s): {entry.occurrences})
   - **Code:** no `painterResource({entry.res_reference})` call found in `feature/{featurename}/src/`
   - **Fix:** Either reference the icon in the relevant composable, or remove its `<span>` from the Stitch design and re-run `/ui-designer`.
   - **Source:** icons.json
   ```

3. **Wrong scope reference** — for `entry.scope == "chrome"`, code MUST use `DesignSystemResources.drawable.{ident}`. If any `.kt` references the same ident via `Res.drawable.{ident}` (feature-local Res) instead → CRITICAL "Chrome icon referenced via feature-local Res":
   ```markdown
   ### CRITICAL — Chrome icon referenced via feature-local Res: {entry.drawable_name}
   - **Where:** `{File.kt:LINE}`
   - **Code:** `Res.drawable.{ident}` (would resolve to feature-local generated Res, not the design system's)
   - **Fix:** Change to `DesignSystemResources.drawable.{ident}` and adjust imports.
   - **Source:** icons.json
   ```

> **Bottom-bar tab icons are app-shell chrome.** A bottom navigation bar is rendered in `App.kt` (not in any feature), so its tab icons and labels both resolve via `{PROJECT_NAMESPACE}.composeapp.generated.resources.Res` (drawables and strings in `composeApp/composeResources/`) — this is correct and expected, not a "wrong scope" finding. Do not flag the app-shell bottom bar as a missing/mis-scoped feature element. Auditing the bar itself (tab count, order, labels) is out of scope for the per-feature UI audit.

4. **Forbidden legacy imports** — grep all feature Kotlin for `import androidx.compose.material.icons.`. Any match is CRITICAL (the pinned `material-icons-extended` library is deprecated; every icon usage in a design-aware feature must go through `XIcon(painter = painterResource(...))`):
   ```markdown
   ### CRITICAL — Deprecated material-icons-extended import
   - **Where:** `{File.kt:LINE}`
   - **Code:** `import androidx.compose.material.icons.{name}`
   - **Fix:** Replace `XIcon(imageVector = Icons.*)` with `XIcon(painter = painterResource(...))` using the matching entry's `res_reference` from icons.json.
   - **Source:** project-wide rule (pinned/deprecated library)
   ```

5. **Orphan XML files** — list every `*.xml` under `feature/{featurename}/.../composeResources/drawable/` and `core/designsystem/.../composeResources/drawable/`. Cross-reference each ident against the union of all `icons.json` manifests under `.claude/docs/*/designs/extracted/`. Any file whose ident is in NONE of the manifests AND **not actively referenced** by any `.kt` (grep for `Res.drawable.{ident}` or `DesignSystemResources.drawable.{ident}` across all feature and core sources) → MINOR "Orphan icon XML":
   ```markdown
   ### MINOR — Orphan icon XML: {ident}.xml
   - **Where:** `{drawable_dir}/{ident}.xml`
   - **Code:** not referenced by any icons.json manifest AND no `.kt` code references it
   - **Fix:** If the icon is no longer used by any feature, delete it. If it should be in a feature's manifest, re-run `/ui-designer {featurename}` to regenerate.
   ```

   The "not actively referenced" rule alone is sufficient — pre-existing project assets (the design system's logos, placeholders, level icons, etc.) survive because their Kotlin call-sites still reference them. No hardcoded allowlist needed.

6. **Forbidden `@android:color/*` in drawable XML** — grep the **content** of every `*.xml` under all three drawable dirs (`feature/{featurename}/.../composeResources/drawable/`, `core/designsystem/.../composeResources/drawable/`, **and** `composeApp/.../composeResources/drawable/` — the last holds hand-added tab icons not in any manifest) for `@android:color`. Any match is CRITICAL (the `@android:color` namespace is Android-only, unresolved by the KMP resource pipeline → **runtime crash on iOS/desktop**). This catches XML that `download_assets.py` did not clean (hand-added icons, third-party icon packs, brand drawables):
   ```markdown
   ### CRITICAL — Forbidden @android:color in drawable XML: {ident}.xml
   - **Where:** `{drawable_dir}/{ident}.xml:LINE`
   - **Code:** `android:fillColor="@android:color/{name}"` (or strokeColor/other color attr)
   - **Fix:** Replace with the literal ARGB hex (`white` → `#FFFFFFFF`, `black` → `#FF000000`, `transparent` → `#00000000`; look up others in Android's `colors.xml`). For Material Symbols, re-run `download_assets.py` (full mode) which cleans this automatically.
   - **Source:** project-wide rule (KMP resource pipeline) — see `_shared/X_COMPONENTS_CATALOG.md` → "Drawable XML authoring"
   ```

### 5.8 Images Manifest Audit

**Purpose**: Verify the implementation matches the images manifest produced by `/ui-designer` Step 1.15 sub-step 6 and materialized by `/creating-kmp-feature` / `/modifying-kmp-feature`.

**Skip condition**: If `.claude/docs/{featurename}/designs/extracted/images.json` does not exist OR `stitch-project.json.features[{featurename}].blueprintConsumed != true`, skip with: `### Images Manifest Audit: skipped — feature not implemented in design-aware mode.`

**Inputs**:
- `.claude/docs/{featurename}/designs/extracted/images.json`
- Feature Kotlin sources

**Per-entry checks** — branch on each entry's `delivery` field (`bundled` = static design asset rendered via `painterResource`; `remote` = dynamic content rendered via `AsyncImage(url = <data field>)`).

**For `delivery: "bundled"` entries:**

1. **File presence** — confirm a file exists at `entry.drawable_path` (extension included). If `entry.extension == "unknown"`, glob for `{drawable_name}.{png,jpg,jpeg,webp}` in the predicted directory. Missing → CRITICAL.

2. **Code reference present** — grep for `painterResource({entry.res_reference})` in feature Kotlin. Missing → MINOR.

3. **Wrong scope reference** — same chrome/domain rule as icons (5.7 check 3).

4. **Wrong renderer (bundled rendered as AsyncImage)** — if `{entry.drawable_name}`/`{entry.res_reference}` is never used with `painterResource` but the design clearly bundles it, and instead an `AsyncImage` stands in its place → MINOR "Bundled image rendered as AsyncImage" (a static design asset should be `Image(painterResource(...))`).

**For `delivery: "remote"` entries:**

5. **No bundled file expected** — there should be **no** `{drawable_name}.{png,jpg,jpeg,webp}` under the feature/DS drawable dirs and **no** `DesignSystemResources` entry for it. If a stale file exists (e.g. left over from a prior `bundled` run) → MINOR "Stale bundled file for remote image — delete; it ships as AsyncImage".

6. **Rendered via AsyncImage** — the feature should render dynamic images through the design-system `AsyncImage(url = …)`. If the feature has remote-delivery entries but **no** `AsyncImage(` call at all in its sources → MINOR "Remote image not wired to AsyncImage" (suggest binding `url` to the manifest's `data_binding` field). Do **not** hard-assert the exact field name (the data layer chooses it).

7. **Remote rendered as painterResource** — if `painterResource(...{drawable_name})` is used for a `remote` entry → MAJOR "Remote image bundled instead of AsyncImage" (the Stitch CDN image is an ephemeral placeholder, not a shippable asset).

**For ALL entries:**

8. **Forbidden Stitch CDN URL in `AsyncImage`** — the Stitch CDN URL must never reach runtime in any `AsyncImage` call (it is ephemeral). Grep both wrapper params (`url=` for the design-system wrapper, `model=` for raw coil3):
   ```bash
   grep -rnE 'AsyncImage\([^)]*(url|model)\s*=\s*"https://lh3\.googleusercontent\.com/aida-public/' \
     feature/{featurename}/src/commonMain/
   ```
   Any match → CRITICAL "Stitch CDN URL used with AsyncImage":
   ```markdown
   ### CRITICAL — Stitch CDN URL used with AsyncImage: {ident}
   - **Where:** `{File.kt:LINE}`
   - **Code:** `AsyncImage(url = "https://lh3.googleusercontent.com/aida-public/...")`
   - **Fix:** The Stitch CDN URL is an ephemeral placeholder. For a `bundled` image use `Image(painter = painterResource({entry.res_reference}), …)`; for a `remote` image bind `url` to a data-layer field (`AsyncImage(url = uiModel.{field}, loadingResId = DesignSystemResources.drawable.ds_image_placeholder)`), never the literal CDN URL.
   - **Source:** images.json + project-wide rule
   ```
   This check intentionally does NOT flag `AsyncImage(url = uiModel.{field})` / `AsyncImage(model = uiModel.{field})` — runtime-data binding via ViewModel state is the legitimate `AsyncImage` use case.

9. **Orphan image files** — list every `*.png|*.jpg|*.jpeg|*.webp` under the feature and design-system drawable dirs. Cross-reference against the union of all `images.json` manifests' **bundled** entries. Files whose ident is in NONE of the manifests (or belongs only to a now-`remote` entry) AND **not actively referenced** by any `.kt` (same safety rule as 5.7) → MINOR "Orphan image".

### 5.10 Motion Audit — presence check per blueprint motion row

**Purpose**: Confirm each **kept** animation the blueprint declares is implemented with the declared primitive in a dedicated `motion/` file. Static code can't prove runtime feel — this is a **presence** check only (all findings **MINOR**). Policy + family→primitive mapping: `.claude/skills/_shared/motion.md`.

**Skip condition**: If `.claude/docs/{featurename}/designs/{featurename}_blueprint.md` is missing OR has no `## Motion` table, skip with: `### Motion Audit: skipped — no ## Motion table (static design).`

**Inputs**:
- The blueprint's `## Motion` table (and its trailing **Dropped** note) — the **only** blueprint section this step reads. Each row: `Element | Family | Compose primitive | Params | Target file`.
- Feature Kotlin sources, especially `feature/{featurename}/.../presentation/ui/motion/*.kt` and `core/designsystem/.../motion/*.kt`.

**Per-row checks** (all MINOR):

1. **Primitive present** — for each KEEP row, grep the feature + DS sources for the declared primitive token (`shimmer`, `rememberInfiniteTransition`, `AnimatedVisibility`, `animateContentSize`, `animate*AsState`, `pulseGlow`, `AmbientMeshBackground`, `PulseDot`, `revealOnAppear`, etc.). Zero matches → MINOR "Motion row not implemented":
   ```markdown
   ### MINOR — Motion not implemented: {Element} ({Family})
   - **HTML/Blueprint:** {primitive} expected ({params})
   - **Code:** no `{primitive}` reference found in feature or DS motion/ sources
   - **Fix:** implement the row in {Target file} per _shared/motion.md, gated by rememberReducedMotion().
   - **Source:** Blueprint ## Motion
   ```
2. **Right family** — if a different-family primitive is used where the row declares another (e.g. an entrance row implemented as an infinite loop), → MINOR "Motion family mismatch".
3. **In a `motion/` file (not inline)** — if the primitive is found only inside `{Feature}Screen.kt` or a `components/*.kt` file (not under `motion/`), → MINOR "Motion implemented inline, should be in motion/".
4. **Reduced-motion gate** — if a kept row's primitive is present but no `rememberReducedMotion()` reference exists in the same motion file, → MINOR "Motion not gated by rememberReducedMotion()". Also confirm `rememberReducedMotion` is an `expect/actual` (a `XMotion.kt` `expect` + `.android`/`.ios`/`.desktop` actuals exist under `core/designsystem/.../motion/`); a plain commonMain stub → MINOR "rememberReducedMotion is a stub, not expect/actual (does not honor OS setting)".
5. **`XMotion` token usage** — grep the feature + DS motion files for ad-hoc duration/easing literals: `tween(<number>)` or a raw `CubicBezierEasing(` not routed through an `XMotion.*` token. Any hit → MINOR "Ad-hoc motion param, use XMotion token":
   ```markdown
   ### MINOR — Ad-hoc motion param: {File.kt:LINE}
   - **Code:** `tween(1730)` / raw `CubicBezierEasing(...)`
   - **Fix:** reference an `XMotion.*` duration/easing token (motion.md — durations/easings are app-global, like M3 color roles).
   ```

> **Magnitude is recorded, not auto-asserted.** The blueprint's **Magnitude** column pins the value range so the implementer doesn't guess, but static code can't reliably prove a runtime magnitude — do **not** emit a finding for a magnitude mismatch. (Presence + family + placement + gate + token are the checkable parts.)

**Never expected in code** — rows are KEEP-only by construction, so the blueprint's **Dropped** entries (touch press `active:*`/`ripple`, pointer `hover:*`/`group-hover:*`) must **not** be flagged as missing. Do not invent findings for dropped interaction/hover motion.

### 5.9 Save the audit report

After all of 5.2–5.10 complete, write the full report (all mismatch blocks from 5.2 / 5.3 / 5.4 / 5.5 / 5.7 / 5.8 / 5.10, classification per 5.6) to:

```
.claude/docs/{featurename}/designs/{featurename}_audit.md
```

This is the single save point for the entire token-audit section. Step 7 (Present Results) reads from the in-memory report; Step 9 (Cleanup) records the audit metadata in `stitch-project.json`.

---

## Step 6: X-Components Compliance Check

### 6.1 Detect forbidden Material3 imports

```
import androidx.compose.material3.    // any M3 component import (wildcard or named)
import coil3.compose.AsyncImage       // use AsyncImage from :core:designsystem
```

`MaterialTheme.colorScheme` and `MaterialTheme.typography` are **allowed** — `XTheme` wraps `MaterialTheme`. Only component imports are forbidden.

**Animation imports are allowed** — `androidx.compose.animation.*`, `androidx.compose.animation.core.*`, and `androidx.compose.foundation.interaction.*` are **not** Material3 and are required for motion (Step 5.10). Do **not** flag them as violations.

### 6.2 Compliance report

```markdown
## X-Components Compliance Report: {FeatureName}

| File | Line | Violation | Correct Alternative |
|------|------|-----------|---------------------|
| SendScreen.kt | 12 | import androidx.compose.material3.Button | XButton from :core:designsystem |
```

All M3 component violations are Critical.

### 6.3 String-resource compliance (Rule 12)

Scan feature composables for hardcoded user-facing literals:

```
grep -rnHE '(text|label|placeholder|contentDescription)\s*=\s*"[^"]' feature/{featurename}/src/commonMain/**/presentation/ui/
grep -rnHE '\bX(Text|Button)\s*\(\s*"[^"]' feature/{featurename}/src/commonMain/**/presentation/ui/
```

For each hit, classify:
- **Violation (Critical)** — a display string not wrapped in `stringResource(...)` / `UiText`.
- **Allowed** — inside a `@Preview` fixture, a control sentinel parsed in logic (e.g. `label == "MAX"`), a single-glyph symbol (`$`, `₿`, `%`, `✓`), or repository-supplied data passed through (names, dates, tickers).

Also confirm `composeResources/values/strings.xml` exists and every `Res.string.*` referenced resolves to a key in it.

```markdown
## String Resource Compliance (Rule 12): {FeatureName}

| File | Line | Literal | Verdict | Fix |
|------|------|---------|---------|-----|
| RecipientCard.kt | 88 | "Wallet address or ENS name" | Violation | extract to strings.xml → stringResource |
```

---

## Step 7: Present Results

```
## Verification Report: {FeatureName}

### Token Audit
- Visual elements audited: {N}
- Critical mismatches: {N}
- Minor mismatches: {N}

### Icons Manifest Audit (Step 5.7)
- Manifest entries: {N} (or "skipped — no manifest")
- Missing files: {N} | Wrong-scope refs: {N} | Deprecated imports: {N} | Orphans: {N}

### Images Manifest Audit (Step 5.8)
- Manifest entries: {N} (or "skipped — no manifest")
- Missing files: {N} | Wrong-scope refs: {N} | Stitch-CDN AsyncImage misuse: {N} | Orphans: {N}

### Motion Audit (Step 5.10)
- Motion rows: {N} (or "skipped — no ## Motion table")
- Not implemented: {N} | Family mismatch: {N} | Inline (not in motion/): {N} | Ungated: {N} (all MINOR)

### X-Components Compliance
- Material3 violations: {N} ({PASS if 0, FAIL otherwise})

{Mismatch blocks from Step 5.2 / 5.3 / 5.4 / 5.5 / 5.7 / 5.8 / 5.10 — only the blocks, no OK rows.}

{Compliance violations table (if any).}
```

---

## Step 8: Handle Mismatches

### Critical issues (token mismatches OR Material3 violations)

1. Save audit report with fix instructions: file, line, current value, correct value.
2. Tell the user — emit the blockquote as the very last line of output:

   ```
   Critical issues found: {N} token mismatches, {N} M3 violations.
   Audit report: .claude/docs/{featurename}/designs/{featurename}_audit.md

   ---

   > **Next step —** run `/clear` to free the context window (the audit at `.claude/docs/{featurename}/designs/{featurename}_audit.md` + the blueprint are durable artifacts the next skill re-reads fresh, so clearing loses nothing), then `/modifying-kmp-feature {featurename} fix all UI audit issues based on @.claude/docs/{featurename}/designs/{featurename}_audit.md` to apply the fixes.
   ```

This skill does not invoke `/modifying-kmp-feature` — the user controls the pipeline.

### Only minor mismatches

| Option | Description |
|--------|-------------|
| Accept (Recommended) | Minor differences are acceptable |
| Fix all | User invokes `/modifying-kmp-feature` |

---

## Step 9: Cleanup

1. Preserve HTML and token inventories in `.claude/docs/{featurename}/designs/extracted/`.
2. Update `.claude/docs/_project/stitch-project.json` — set `features[{featurename}].verification` (replace if exists):

   ```json
   {
     "verified": true,
     "verifiedAt": "2026-05-02",
     "auditReport": "designs/{featurename}_audit.md",
     "extractedSources": "designs/extracted/",
     "xComponentsCompliant": true,
     "criticalIssues": 0,
     "attempts": 1
   }
   ```

   Also update `stitch-project.json.updatedAt`.

3. Show the completion report.

---

## Completion Report

```
## Verify UI Complete: {FeatureName}

### Token Audit Results
| State | Critical | Minor | Status |
|-------|----------|-------|--------|
| Success | {N} | {N} | {PASS/FAIL} |
| Loading | {N} | {N} | {PASS/FAIL} |
| Failed | {N} | {N} | {PASS/FAIL} |

### Asset Manifest Audit
| Manifest | Critical | Minor | Status |
|----------|----------|-------|--------|
| Icons (5.7)  | {N} | {N} | {PASS/FAIL or SKIPPED} |
| Images (5.8) | {N} | {N} | {PASS/FAIL or SKIPPED} |
| Motion (5.10) | — | {N} | {PASS/FAIL or SKIPPED} |

### X-Components Compliance
| Check | Violations | Status |
|-------|------------|--------|
| No Material3 usage | {N} | {PASS/FAIL} |

Audit report: .claude/docs/{featurename}/designs/{featurename}_audit.md

{If critical issues, append as the FINAL line of output:}

---

> **Next step —** run `/clear` to free the context window (the audit at `.claude/docs/{featurename}/designs/{featurename}_audit.md` + the blueprint are durable artifacts the next skill re-reads fresh, so clearing loses nothing), then `/modifying-kmp-feature {featurename} fix all UI audit issues based on @.claude/docs/{featurename}/designs/{featurename}_audit.md` to apply the fixes.
```
