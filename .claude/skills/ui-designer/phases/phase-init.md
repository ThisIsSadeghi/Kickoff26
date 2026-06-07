# Phase Init: Project Initialization (One-Time Per Repo)

**Purpose**: Create a single shared Stitch project for the entire KMP repo, establish the design system from XTheme.kt, and generate shared Loading and Failed state screens that all features reuse.

**When to run**: When `.claude/docs/_project/stitch-project.json` does not exist, or `initState.completedAt` is null. Safe to re-run — each step checks `initState` flags before executing and skips completed steps.

---


## Init Checklist

Display this checklist at the start of Init and update each item as it completes:

```
Project Init Progress:
- [ ] Init-1: Verify Stitch MCP
- [ ] Init-2: Theme setup
- [ ] Init-3: Create shared Stitch project
- [ ] Init-4: Create design system from XTheme.kt
- [ ] Init-5: Persist and finalize
```

**Shared Loading/Failed screens are NOT generated during init.** They are designed on-demand by the first feature that opts in to them (Phase 1 Step 1.8 in `phase-1-design.md`), then persisted to `_shared/` and inherited by every subsequent feature that opts in. The canonical generation procedures live in **On-Demand Procedures** at the bottom of this file.

---

## Resumption Logic

Before running any init step, read `.claude/docs/_project/stitch-project.json` if it exists and check `initState`:

- `initState.projectCreated == true` → skip Init-3
- `initState.designSystemCreated == true` → skip Init-4
- `initState.completedAt` non-null → Init is already complete; stop and inform user

**Legacy compatibility**: older projects may have `initState.sharedScreensGenerated == true` and populated `sharedStateScreens.{loading,failed}`. The new init flow no longer sets this flag, but legacy projects continue to work — their shared screens already exist, so Step 1.8 will detect them and skip generation.

For each step that is skipped, mark it as `[done]` in the checklist display.

---

## Init-1: Verify Stitch MCP

Call `mcp__stitch__list_projects`.

**If successful**: Stitch MCP is available. Mark Init-1 complete. Proceed to Init-2.

**If fails** (tool not found or connection error): Stitch MCP is not configured. Walk the user through setup using the procedure below — the full reference lives at `.claude/skills/ui-designer/references/stitch-setup.md`.

### Guided Setup

1. **Check Node version** by running `node -v`. If Node is missing or below 18, tell the user to install Node 18+ (e.g. via `nvm install 20`) and stop until they confirm.

2. **Pick a path** using `AskUserQuestion`:

   - **API Key (Recommended)** — fastest, works in any environment including WSL/SSH/Docker
   - **OAuth wizard** — only if the user already manages a Google Cloud project they want Stitch billed against
   - **I'll set it up myself** — user prefers to read the docs and configure manually

3. **If API Key**:
   - Ask the user to open <https://stitch.withgoogle.com/settings>, sign in with their Stitch Google account, find the **API keys** section, click **Create API key**, and copy the value. **Do NOT use AI Studio (`aistudio.google.com/apikey`) — those keys are rejected by the Stitch API with "API keys are not supported by this API".**
   - Once they confirm they have the key, tell them to run (replacing `YOUR_API_KEY`):
     ```bash
     claude mcp add stitch \
       --transport http https://stitch.googleapis.com/mcp \
       --header "X-Goog-Api-Key: YOUR_API_KEY" \
       -s user
     ```
   - Tell them to **fully quit and reopen Claude Code**, then re-invoke `/ui-designer`.

4. **If OAuth wizard**:
   - Tell them to run `npx @_davideast/stitch-mcp init` and follow the prompts (select Claude Code as the client, OAuth as the auth mode).
   - Tell them to **fully quit and reopen Claude Code**, then re-invoke `/ui-designer`.

5. **If "I'll set it up myself"**: point them to `.claude/skills/ui-designer/references/stitch-setup.md` for the full guide and stop.

**STOP** in all cases — Stitch MCP cannot be activated within a running Claude Code session. The user must restart Claude Code before re-invoking the skill.

---

## Init-2: Theme Setup

Run the **full theme setup logic** exactly as specified below. This is the same procedure as the old phase-0-preflight.md Step 0.1.

### Discover XTheme.kt Path

