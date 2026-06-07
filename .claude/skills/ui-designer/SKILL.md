---
description: Design UI screens in Google Stitch and produce a Compose Implementation Blueprint with HTML + token inventories persisted for downstream skills.
argument-hint: [feature-name]
allowed-tools: Task, Read, Write, Edit, Glob, Grep, Bash(mkdir *), Bash(ls *), Bash(curl *), Bash(rm *), Bash(touch *), Bash(python3 *), AskUserQuestion, mcp__stitch__create_project, mcp__stitch__get_project, mcp__stitch__list_projects, mcp__stitch__list_screens, mcp__stitch__get_screen, mcp__stitch__generate_screen_from_text, mcp__stitch__edit_screens, mcp__stitch__generate_variants, mcp__stitch__create_design_system, mcp__stitch__update_design_system
---

# UI Designer

Design UI screens in Google Stitch and produce a Compose Implementation Blueprint with HTML + token inventories persisted for downstream skills.

**Architecture Reference:** @../_shared/patterns.md

## Purpose

`ui-designer` is a **design-only** skill. It handles all Stitch MCP design work and produces a Compose Implementation Blueprint plus persisted HTML + token inventories under `.claude/docs/{featurename}/designs/extracted/`. The blueprint references project-wide rules (`patterns.md`, `m3-colors.md`, `X_COMPONENTS_CATALOG.md`) rather than restating them. It does NOT implement code or invoke other skills.

After `ui-designer` completes, the user can invoke `/creating-kmp-feature` (new feature, no Kotlin source yet) or `/modifying-kmp-feature` (existing feature, Kotlin source already exists) to implement the design. Those skills auto-detect the blueprint and enter design-aware mode.

**Invocation modes:**
- `/ui-designer` (no args) — explicitly run Project Init for the repo
- `/ui-designer {featurename}` — design that feature's screen (Project Init auto-runs if needed)

## Quick Start (first feature)

1. **(First-time only)** Run `/ui-designer` with no arguments to initialize the shared Stitch project + design system from `XTheme.kt`. If you skip this, any future `/ui-designer {featurename}` call will auto-bootstrap.
2. Run `/ui-designer {featurename}` (e.g., `/ui-designer dashboard`) — one screen per invocation. Iterate via approve/edit. Produces a blueprint at `.claude/docs/{featurename}/designs/{featurename}_blueprint.md`.
3. Run `/creating-kmp-feature {featurename}` (new) or `/modifying-kmp-feature {featurename}` (existing) to implement — they auto-detect the blueprint.
4. Run `/verify-ui {featurename}` to audit code against the design.

## Tooling Preflight

Run this check **before any other work**, for every invocation (no-args init mode and `{featurename}` mode alike). It is cheap and must pass before Phase 0 or Project Init begin.

**Python 3** is required by two scripts invoked during Phase 1 Step 1.15:
- `.claude/skills/_shared/extract_tokens.py` — Stitch HTML token extraction (sub-step 4; also used by the shared-state generation paths in `phase-init.md`).
- `.claude/skills/_shared/download_assets.py` — generates the **icons manifest** (`--type icons`, sub-step 5) AND the **images manifest** (`--type images`, sub-step 6). `/ui-designer` always invokes it with `--manifest-only` (doc artifact only).

XML downloads, image downloads, `DesignSystemResources.kt` edits, and cross-feature Kotlin import rewrites all happen later in `/creating-kmp-feature` or `/modifying-kmp-feature` (which run the same script without `--manifest-only`).

> **MANDATORY — `download_assets.py` is the ONLY asset acquisition mechanism.** Never download, fetch, or write any icon/image asset yourself (no `curl`/`wget`/WebFetch to icon or image URLs, no hand-written SVG/XML/raster files). Material Symbols are font glyphs in the HTML, not files — never "source" an icon. The only sanctioned path is `.claude/skills/_shared/download_assets.py` (manifest-only in `/ui-designer`; full materialization in the implementation skills). If that script cannot produce an asset, **STOP and report** — do not improvise a download. Improvised fetches grab SVGs, which crash on Android at runtime.

```bash
python3 --version
```

If the command fails (`python3: command not found`, non-zero exit), **STOP** and tell the user:

