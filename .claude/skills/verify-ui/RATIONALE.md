# verify-ui: Rationale & Edge Cases

This doc captures the *why* behind verify-ui decisions. `SKILL.md` is the runbook — read it on every run. Read this only when an edge case stumps you, or when you're maintaining the skill itself.

---

## Why re-download HTML every run

Stitch URLs returned by `mcp__stitch__get_screen` are typically one-time use. The design at the moment of verification is the audit's authoritative input — preserving the HTML alongside the audit makes the audit reproducible later even if the design has changed upstream or the URL has expired.

Also: download states **sequentially**. Concurrent downloads can race the URL's single-use semantics.

---

## What `extract_tokens.py` extracts

The script walks every DOM element and lists every Tailwind class — guaranteed complete. It also extracts:

- **Tailwind config overrides** — project-specific custom colors, borderRadius, fontFamily (including array-valued fonts).
- **Global `<style>` rules** — body font-family, `font-variation-settings` for material-symbols, etc. These affect every matching element and would otherwise be invisible to a class-only audit.
- **Deterministic token interpretation per class** — every recognised Tailwind class is annotated inline with its dp/sp/color/percentage value (e.g. `mt-4 → margin-top: 16dp`, `bg-primary/10 → background: primary @ 10%`, `rounded-xl → corner-radius: 24dp` using the config override).

Auto-conversion rate is roughly 65%: spacing, sizing, font-size/weight, letter-spacing, line-height, border-radius, border-width, colors with opacity. Layout primitives (`flex`, `items-center`, `justify-between`), state variants (`hover:*`, `focus:*`), and unrecognised classes pass through unannotated — interpret those yourself.

The conversions are deterministic Tailwind rules + config-resolved colors (no LLM judgement needed) — trust the values directly.

---

## Why the X-components catalog is the third source

X-components are not black boxes. They enforce internal constraints (min sizes, default colors, hardcoded padding) that override or augment any parameters the feature passes in. These constraints are invisible to a parameter-level audit but directly affect what gets rendered. Examples: `XTopAppBar` always center-aligns, `XDialog` always 90% width, `XIconButton` defaults `containerColor = surface`.

Reading the cached catalog once per run replaces re-reading ~20 `X*.kt` files.

The audit has three sources: HTML inventory, code, and the catalog. The blueprint used to be a fourth source — see *Why we dropped the blueprint as an audit source* below.

---

## Why we dropped the blueprint as an audit source (with one exception)

The implementation blueprint is the design ground truth at *implementation time*. It is consumed by `/creating-kmp-feature` and `/modifying-kmp-feature` (design-aware mode) so the developer writes Compose that matches the design. By the time `/verify-ui` runs, most of the blueprint has already done its job.

Reading the blueprint's **token data** a second time in verify-ui caused three problems:

1. **It duplicated the HTML.** Every Design Token / Typography Scale / Spacing Grid value in the blueprint is derivable from the HTML (`text-xl → 20sp`, `mt-4 → 16dp`, `bg-primary` resolves through the Tailwind config). The blueprint column in past audits paraphrased either HTML or Code in every row — it never resolved a real disagreement.
2. **It defined verdicts that never fired.** `BLUEPRINT MISMATCH` and `BLUEPRINT + CODE MISMATCH` were both defined, both told the LLM to "fix the code using HTML," and neither produced a different action than the simpler `CODE MISMATCH` verdict.
3. **It was expensive.** ~5–7K tokens per run for an artifact that produced no extra signal.

So the token-level audit is `HTML ↔ Code` with the catalog as a render-behaviour overlay. Any conflict between the design and the implementation is reduced to one verdict: the code disagrees with the HTML, fix the code.

### The exceptions: Component Overrides (5.4) + Typography Updates Required (5.4b)

Two `Pre-Implementation Contract` tables are the **only** blueprint sections verify-ui consults — everything else is re-derived from the HTML.

**Component Overrides (Step 5.4)** records concrete X-component override decisions the blueprint generator made by reconciling the HTML inventory against `X_COMPONENTS_CATALOG.md` defaults — e.g. "this `XCard` needs `containerColor = surface` because the X default doesn't match the HTML hex."