The path is project-specific (depends on the repo's package prefix). Discover it once here; every later step in this skill reads it from `stitch-project.json.designSystem.xthemePath`.

1. Read `core/designsystem/build.gradle.kts` and grep `namespace = "..."`. Example: `namespace = "thisissadeghi.kmpilot.designsystem"`.
2. Convert dots to slashes → `thisissadeghi/kmpilot/designsystem`.
3. Derive `{XTHEME_PATH}` = `core/designsystem/src/commonMain/kotlin/{slash-path}/XTheme.kt`.
4. Verify the file exists. **Fallback**: if it does not, use `Glob` with `core/designsystem/src/commonMain/kotlin/**/XTheme.kt` and take the first match.

Store the resolved path as `{XTHEME_PATH}`; it will be written into `stitch-project.json` at the Init-2 Write Checkpoint below.

### Detect Existing Setup

Read the file at `{XTHEME_PATH}`.

**If both `XLightColors` (lightColorScheme) and `XDarkColors` (darkColorScheme) already exist** in `XTheme.kt`:

```
The app already has both light and dark color schemes configured.

Would you like to:
- Keep existing palette — proceed with the current colors
- Reconfigure — set a new palette from a primary brand color
```

Use `AskUserQuestion` with these two options. If **Reconfigure**: proceed with full setup below.

If **Keep existing**:

1. **Check completeness** — verify both `XLightColors` and `XDarkColors` define all of these M3 roles: `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `background`, `surface`, `onBackground`, `onSurface`, `onSurfaceVariant`, `surfaceVariant`, `outline`, `outlineVariant`, `error`, `onError`, `errorContainer`, `onErrorContainer`.

2. **If complete**: read the current primary color and default theme choice from `XTheme.kt` (infer default from which scheme `XTheme()` composable passes to `MaterialTheme`). Store in context and skip to **Output**.

3. **If NOT complete**: extract the `primary` hex value from the existing scheme (prefer `XLightColors`; fall back to `XDarkColors` if light is absent). Set this value as `primaryHex` and proceed with **Ask Default Theme** → **Generate Both Color Palettes** → **Update XTheme.kt Structure** — exactly as if the user had selected **Reconfigure**.

**If only `lightColorScheme` / `XColors` exists** (no dark scheme): proceed with full setup.

### Ask Default Theme

Using `AskUserQuestion`: **"What should the app's default theme be?"**

| Option | Description |
|--------|-------------|
| Light | App always uses the light color scheme |
| Dark | App always uses the dark color scheme |

Store as `defaultTheme`.

### Ask Color Palette

Using `AskUserQuestion`: **"Which color palette do you want to use?"**

| Option | Description |
|--------|-------------|
| Keep current | Use the colors already defined in XTheme.kt |
| Customize | Provide a primary brand color to generate a full palette |

**If Keep current**: note the existing primary hex. Skip to **Update XTheme.kt Structure**.

**If Customize**: ask (free text via `AskUserQuestion`): **"Enter your primary brand color as a HEX value (e.g., #B02418):"**

Store the user's answer as `primaryHex`.

### Generate Both Color Palettes

From `primaryHex`, derive complete M3-compliant palettes for both themes. Apply the primary color's hue undertone consistently across the neutral tones.

**Light theme (`XLightColors = lightColorScheme(...)`)** — bright backgrounds, dark text:

| Role | Derivation rule |
|------|----------------|
| `primary` | `primaryHex` as-is |
| `onPrimary` | High-contrast on primary: white if primary is dark, near-black if primary is light |
| `primaryContainer` | Primary hue desaturated and lightened to ~90% tonal value (very light tint) |
| `onPrimaryContainer` | Primary hue darkened to ~10% tonal value (very dark tint) |
| `background` | Near-white neutral with a subtle undertone derived from the primary hue |
| `surface` | Same as background or marginally lighter |
| `onBackground` | Near-black with the primary hue's subtle undertone |
| `onSurface` | Same as onBackground |
| `onSurfaceVariant` | Medium gray with primary hue undertone — lower contrast than onSurface |
| `surfaceVariant` | Light gray with primary hue undertone — visually distinct from surface |
| `outline` | Medium-weight gray with primary hue undertone, readable against surface |
| `outlineVariant` | Lighter/softer variant of outline for decorative or low-emphasis borders |
| `error` | Standard M3 light error color |
| `onError` | Standard M3 onError for light theme |
| `errorContainer` | Standard M3 errorContainer for light theme |
| `onErrorContainer` | Standard M3 onErrorContainer for light theme |

**Dark theme (`XDarkColors = darkColorScheme(...)`)** — dark backgrounds, light text:

| Role | Derivation rule |
|------|----------------|
| `primary` | Primary hue lightened to ~80% tonal value — bright enough to stand out on dark bg |
| `onPrimary` | Primary hue darkened to ~20% tonal value |
| `primaryContainer` | Primary hue darkened to ~30% tonal value (dark container) |
| `onPrimaryContainer` | Primary hue lightened to ~90% tonal value |
| `background` | Very dark neutral with the primary hue's subtle undertone |
| `surface` | Slightly elevated over background — same undertone, marginally lighter |
| `onBackground` | Near-white with the primary hue's subtle undertone |
| `onSurface` | Same as onBackground |
| `onSurfaceVariant` | Muted light gray with primary hue undertone — lower contrast than onSurface |
| `surfaceVariant` | Dark elevated surface with primary hue undertone |
| `outline` | Medium gray readable against dark surface, with primary hue undertone |
| `outlineVariant` | Softer/darker variant of outline for subtle borders |
| `error` | Standard M3 dark error color (brighter than light theme for dark bg legibility) |
| `onError` | Standard M3 onError for dark theme |
| `errorContainer` | Standard M3 errorContainer for dark theme |
| `onErrorContainer` | Standard M3 onErrorContainer for dark theme |

Apply the primary color's hue undertone consistently across all neutral roles. Generate precise hex values — do not leave placeholders. Only include roles the current project uses.

### Update XTheme.kt Structure

Edit the file at `{XTHEME_PATH}`:

1. If the old private val was named `XColors`, rename it to `XLightColors`.
2. Add `XDarkColors` using `darkColorScheme(...)` with the generated dark palette.
3. Update the `XTheme()` composable to always use the scheme matching `defaultTheme`:

```kotlin
// defaultTheme = light
@Composable
fun XTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content,
        colorScheme = XLightColors,
        shapes = Shapes,
        typography = MaterialTheme.typography,
    )
}