```
Python 3 is not installed (or `python3` is not on PATH). /ui-designer extracts
design tokens via .claude/skills/_shared/extract_tokens.py and generates the
icons + images manifests via .claude/skills/_shared/download_assets.py
(--type icons | --type images). Cannot proceed without it.

Install Python 3, then re-invoke /ui-designer:
  - macOS:   brew install python3
  - Linux:   sudo apt-get install python3   (or your distro's package manager)
  - Windows: https://www.python.org/downloads/   (or `winget install Python.Python.3`)

Verify with: python3 --version
```

Do not retry or work around the failure — wait for the user to install Python 3 and re-invoke the skill.

## Workflow

```
[USER INVOKES] → Tooling Preflight (python3) → Phase 0 (Preflight) ──┐                                  ┌──→ Phase 1 (Design) → [USER APPROVES] → DONE
                                                                     │ (if stitch-project.json absent   │      Blueprint saved
                                                                     │  or initState.completedAt null)  │
                                                                     └──→ Project Init (phase-init.md) ─┘
                                                                          (auto-runs; one-time per repo)
```

### Project Init (One-Time Per Repo)
Run when `.claude/docs/_project/stitch-project.json` does not exist or `initState.completedAt` is null. Creates the shared Stitch project and design system. **Shared Loading/Failed screens are NOT generated at init time** — they are designed lazily by the first feature that opts in (Phase 1 Step 1.8).
See: [Project Init](phases/phase-init.md)

### Phase 0: Preflight Checks
Verify MCP availability, resolve feature context, load project-wide config, and register or resume the feature in the shared project.
See: [Phase 0: Preflight](phases/phase-0-preflight.md)

### Phase 1: Design in Stitch
Generate screens, iterate with user, export approved designs as screenshots. Generate Implementation Blueprint.
See: [Phase 1: Design](phases/phase-1-design.md)

## Critical Rules

1. **User Confirmation Required** after Phase 1 (design approval) - never proceed without explicit approval
2. **All design changes go through Stitch** - never modify designs outside of Stitch MCP tools
3. **Screenshots stored at** `.claude/docs/{featurename}/designs/` - visible to user
4. **Single config architecture**: All Stitch state lives in `.claude/docs/_project/stitch-project.json`. It is the source of projectId, shared screen IDs, per-feature screen metadata, per-feature state selections (`features[featurename].states`), and `blueprintConsumed`. There are no per-feature `stitch.json` files.
5. **Stitch MCP is mandatory** - if not available, stop and ask user to configure it
6. **Blueprint is the handoff artifact** — contains Pre-Implementation Contract + Post-Implementation Checklist. Implementation skills consume it via `blueprintConsumed` flag in `stitch-project.json.features[featurename]`
7. **`blueprintConsumed` lifecycle** — ui-designer sets `blueprintConsumed: false` in `stitch-project.json.features[featurename]` when saving a new blueprint. Implementation skills set it to `true` after consuming the blueprint
8. **M3 Color Roles Only** - All design colors must map to M3 roles defined in `XTheme.kt`'s `XLightColors` and `XDarkColors`. After design approval, a Color Audit identifies missing roles which are documented in the blueprint's Pre-Implementation Contract. Feature code uses `MaterialTheme.colorScheme.*` exclusively — never hardcoded `Color()`. Custom `XTheme.Colors.*` extensions are last resort for non-semantic colors (gradients, decorative effects).
9. **Project Init auto-runs when needed** — Phase 0 preflight checks for `.claude/docs/_project/stitch-project.json` before proceeding. If absent or `initState.completedAt` is null, Project Init runs automatically (following `phase-init.md` end-to-end), then Phase 0 resumes for the requested feature. Users may also invoke `/ui-designer` with no arguments to run Init explicitly.
10. **Cross-Screen Chrome Consistency** — When generating a screen for a project that already has approved features, the **shared chrome** (top app bar style, bottom navigation presence/style, screen background) must match the existing feature screens. The only exception is when the user **explicitly** asks for a different chrome ("no bottom nav", "centered title bar", "full-screen modal", etc.). The chrome snapshot is captured in Phase 1 Step 1.2 and injected as a "Shared Conventions" block into the Stitch generation prompt (Step 1.10).
11. **Optional States — User-Selected, Lazily Designed, Confirmable** — Only success is mandatory per feature. Loading/Failed/Empty are gated in Phase 1 Step 1.7:
    - **Loading / Failed** — user opt-in. Reuse the shared screens. **Project Init does NOT auto-generate shared Loading/Failed**; they're designed lazily by the first feature that opts in via **Step 1.8** (which invokes phase-init.md's On-Demand Procedures, before the feature's success screen). Every subsequent feature that opts in inherits them for free.
    - **Empty** — dual gate: user opt-in **AND** `isListBased == true` (determined in Step 1.1). When `isListBased == false`, the Empty option is not even offered. When both gates pass, design per-feature via a single approve-or-edit loop.
    - **Skipped states** are omitted everywhere: no token inventory, no implementation reference. Loading/Failed sections in the blueprint show an explicit "Skipped" marker; Empty is omitted entirely.
    - All shared and empty designs use the same single approve-or-edit loop pattern (max 10 iterations).

