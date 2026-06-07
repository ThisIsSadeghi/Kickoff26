# Phase 0: Per-Feature Preflight Checks

**Purpose**: Load and validate the project-wide config, verify MCP availability, resolve feature context, and register or resume the feature in the shared Stitch project.

---

## Project Init Gate

Before running per-feature preflight, check:

1. Does `.claude/docs/_project/stitch-project.json` exist?
2. Is `initState.completedAt` non-null?

If either check fails → **run Project Init now** by following `phases/phase-init.md` end-to-end. Return here when `initState.completedAt` is set.

If both pass → continue with per-feature preflight below.

---

## Checklist

```
Per-Feature Preflight Progress:
- [ ] Step 0.1: Load and validate project-wide config
- [ ] Step 0.2: Verify Stitch MCP availability
- [ ] Step 0.3: Resolve feature context
- [ ] Step 0.4: Resolve or register feature in project-wide config
- [ ] Step 0.5: Create per-feature docs directory
```

---

## Model Selection

The model is always `GEMINI_3_FLASH` for all Stitch generation calls. Device type is always `MOBILE`.

---

## Step 0.1: Load and Validate Project-Wide Config

**Purpose**: Load the shared Stitch project configuration and detect any design system drift.

1. Read `.claude/docs/_project/stitch-project.json`.

2. Call `mcp__stitch__get_project` with `name` set to `stitch-project.json.projectName` to verify the shared project is still accessible.
   - If 404 or project not found:
     ```
     The shared Stitch project ({projectId}) no longer exists. Re-run /ui-designer without
     arguments to run Project Init again. Existing blueprints and screenshots in .claude/docs/
     are preserved.
     ```
     **STOP** — do not proceed.
   - If valid: proceed.

3. Load the following into working context (do NOT re-read XTheme.kt here — deferred to Step 1.9):
   - `projectId` — from `stitch-project.json.projectId`
   - `projectName` — from `stitch-project.json.projectName`
   - `designSystemAssetId` — from `stitch-project.json.designSystem.assetId`
   - `xthemePath` — from `stitch-project.json.designSystem.xthemePath` (the discovered path to XTheme.kt, written by Init-2)
   - `defaultTheme` — from `stitch-project.json.designSystem.themeSnapshot.defaultTheme`
   - `primaryHex` — from `stitch-project.json.designSystem.themeSnapshot.primaryHex`
   - `paletteCustomized` — from `stitch-project.json.designSystem.themeSnapshot.paletteCustomized`

4. **Drift detection**: Read the file at `xthemePath`. Extract the `primary` hex from the scheme matching `defaultTheme`. Compare it against `stitch-project.json.designSystem.themeSnapshot.{defaultTheme}.primary`.

   If they differ, surface:
   ```
   Design system drift detected:
   XTheme.kt primary: {live hex} (updated since last sync)
   Stitch design system primary: {snapshot hex} (last synced: {syncedAt})

   Options:
   - Update Stitch design system to match XTheme.kt (recommended)
   - Ignore for this session
   ```
   Use `AskUserQuestion` with these two options.

   If user picks **Update**:
   - Call `mcp__stitch__update_design_system` with the new primary color and any other changed roles.
   - Update `stitch-project.json.designSystem.themeSnapshot.{defaultTheme}.primary` to the live hex.
   - Update `stitch-project.json.designSystem.syncedAt` to current ISO timestamp.
   - Update `stitch-project.json.updatedAt`. Write the file.

   If user picks **Ignore**: proceed without changes.

---

## Step 0.2: Verify Stitch MCP

**Required for ALL invocations.**

Attempt to call `mcp__stitch__list_projects`. This verifies the Stitch MCP server is configured and accessible.

**If successful**: Stitch MCP is available. Proceed.