// defaultTheme = dark
@Composable
fun XTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content,
        colorScheme = XDarkColors,
        shapes = Shapes,
        typography = MaterialTheme.typography,
    )
}
```

4. Verify the build compiles: `./gradlew :core:designsystem:assembleAndroidMain`

### Init-2 Write Checkpoint

After completing theme setup:

1. Create directory `.claude/docs/_project/` if it does not exist:
   ```bash
   mkdir -p .claude/docs/_project
   ```

2. Create `.claude/docs/_project/stitch-project.json` as a skeleton with `initState` all false/null, and write the `themeSnapshot` into `designSystem.themeSnapshot`:

```json
{
  "projectId": null,
  "projectName": null,
  "repoName": "{repo name, e.g. KMPilot}",
  "deviceType": "MOBILE",
  "modelId": "GEMINI_3_FLASH",
  "designSystem": {
    "assetId": null,
    "name": null,
    "colorMode": "{LIGHT|DARK}",
    "sourceOfTruth": "XTheme.kt",
    "xthemePath": "{XTHEME_PATH discovered above}",
    "syncedAt": null,
    "themeSnapshot": {
      "defaultTheme": "{light|dark}",
      "primaryHex": "{primaryHex}",
      "paletteCustomized": "{true|false}",
      "light": {
        "primary": "{hex}",
        "background": "{hex}",
        "surface": "{hex}",
        "error": "{hex}",
        "onSurfaceVariant": "{hex}"
      },
      "dark": {
        "primary": "{hex}",
        "background": "{hex}",
        "surface": "{hex}",
        "error": "{hex}",
        "onSurfaceVariant": "{hex}"
      }
    }
  },
  "sharedStateScreens": {
    "loading": {
      "screenId": null,
      "screenName": null,
      "screenshot": null,
      "htmlPath": null,
      "tokensPath": null,
      "dimensions": null,
      "generatedAt": null,
      "designSystemApplied": false
    },
    "failed": {
      "screenId": null,
      "screenName": null,
      "screenshot": null,
      "htmlPath": null,
      "tokensPath": null,
      "dimensions": null,
      "generatedAt": null,
      "designSystemApplied": false
    }
  },
  "features": {},
  "initState": {
    "projectCreated": false,
    "designSystemCreated": false,
    "completedAt": null
  },
  "createdAt": "{ISO timestamp}",
  "updatedAt": "{ISO timestamp}"
}
```

Mark Init-2 complete. Proceed to Init-3.

---

## Init-3: Create Shared Stitch Project

Call `mcp__stitch__create_project`.

Store the returned `projectId` and `projectName` in `stitch-project.json`:
- `stitch-project.json.projectId = {returned projectId}`
- `stitch-project.json.projectName = {returned projectName}`

Update:
- `stitch-project.json.initState.projectCreated = true`
- `stitch-project.json.updatedAt = {ISO timestamp}`

Write the file. Mark Init-3 complete. Proceed to Init-4.

---

## Init-4: Create Design System

Read `XTheme.kt` and map M3 roles to Stitch design-system fields using the `themeSnapshot` already written in Init-2.

Use the color values from the scheme matching `defaultTheme`:

| XTheme.kt role | Stitch field | Notes |
|---|---|---|
| `primary` (active scheme) | `primaryColor` | Use scheme matching `defaultTheme` |
| `secondary` | `secondaryColor` | Omit if not defined in XTheme.kt |
| `tertiary` | `tertiaryColor` | Omit if not defined in XTheme.kt |
| `defaultTheme` | `colorMode` | `LIGHT` or `DARK` |
| `Shapes` corner dp | `roundness` | Map small/medium/large corner to Stitch roundness scale |

Call `mcp__stitch__create_design_system` with:
- `projectId`: from `stitch-project.json.projectId`
- `primaryColor`: primary hex from active scheme
- `colorMode`: `LIGHT` or `DARK` matching `defaultTheme`
- `roundness`: mapped from `Shapes` corner dp
- `secondaryColor` / `tertiaryColor`: if defined in XTheme.kt

Store the returned design system ID:
- `stitch-project.json.designSystem.assetId = {returned assetId}`
- `stitch-project.json.designSystem.name = {returned name}`
- `stitch-project.json.designSystem.syncedAt = {ISO timestamp}`

Update:
- `stitch-project.json.initState.designSystemCreated = true`
- `stitch-project.json.updatedAt = {ISO timestamp}`

Write the file. Mark Init-4 complete.

**Source of truth rule**: XTheme.kt always wins when XTheme.kt and the Stitch design system drift. Stitch is a design-time mirror; sync direction is always XTheme.kt → Stitch.

Proceed to Init-5.

---

## Init-5: Persist and Finalize

Set `initState.completedAt` to the current ISO timestamp.
Update `updatedAt` to the current ISO timestamp.
Write the file.

### Completion Summary

Present to the user:

```
Project Init Complete