## Stitch MCP Reference

For Stitch tool usage patterns and prompt engineering tips:
See: [Stitch MCP Reference](references/stitch-guide.md)

## Error Handling

| Error | Action |
|-------|--------|
| Stitch MCP not available | Run the Guided Setup in [phase-init.md → Init-1](phases/phase-init.md#guided-setup). Full reference: [stitch-setup.md](references/stitch-setup.md). Stop after instructing the user — they must restart Claude Code before the skill can continue. |
| Stitch generation times out / connection reset | **Do NOT retry the generation call** (known Google Stitch bug — the generation often succeeded server-side and a retry creates a duplicate). Ask the user to open `https://stitch.withgoogle.com/projects/{projectId}` in their browser to trigger sync. Wait for confirmation, then call `list_screens` to locate the new screen. |
| Stitch generation fails with a non-timeout error | Retry with refined prompt. Max 3 attempts |
| Stitch project not found | Create new project automatically |
| stitch-project.json not found | Run Project Init first. Invoke `/ui-designer` without a feature name argument. |

## Completion Report

Emit one screenshot row per **selected** state only. Always show Success. Loading/Failed/Empty rows appear only when `stitch-project.json.features[{featurename}].states.{state} == true`.

```
## UI Designer Complete: {FeatureName}

Stitch Project ID: {projectId} (shared project)
Design System ID: {designSystemAssetId}
Project config: .claude/docs/_project/stitch-project.json

| State | Screenshot |
|-------|------------|
| Success | designs/{featurename}.png |
{if states.loading} | Loading | .claude/docs/_shared/designs/loading.png (shared) |
{if states.failed}  | Failed  | .claude/docs/_shared/designs/failed.png (shared) |
{if states.empty}   | Empty   | designs/{featurename}_empty.png |

States selected: success{, loading if states.loading}{, failed if states.failed}{, empty if states.empty}
States skipped (no design reference): {comma-separated list, or "none"}

Design spec: designs/{featurename}.md
Blueprint: designs/{featurename}_blueprint.md
Project config: .claude/docs/_project/stitch-project.json
blueprintConsumed: false (set in stitch-project.json.features[{featurename}])

## What's next

- Implementation skill will auto-detect the blueprint and enter design-aware mode.
- For skipped states, implementation will use generic handling (Rule 4 in patterns.md still applies — feature code must handle all four UI states, just without a design reference for the skipped ones).
- After implementation, run `/verify-ui {featurename}` to audit code against the design.

---

> **Next step —** run `/clear` to free the context window (the blueprint + `stitch-project.json` are durable artifacts — the implementation skill auto-detects and re-reads them fresh, so clearing loses nothing and frees the large Stitch/HTML context), then pick the implementation skill based on **`Feature Exists`** from Phase 0:
> - **`Feature Exists: no`** → `/creating-kmp-feature {featurename}` — no Kotlin source files exist; create the feature from scratch
> - **`Feature Exists: yes`** → `/modifying-kmp-feature {featurename}` — Kotlin source files already exist; add the design to the existing feature
>
> **Do not use `StitchMode` (stitch-new / stitch-resume) to make this decision** — that tracks Stitch design session state, not Kotlin code existence.
```