This data is derivable in principle (HTML inventory + catalog), but the fixed 7-trap checklist in Step 5.3 deliberately does **not** walk every `X-component × every catalog property` combination — full sweeps were churn-y false-positive machines (see *Why Step 5.3 is a fixed Trap Checklist* below). The Component Overrides table is the catalog-style sweep, but pre-curated to the few rows that actually apply to **this** feature. Walking it costs ~N rows of work (typically 0–5) instead of the hundreds a full sweep generated.

**Typography Updates Required (Step 5.4b)** records the app-global type deltas — the font swap and any per-node type-scale role override. Verify-ui consults it for the same reason: a logged `style = …copy(fontWeight = …)` override is an intentional divergence the auditor must reconcile rather than flag. The font-family check itself reads the HTML typeface vs `XFontFamily()` directly (no blueprint needed); only the override reconciliation uses the table.

If the blueprint is missing or a table is empty, the corresponding step (5.4 / 5.4b) silently skips. The token audit + 5.3 trap checklist + the 5.4b font-family check still run as before.

---

## Regenerating the catalog

If `X_COMPONENTS_CATALOG.md` is missing or appears stale (e.g. references an X-component that doesn't exist, or a feature uses an X-component not listed), regenerate it by reading every `core/designsystem/src/commonMain/kotlin/**/X*.kt` file and extracting:

| What to look for | Why it matters |
|-----------------|----------------|
| `Modifier.defaultMinSize(minWidth=…, minHeight=…)` | Overrides effective rendered size regardless of contentPadding |
| Default parameter values for `colors`, `shape`, `elevation` | What renders when feature code doesn't override them |
| Hardcoded internal padding/spacing in the component body | Adds to or overrides contentPadding |
| `CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides …)` | Changes whether M3's touch-target minimum applies |
| Any `Modifier.padding(...)`, `.background(...)`, or `.fillMaxWidth(...)` applied inside the component | May override feature-level modifiers |
| Forced text styles via `CompositionLocalProvider(LocalTextStyle provides …)` | Caller's typography params get overridden |

Then update `X_COMPONENTS_CATALOG.md` so subsequent runs read the fresh version.

Theme palette values (XDarkColors / XLightColors / Shapes) are mirrored from `XTheme.kt` near the bottom of the catalog. Sync them manually when `XTheme.kt` changes — the previous auto-sync script (`sync_theme_tables.py`) was removed because the maintenance cost outweighed the benefit.

---

## Why `\b...\(` for the X-component grep

The grep in Step 4.2 uses `\b...\(` (word boundary + open paren) to catch X-components nested inside other calls (e.g. `trailingIcon = { XIconButton(...) }`), assigned to variables (`val btn = XButton(...)`), or chained on the same line as another expression. A start-of-line anchor (`^\s*`) would miss all of these and silently drop instances from the reverse-sweep map.

Also: preserve duplicates (no `sort -u`). Step 5.3 iterates every site, including repeated invocations of the same component.

---

## Effective rendered value (the rule used in 5.2)

```
effective rendered value = declared parameter, then overridden/constrained by X-component internals
```

Two worked examples:

- Feature declares `contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)` on `XButton`
  → `XButton` also applies `defaultMinSize(minWidth = 100.dp, minHeight = 44.dp)`
  → Effective rendered size is constrained to minimum 100dp × 44dp regardless of padding.
  → **Code column must show the effective size** (e.g. "min 100dp wide"), not just the declared padding.

- Feature passes no `colors` to `XIconButton`
  → `XIconButton` defaults to `containerColor = colorScheme.surface` → visible background rendered.
  → **Code column must show** "visible surface background", not "no color declared".

---

## Why Step 5.3 is a fixed Trap Checklist (not a full reverse sweep)

Step 5.2 is HTML-driven — one block per visible mismatch. It cannot catch properties the design omits but the X-component renders by **default** (e.g. `XIconButton` rendering a surface-colored circle when no `bg-*` class exists in HTML).

The bug that motivated this step:

- Instance: `RecipientAddressInput.kt:63` — `XIconButton(onPasteClick, contentPadding = PaddingValues(4.dp))`
- Catalog: `XIconButton.containerColor` defaults to `surface`
- HTML: paste button class list is `text-primary` only — no `bg-*` class
- Code: no `colors` parameter passed → default applies
- Verdict: emit a CRITICAL block flagging the visible default-rendered surface circle.

A full catalog-driven reverse sweep (walk every X-component instance × every catalog property) was the original design. It produced churn-y false positives: most catalog properties are theme defaults that match the design by coincidence, and the LLM had to relitigate each one. The Send audit took five attempts to converge.

The trap checklist replaces it with a fixed list of seven traps that have actually caused bugs:

1. `XIconButton` default `containerColor = surface`
2. `XTextField` `defaultMinSize(280dp × 48dp)`
3. `XTextField` extra `padding(top = 8.dp)` when `label != null`
4. `XTopAppBar` always centre-aligned title
5. `XDialog` always 90% width
6. `XPrimaryScrollableTabRow` no divider by default
7. `XRadioButton` unselected colour = `primary` (not `outline`)

These cover every X-component default-render bug we have actually seen. New traps get added here as they're discovered. Walking only the checklist (instead of every catalog row) cuts the false-positive churn that drove the multi-attempt convergence.

---

## Why layout-only HTML elements get a compact one-liner

`extract_tokens.py` splits emitted elements into two formats:

- **Visual elements** (any class converts to a visual token: padding, margin, gap, sizing, font, color, border, radius, opacity, shadow) → full block, one line per class with the dp/sp/color conversion.
- **Layout-only elements** (only structural classes: `flex`, `flex-col`, `items-center`, `justify-between`, `relative`, etc.) → single compact line like `- [42] <div> flex items-center justify-between`.

Why not drop layout-only elements entirely? Because they carry **structural information** the audit still needs:

- `flex-row` rendered as `Column` is a real bug.
- `justify-between` rendered as `Arrangement.Start` is a real bug.
- `items-center` rendered as `Alignment.Top` is a real bug.

These mismatches are usually obvious from a screenshot, which is why they're rare in practice — but verify-ui keeps them visible without paying per-class enumeration tokens for every wrapper div. The audit reads them in order and only emits a block when the structure disagrees with the code.

The summary line at the end of each inventory reports `Visual: N | Layout-only: M` so you can see at a glance how much structural scaffolding the screen has.

---

## Why preserve `designs/extracted/`

These files (HTML + token inventories) are the audit's authoritative inputs. Keep them so the audit report can be re-verified later without re-downloading from Stitch. Stitch URLs are one-time-use, and the upstream design may have changed by the time someone re-checks the audit.

---

## Optional: desktop screenshots

The earlier version of this skill always rendered the implemented screen headlessly via Compose Desktop's `ImageComposeScene`. That step has been removed from the runbook because:

- The token audit is text-based — pixels are never scored.
- The PNGs cost ~10–14K tokens to read inline, and the runbook explicitly told the LLM not to read them.
- Generating the test file + running Gradle adds ~30s and ~3–5K tokens of instructions per run.

If a human still wants a side-by-side, you can render screens manually:

1. Read screen dimensions from `stitch-project.json` (`features[{featurename}].dimensions`).
2. Create a temporary `desktopTest` file at `feature/{featurename}/src/desktopTest/kotlin/{PKG_PATH}/{featurename}/verification/VerificationScreenshot.kt`:
   - Imports: `ImageComposeScene`, `Density`, `EncodedImageFormat` from Compose/Skia.
   - `@OptIn(ExperimentalComposeUiApi::class)`.
   - Render at Stitch dimensions with `Density(2f)` (Stitch uses 2× CSS pixels for mobile).
   - Wrap in `XTheme(darkTheme = …)` matching the app theme.
   - One test per state.
3. Output path — Gradle sets `user.dir` to the module root; navigate up two parents:
   ```kotlin
   val projectRoot = File(System.getProperty("user.dir")).parentFile.parentFile
   val outputDir = File(projectRoot, ".claude/docs/{featurename}/designs/device").also { it.mkdirs() }
   ```
4. Run `./gradlew :feature:{featurename}:desktopTest --tests "*VerificationScreenshot*"`.
5. **Delete the test file afterwards** — temporary scaffolding, not source code.

Don't `Read` the resulting PNGs inline. The audit doesn't score against them.