Shared Stitch Project: {projectId}
Design System: {designSystemAssetId}
Config: .claude/docs/_project/stitch-project.json

Shared Loading and Failed screens are NOT generated yet — they will be
designed on demand by the first feature that opts in to each state
(Phase 1 Step 1.8).

---

> **Next step —** run `/ui-designer {featurename}` to design any feature's screens.
```

---

# On-Demand Procedures

> The two procedures below are **not part of the linear init flow**. They are invoked from Phase 1 Step 1.8 the first time a feature opts in to a Loading or Failed state. Each procedure writes to `_shared/designs/` and `sharedStateScreens.{state}` in `stitch-project.json`; the result is reused by every subsequent feature that opts in.

## On-Demand: Generate Shared Loading Screen

### Prepare Prompt

Substitute color values from the active scheme's `themeSnapshot`:

```
A mobile loading screen using the app's M3 color scheme.

Background: {background hex from defaultTheme} (M3: background)
Primary accent: {primary hex} (M3: primary)

Layout:
- Full-screen background in the background color — no top app bar, no bottom navigation
- Center: Vertically and horizontally centered circular progress indicator in primary accent color
- No other content whatsoever

The screen represents a raw generic loading state overlaid by the feature. Keep it minimal.
```

### Generation Procedure

1. **Record baseline**: Call `mcp__stitch__list_screens` with `projectId` from `stitch-project.json`. Record all current screen IDs.

2. Call `mcp__stitch__generate_screen_from_text` with:
   - `projectId`: from `stitch-project.json.projectId`
   - `prompt`: loading screen prompt above
   - `deviceType`: MOBILE
   - `modelId`: GEMINI_3_FLASH

3. **Timeout / connection-reset handling**: If the call times out or returns a connection error, **do NOT retry `generate_screen_from_text`** — this is a known Google Stitch bug where the request usually completed server-side and a retry creates a duplicate screen. Instead:
   - Ask the user to open the project in their browser to trigger sync: `https://stitch.withgoogle.com/projects/{projectId}`. Tell them this is a known Google Stitch limitation.
   - Wait for the user's confirmation that they have opened the project and can see the new screen.
   - Then call `mcp__stitch__list_screens` and diff against the baseline to identify the new screen ID. If `list_screens` still shows nothing after browser sync, ask the user to refresh the browser page once more. Max 2 sync attempts.