**If fails** (tool not found or connection error): Stitch MCP is not configured. Run the **Guided Setup** procedure from [phase-init.md → Init-1](phase-init.md#guided-setup) — it walks the user through Node check, path selection (API Key vs OAuth), key acquisition, the `claude mcp add stitch …` command, and the required Claude Code restart. Full reference: `.claude/skills/ui-designer/references/stitch-setup.md`.

**STOP** — Stitch MCP cannot be activated within a running Claude Code session. The user must restart Claude Code and re-invoke `/ui-designer` before this skill can continue.

---

## Step 0.3: Resolve Feature Context

Extract feature information from arguments, user's request, or ask for it.

### Detect Feature Name

**Priority order:**
1. **$ARGUMENTS** — If the user invoked `/ui-designer productdetail`, use `$ARGUMENTS` directly as the feature name
2. **Parse from prompt** — "design the product detail screen" → `productdetail`
3. **Ask the user** — If neither source provides a clear feature name, use `AskUserQuestion`:
   - What is the feature name? (lowercase, no hyphens: e.g., `productdetail`)

### Check Feature Existence

Use the `Glob` tool with pattern `feature/{featurename}/src/commonMain/kotlin/**/*.kt` to check if the feature exists.

| Result | Meaning |
|--------|---------|
| Files found | Existing feature |
| No matches | New feature |

Note: Feature existence is informational — it helps the user decide which implementation skill to use after the blueprint is ready.

---

## Step 0.4: Resolve or Register Feature in Project-Wide Config

This is the **only place** feature ↔ screen identity is established. Never assume "latest screen in the shared project" is the right one — the project is shared across all features.

1. Look up `stitch-project.json.features[featurename]`.

2. If entry **exists**:
   - Call `mcp__stitch__get_screen` with the stored `successScreenId`:
     ```
     projectId = stitch-project.json.projectId
     name      = "projects/{projectId}/screens/{successScreenId}"
     screenId  = {successScreenId}
     ```
   - If 404 or screen not found: the screen was deleted from Stitch UI. Prompt:
     ```
     The success screen for '{featurename}' (screenId: {id}) no longer exists in Stitch.
     Options:
     - Regenerate: Re-run screen generation for this feature
     - Cancel
     ```
     Use `AskUserQuestion` with these options.
     If Regenerate: set `features[featurename].successScreenId = null` and `features[featurename].emptyScreenId = null` in `stitch-project.json`. Write the file. Continue as new-feature flow (step 3 below).
     If Cancel: stop.
   - If valid: set **StitchMode** to **stitch-resume**. Load `successScreenId`, `emptyScreenId` (may be null) into working context.

3. If entry **does not exist**:
   - Create a new entry in `stitch-project.json.features`:
     ```json
     "{featurename}": {
       "successScreenId": null,
       "successScreenName": null,
       "emptyScreenId": null,
       "states": { "loading": false, "failed": false, "empty": false },
       "screenshot": null,
       "htmlPath": null,
       "tokensPath": null,
       "dimensions": null,
       "designFile": null,
       "blueprintFile": null,
       "approved": false,
       "approvedAt": null,
       "createdAt": "{ISO timestamp}",
       "updatedAt": "{ISO timestamp}"
     }
     ```
   - Update `stitch-project.json.updatedAt`. Write the file.
   - **StitchMode**: **stitch-new**.

---

## Step 0.5: Per-Feature Docs Directory

Create the feature docs directory if it does not exist:
```bash
mkdir -p .claude/docs/{featurename}/designs
```

No per-feature `stitch.json` is created. All Stitch state lives in `.claude/docs/_project/stitch-project.json`.

---

## Output

After preflight completes, the following context is available for Phase 1:

```
Model ID: GEMINI_3_FLASH
Feature: {featurename}
Feature Exists: {yes|no}          ← Kotlin source files found? Drives which implementation skill to recommend at completion.
StitchMode: {stitch-new|stitch-resume}   ← Stitch design session state only. NOT a proxy for code existence.
Stitch Project ID: {projectId} (shared project)
Design System ID: {designSystemAssetId}
Docs Path: .claude/docs/{featurename}/
Designs Path: .claude/docs/{featurename}/designs/
Project Config: .claude/docs/_project/stitch-project.json
Default Theme: {defaultTheme}
Primary Color: {primaryHex}
```

Proceed to **Phase 1: Design in Stitch**.