4. Call `mcp__stitch__list_screens` again with `projectId`. Diff against baseline → identify the new screen ID. This is the working `loadingScreenId`.

5. Call `get_screen` for the new screenId:
   ```
   projectId = stitch-project.json.projectId
   name      = "projects/{projectId}/screens/{loadingScreenId}"
   screenId  = {loadingScreenId}
   ```
   Use `screenshot.downloadUrl` from the response.

6. Create the shared designs directory if it does not exist:
   ```bash
   mkdir -p .claude/docs/_shared/designs
   ```

7. Download screenshot to the canonical shared path (overwritten on each edit iteration; only finalized once the user approves below):
   ```bash
   curl -sL "{downloadUrl}=s0" -o .claude/docs/_shared/designs/loading.png
   ```

### Approve-or-Edit Loop (single loop, applies once per iteration)

After each generation/edit, tell the user the screenshot is at `.claude/docs/_shared/designs/loading.png` and ask via `AskUserQuestion`:

> **"How does the shared Loading screen look? It will be reused by every feature that opts in to a loading state."**

| Option | Description |
|--------|-------------|
| Approve (Recommended) | Save this as the canonical shared Loading screen |
| Edit | Request changes (the current screen will be edited in Stitch) |

**If Approve** → exit the loop; continue to step 8.

**If Edit**:
1. Use `AskUserQuestion` (free text via "Other") to capture the user's edit request.
2. Record baseline by calling `mcp__stitch__list_screens`.
3. Call `mcp__stitch__edit_screens` with:
   ```
   projectId: {stitch-project.json.projectId}
   selectedScreenIds: [{current loadingScreenId}]
   prompt: {user's edit request}
   deviceType: MOBILE
   modelId: GEMINI_3_FLASH
   ```
4. Apply the same timeout/connection-reset handling as the initial generation (Screen Sync Procedure, no blind retries — max 2 sync attempts).
5. Diff `list_screens` against baseline. The new screen ID becomes the working `loadingScreenId`.
6. Re-download as `.claude/docs/_shared/designs/loading.png` (overwrite).
7. Return to the top of the Approve-or-Edit Loop.

**Iteration limit**: Maximum 10 edit iterations. If not converging, ask the user to clarify before continuing.

8. Record dimensions (`width`, `height`) from the most recent `get_screen` response for the approved `loadingScreenId`.

9. Write to `stitch-project.json.sharedStateScreens.loading` using the **approved** `loadingScreenId`:
   ```json
   {
     "screenId": "{loadingScreenId}",
     "screenName": "projects/{projectId}/screens/{loadingScreenId}",
     "screenshot": ".claude/docs/_shared/designs/loading.png",
     "htmlPath": null,
     "tokensPath": null,
     "dimensions": { "width": {width}, "height": {height} },
     "generatedAt": "{ISO timestamp}",
     "designSystemApplied": false,
     "codeImplemented": false
   }
   ```

10. **Download HTML and tokenize** so verify-ui and feature blueprints can read the loading inventory:
    ```bash
    mkdir -p .claude/docs/_shared/designs/extracted
    curl -sL -o .claude/docs/_shared/designs/extracted/stitch_loading.html {htmlCode.downloadUrl from a fresh get_screen call}
    python3 .claude/skills/_shared/extract_tokens.py \
      .claude/docs/_shared/designs/extracted/stitch_loading.html \
      > .claude/docs/_shared/designs/extracted/tokens_loading.md
    ```
    Update `sharedStateScreens.loading.htmlPath` and `.tokensPath`.

11. Update `stitch-project.json.updatedAt`. Write the file. **Return to caller** (Phase 1 Step 1.8).

---

## On-Demand: Generate Shared Failed Screen

### Prepare Prompt

Substitute color values from the active scheme's `themeSnapshot`:

```
A mobile error screen using the app's M3 color scheme.

Background: {background hex from defaultTheme} (M3: background)
Primary accent: {primary hex} (M3: primary)
Error color: {error hex from defaultTheme} (M3: error)
Muted text: {onSurfaceVariant hex} (M3: onSurfaceVariant)

Layout:
- Full-screen background in the background color — no top app bar, no bottom navigation
- Center: Vertically and horizontally centered column containing:
  - Error icon (warning or error symbol) in error color
  - Error message "Something went wrong" in onSurfaceVariant text, body size
  - "Retry" button in primary accent color with onPrimary text

The screen represents a raw generic failed/error state. No chrome — no app bar, no nav bar.
```

### Generation Procedure

Same baseline-diff procedure as the Loading on-demand procedure above:

1. **Record baseline**: Call `mcp__stitch__list_screens` with `projectId` from `stitch-project.json`.

2. Call `mcp__stitch__generate_screen_from_text` with:
   - `projectId`: from `stitch-project.json.projectId`
   - `prompt`: failed screen prompt above
   - `deviceType`: MOBILE
   - `modelId`: GEMINI_3_FLASH

3. **Timeout / connection-reset handling**: Same as the Loading procedure — **do NOT retry `generate_screen_from_text`** on timeout/connection reset (known Google Stitch bug, causes duplicate screens). Ask the user to open `https://stitch.withgoogle.com/projects/{projectId}` in their browser to trigger sync, wait for confirmation, then call `mcp__stitch__list_screens` and diff against the baseline. Max 2 sync attempts.

4. Call `mcp__stitch__list_screens` again. Diff against baseline → identify the new screen ID. This is the working `failedScreenId`.

5. Call `get_screen` for the new screenId (same pattern as the Loading procedure). Use `screenshot.downloadUrl`.

6. Download screenshot:
   ```bash
   curl -sL "{downloadUrl}=s0" -o .claude/docs/_shared/designs/failed.png
   ```

### Approve-or-Edit Loop (single loop, applies once per iteration)

After each generation/edit, tell the user the screenshot is at `.claude/docs/_shared/designs/failed.png` and ask via `AskUserQuestion`:

> **"How does the shared Failed screen look? It will be reused by every feature that opts in to a failed state."**

| Option | Description |
|--------|-------------|
| Approve (Recommended) | Save this as the canonical shared Failed screen |
| Edit | Request changes (the current screen will be edited in Stitch) |

**If Approve** → exit the loop; continue to step 7.

**If Edit**:
1. Use `AskUserQuestion` (free text via "Other") to capture the user's edit request.
2. Record baseline by calling `mcp__stitch__list_screens`.
3. Call `mcp__stitch__edit_screens` with:
   ```
   projectId: {stitch-project.json.projectId}
   selectedScreenIds: [{current failedScreenId}]
   prompt: {user's edit request}
   deviceType: MOBILE
   modelId: GEMINI_3_FLASH
   ```
4. Apply the same timeout/connection-reset handling (Screen Sync Procedure, no blind retries — max 2 sync attempts).
5. Diff `list_screens` against baseline. The new screen ID becomes the working `failedScreenId`.
6. Re-download as `.claude/docs/_shared/designs/failed.png` (overwrite).
7. Return to the top of the Approve-or-Edit Loop.

**Iteration limit**: Maximum 10 edit iterations.

7. Record dimensions from the most recent `get_screen` response for the approved `failedScreenId`.

8. Write to `stitch-project.json.sharedStateScreens.failed` using the **approved** `failedScreenId`:
   ```json
   {
     "screenId": "{failedScreenId}",
     "screenName": "projects/{projectId}/screens/{failedScreenId}",
     "screenshot": ".claude/docs/_shared/designs/failed.png",
     "htmlPath": null,
     "tokensPath": null,
     "dimensions": { "width": {width}, "height": {height} },
     "generatedAt": "{ISO timestamp}",
     "designSystemApplied": false,
     "codeImplemented": false
   }
   ```

9. **Download HTML and tokenize** so verify-ui and feature blueprints can read the failed inventory:
   ```bash
   mkdir -p .claude/docs/_shared/designs/extracted
   curl -sL -o .claude/docs/_shared/designs/extracted/stitch_failed.html {htmlCode.downloadUrl from a fresh get_screen call}
   python3 .claude/skills/_shared/extract_tokens.py \
     .claude/docs/_shared/designs/extracted/stitch_failed.html \
     > .claude/docs/_shared/designs/extracted/tokens_failed.md
   ```
   Update `sharedStateScreens.failed.htmlPath` and `.tokensPath`.

10. Update `stitch-project.json.updatedAt`. Write the file. **Return to caller** (Phase 1 Step 1.8).

---

## stitch-project.json Full Schema

Created at Init-2, progressively filled through Init-5. The authoritative schema reference is in [stitch-guide.md](../references/stitch-guide.md#stitch-project-schema). Shared state screens start with `null` IDs and are populated lazily by Phase 1 Step 1.8.

```json
{
  "projectId": "string — Stitch project ID (shared)",
  "projectName": "string — Full resource name (projects/{id})",
  "repoName": "string — KMP repo name",
  "deviceType": "string — Always MOBILE",
  "modelId": "string — Always GEMINI_3_FLASH",
  "designSystem": {
    "assetId": "string — Design system asset ID",
    "name": "string — Design system resource name",
    "colorMode": "string — LIGHT or DARK",
    "sourceOfTruth": "string — Always XTheme.kt",
    "xthemePath": "string — Discovered path to XTheme.kt (repo-specific; resolved at Init-2)",
    "syncedAt": "string — ISO timestamp of last XTheme.kt → Stitch sync",
    "themeSnapshot": {
      "defaultTheme": "string — light or dark",
      "primaryHex": "string — primary brand color hex",
      "paletteCustomized": "boolean",
      "light": { "primary": "hex", "background": "hex", "surface": "hex", "error": "hex", "onSurfaceVariant": "hex" },
      "dark":  { "primary": "hex", "background": "hex", "surface": "hex", "error": "hex", "onSurfaceVariant": "hex" }
    }
  },
  "sharedStateScreens": {
    "loading": {
      "screenId": "string",
      "screenName": "string",
      "screenshot": "string — path to .png",
      "htmlPath": "string — path to .html",
      "tokensPath": "string — path to tokens.md",
      "dimensions": { "width": "number", "height": "number" },
      "generatedAt": "string — ISO timestamp",
      "designSystemApplied": "boolean",
      "codeImplemented": "boolean — false until an implementation skill rewrites AppLoadingState.kt to match this design; true after. Reset to false if the design is updated in Stitch."
    },
    "failed": {
      "screenId": "string",
      "screenName": "string",
      "screenshot": "string — path to .png",
      "htmlPath": "string — path to .html",
      "tokensPath": "string — path to tokens.md",
      "dimensions": { "width": "number", "height": "number" },
      "generatedAt": "string — ISO timestamp",
      "designSystemApplied": "boolean",
      "codeImplemented": "boolean — false until an implementation skill rewrites AppErrorState.kt to match this design; true after. Reset to false if the design is updated in Stitch."
    }
  },
  "features": {
    "{featurename}": {
      "successScreenId": "string — Stitch screen ID for success state",
      "successScreenName": "string — Full resource name",
      "emptyScreenId": "string or null — Stitch screen ID for empty state (only when states.empty == true)",
      "states": {
        "loading": "boolean — true if this feature opts in to the shared loading screen",
        "failed":  "boolean — true if this feature opts in to the shared failed screen",
        "empty":   "boolean — true if this feature has a per-feature empty design"
      },
      "screenshot": "string — path to success .png",
      "htmlPath": "string — path to success .html",
      "tokensPath": "string — path to success tokens.md",
      "dimensions": { "width": "number", "height": "number" },
      "designFile": "string — path to .md design description",
      "blueprintFile": "string — path to _blueprint.md",
      "approved": "boolean",
      "approvedAt": "string — ISO date",
      "createdAt": "string — ISO timestamp",
      "updatedAt": "string — ISO timestamp",
      "legacyProject": "boolean — optional, true if migrated from legacy per-feature project",
      "legacyProjectId": "string — optional, old per-feature projectId"
    }
  },
  "initState": {
    "projectCreated": "boolean",
    "designSystemCreated": "boolean",
    "completedAt": "string or null — ISO timestamp when init finalized (Init-5)",
    "sharedScreensGenerated": "boolean — LEGACY only; set by the old init flow when it auto-generated shared screens. Not written by the new init flow. Safe to ignore."
  },
  "createdAt": "string — ISO timestamp",
  "updatedAt": "string — ISO timestamp"
}
```
