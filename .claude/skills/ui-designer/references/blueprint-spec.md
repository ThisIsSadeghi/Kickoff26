# Compose Implementation Blueprint Spec

The Stitch HTML export is parsed into a structured blueprint that provides exact component trees, design tokens, typography, and spacing for implementation.

**Condition**: Always generated after design approval. Used as the handoff artifact for implementation skills (paired with the persisted HTML + token inventories in `extracted/`).

---

## Blueprint Format

Single markdown file at `.claude/docs/{featurename}/designs/{featurename}_blueprint.md`:

```markdown
# Compose Implementation Blueprint: {FeatureName}

## Design Tokens

| Hex | M3 Role | Usage |
|-----|---------|-------|
| #0D0D0F | background | Screen background |
| #1A1A1F | surface | Card backgrounds |

## Typography Scale

> Typography is app-global (see `_shared/patterns.md` → "Typography"). Each text node maps to an **M3 type-scale role** exactly as each fill maps to an M3 color role — implementation uses `style = MaterialTheme.typography.{role}`, not raw `fontSize`. The `Size`/`Weight`/`Letter Spacing` columns are the design's measured values from the token inventory; they exist to (a) pick the closest role and (b) flag a divergence that needs an override or a theme change (recorded under *Typography Updates Required*). The design typeface (e.g. `Manrope`) and how it compares to the theme's current font are captured in the contract, not here.

| Usage | M3 Role | Size (sp) | Weight | Letter Spacing | Text Transform | Color Role |
|-------|---------|-----------|--------|----------------|----------------|------------|
| Screen title | titleLarge | 24 | Bold (700) | 0 | none | onBackground |
| Body text | bodyMedium | 14 | Normal (400) | 0.25 | none | onSurfaceVariant |

## Spacing Grid

| Context | Property | Value (dp) |
|---------|----------|------------|
| Screen | horizontal padding | 16 |
| Card list | gap between items | 8 |

## Component Tree

### Shared Screen container (all states)
- `XScreen` (Rule 13 — feature screens use `XScreen`, never `XScaffold`/`Scaffold`; the one Scaffold lives in `App.kt` and owns all insets)
  - topBar: `XTopAppBar` (title, navigationIcon: back arrow)
  - bottomBar: **[optional sticky CTA slot — `XScreen` bottomBar, NOT a Scaffold bottomBar]**
  - content: **[state-specific content slot]** (fills `XScreen`'s weight box; no `paddingValues` to thread)

> **Tab/navigation bottom bar is app-shell chrome, not a feature screen element.** If the design shows a bottom *navigation* bar, it belongs to the app shell (`App.kt`), rendered once via `XNavigationBar` and shared across all top-level (tab) features — do NOT add it to this feature's `XScreen`. A per-screen *sticky CTA* (e.g. a "Send" button) is different: it goes in this feature's `XScreen` `bottomBar` slot. Tab **icons** go in `composeApp/composeResources/drawable/` (app-module, alongside tab label strings — NOT in `icons.json` / `:core:designsystem`); tab **labels** go in `composeApp/composeResources/values/strings.xml`. Both are referenced via `{PROJECT_NAMESPACE}.composeapp.generated.resources.Res`. Implementation lives in Integration Point 5 (see creating-kmp-feature/architecture/integration.md), wired by `/creating-kmp-feature` or `/modifying-kmp-feature`, not by per-feature screen design.

### Success State
- `LazyColumn` (spacedBy 8.dp, contentPadding: 16.dp)
  - `{ComponentName}` (extracted named component)
    - `XCard` → `Row` → `AsyncImage` + `Column` with `XText` elements

### Loading State
Shared screen — see: `.claude/docs/_shared/designs/loading.png`
Token inventory: `.claude/docs/_shared/designs/extracted/tokens_loading.md`
- `Box` (fillMaxSize, Center) → `XCircularProgressIndicator`

> When `states.loading == false`, replace the section body with: **Skipped** (user opted out). Implementation must still handle Rule 4's Loading UI state; use a generic loading indicator.

### Failed State
Shared screen — see: `.claude/docs/_shared/designs/failed.png`
Token inventory: `.claude/docs/_shared/designs/extracted/tokens_failed.md`
- `Column` (fillMaxSize, center) → `XIcon` + `XText("Something went wrong")` + `XButton("Retry")`

> When `states.failed == false`, replace the section body with: **Skipped** (user opted out). Implementation must still handle Rule 4's Failed UI state; use a generic error indicator with retry.

### Empty State
- `Column` (fillMaxSize, center) → `XIcon` + `XText("No {items} yet")`

> Omit this entire section when `states.empty == false`. Empty is a content variant, not a Rule-4 UI state, so no "Skipped" placeholder is emitted.

## String Inventory

> Every user-facing text node in the design → a proposed string-resource key (Rule 12). The implementer creates `composeResources/values/strings.xml` from this table and references the keys via `stringResource(Res.string.*)`. **Exclude**: repository-supplied data (names, dates, tickers), single-glyph symbols (`$`, `₿`, `%`, `✓`), and control sentinels. Key naming: `{area}_{purpose}` snake_case (suffix `_template` for format strings, `cd_` for content descriptions, `section_` for headers, `status_` for badges).

| Key | Default (English) value | Where used | Notes |
|-----|-------------------------|------------|-------|
| {feature}_title | {Title} | top bar | |
| cd_back | Back | nav icon | content description |
| section_{name} | {Header} | section header | |
| {area}_{purpose}_template | {Text %1$s} | {component} | has format arg |

Shared strings (Retry / Yes / No / Cancel / common errors) are **not** listed here — they come from `DesignSystemResources`.

## Motion

> Every **kept** animation in the design → one row. Built from the Step 1.16 **Motion Audit** + the token inventory's `## Motion Inventory`. Buckets and the family→primitive mapping live in [`_shared/motion.md`](../../_shared/motion.md) — do **not** restate them; this table just records *which* element gets *which* primitive in *which* file. Omit the whole section when the design has no motion (static design).
>
> **KEEP rows only** (the 4 families: Ambient bg, Loading/Attention loop, Entrance, Value-driven). Touch press (`active:*`, `ripple`) and pointer/hover (`hover:*`, `group-hover:*`) feedback is **dropped** — never a row. List dropped sources once in the trailing note for transparency. Every kept row is gated by `rememberReducedMotion()` (state once below the table, not per row).

| Element | Family | Compose primitive | Params (dur / easing / repeat / trigger) | Magnitude | Target file |
|---------|--------|-------------------|------------------------------------------|-----------|-------------|
| {e.g. skeleton blocks} | Loading loop | `Modifier.shimmer()` | 2s / linear / infinite / on-screen | bg-position −200%→200% | DS `motion/` (`Modifier.shimmer`) |
| {e.g. each section} | Entrance | `AnimatedVisibility(fadeIn()+slideInVertically())` | 0.8s / ease-out-expo / once / first compose | translateY 30px→0; opacity 0→1 | feature `motion/{Feature}Motion.kt` |
| {e.g. balance counter} | Value-driven | `animateIntAsState` | spring / — / on value change | 0→target | feature `motion/{Feature}Motion.kt` |

> **Magnitude** = the animated value range (scale / translate / opacity / offset). Copy it verbatim from the token inventory's `### Keyframe magnitudes` — it is the **only** source for these amounts; do **not** invent them. Use `infer` only when the inventory marked it so.

**Reduced motion**: all rows gated by `rememberReducedMotion()` (DS `XMotion.kt`, an `expect/actual` reading the OS setting) — reduced ⇒ skip to end/target state. Durations/easings come from `XMotion` tokens, never ad-hoc `tween(<literal>)`.

**Dropped (interaction + web-only)**: {comma-separated classes/elements, e.g. `active:scale-95`, `.ripple`, `hover:bg-*`, `group-hover:*`} — no Compose output (per `_shared/motion.md` Web-Motion Policy).

## Pre-Implementation Contract

> Architecture rules, color rules, and X-component defaults are project-wide and live in their canonical sources — do not restate them here:
> - Architecture rules → [`_shared/patterns.md`](../../_shared/patterns.md)
> - Color rules → [`m3-colors.md`](m3-colors.md) (sections "Color Rules (Strict)" and "Complete M3 Role Catalog")
> - X-component default-render behavior → [`_shared/X_COMPONENTS_CATALOG.md`](../../_shared/X_COMPONENTS_CATALOG.md)
>
> This contract captures only feature-specific data the implementer cannot derive from those references.

### XTheme Updates Required

| Role | Active Scheme Hex | Counterpart Scheme Hex | Usage |
|------|-------------------|----------------------|-------|
| {role} | {hex} | {hex} | {usage} |

### Typography Updates Required

> Captures the app-global typography deltas the implementer cannot derive elsewhere — parallel to *XTheme Updates Required* (colors). Empty when the design's typeface matches `XTypography`'s current font and every node sits on a stock M3 role. From the Step 1.16 Typography Audit.

**Font swap** (emit only if the design typeface ≠ the theme's current `XFontFamily`):

| Design Family | Current Theme Family | Source (css2 URL / Google Fonts family) | Weights |
|---------------|----------------------|------------------------------------------|---------|
| {e.g. Manrope} | {e.g. Outfit} | {css2 URL from the HTML, or family name} | {regular,medium,bold} |

> Materialized by the implementation skill via `.claude/skills/_shared/download_font.py` → rewires `XFontFamily()` in `XTheme.kt`.

**Type-scale role overrides** (emit a row only when a node's measured size/weight diverges from the chosen M3 role's stock value enough to need an explicit override — otherwise the stock role is used as-is):

| Node | Chosen Role | Stock Role Value | Measured Value | Override |
|------|-------------|------------------|----------------|----------|
| {usage} | {role} | {e.g. 22sp/Normal} | {e.g. 24sp/Bold} | {e.g. `style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)`} |

### Color Audit

#### Defined Roles
| Role | Hex | Usage |
|------|-----|-------|
| {role} | {hex} | {usage} |

#### Missing Roles (must add before implementation)
| Role | Active Scheme Hex | Counterpart Scheme Hex | Usage |
|------|-------------------|----------------------|-------|
| {role} | {hex} | {hex} | {usage} |

#### Custom Colors (justified exceptions only)
| Name | Hex | Justification |
|------|-----|---------------|

#### Component Overrides (divergences from X-component defaults)

> **Audit-aware**: `/verify-ui` reads this table directly (its only blueprint dependency). Every row here is a CRITICAL check at audit time — if the override is missing in code, verify-ui flags it. Keep one row per concrete divergence between the HTML inventory and the X-component default in `X_COMPONENTS_CATALOG.md`.

| Component | Property | HTML Value | X-component Default | Override Required |
|-----------|----------|-----------|-------------------|------------------|

## Post-Implementation Checklist

- [ ] Font swap (if any) applied: `.ttf` downloaded to `core/designsystem/.../composeResources/font/` and `XFontFamily()` in `XTheme.kt` rewired; `:core:designsystem` builds
- [ ] Every text node uses `style = MaterialTheme.typography.{role}` (or an `XTextDefaults` preset) — no raw `fontSize`/`fontWeight` except the recorded *Type-scale role overrides*
- [ ] All XTheme missing roles added to BOTH XLightColors and XDarkColors
- [ ] Every component in blueprint Component Tree exists in implementation
- [ ] Every Modifier in blueprint (border, shadow, alpha, padding, size) is present in code
- [ ] Every image rendered per `images.json` `delivery`: `bundled` → `Image(painterResource(res_reference))`; `remote` → `AsyncImage(url = <data field>, loadingResId = DesignSystemResources.drawable.ds_image_placeholder)` with each `data_binding` wired on the DTO/`*UiModel` — no Stitch CDN URL in code (one sub-item per `remote` image)
- [ ] All colors use MaterialTheme.colorScheme.{role} — no raw Color() hex
- [ ] Component override sizes/colors from Pre-Implementation Contract applied
- [ ] Every String Inventory key exists in `composeResources/values/strings.xml` and is referenced via `stringResource` — no hardcoded display literals (Rule 12)
- [ ] Every `## Motion` row implemented in a `motion/` file with the declared primitive (generic → DS `motion/`, feature-specific → `presentation/ui/motion/{Feature}Motion.kt`) — never inline in `Screen.kt`/components; durations/easings match; loops use `rememberInfiniteTransition`; all gated by `rememberReducedMotion()`; no interaction/hover motion present (omit if no `## Motion` section)
- [ ] Build passes: `./gradlew :feature:{featurename}:assembleAndroidMain`
- [ ] Code formatted: `./gradlew :feature:{featurename}:ktlintFormat`
```

---

## Decomposition Rules

| Condition | Style |
|-----------|-------|
| HTML body ≤150 lines AND ≤3 visual sections | **Flat** — one component tree per state, everything in `{Feature}Screen.kt` |
| HTML body >150 lines OR >3 sections | **Decomposed** — skeleton with `[slot]` placeholders + individual component specs |

**Repeated patterns** (2+ occurrences with same structure) must be extracted as named components.

**For decomposed blueprints**, annotate each named component with its target file based on this rule:
- **`{Feature}Screen.kt`** — structural glue: state routing + top-level layout scaffold. Loading/Failed route to the shared `{PKG_PREFIX}.designsystem.app.AppLoadingState`/`AppErrorState` (one per project, not per-feature); only an optional `EmptyContent` shell may live here, and only if the design needs a dedicated empty screen
- **`components/{ComponentName}.kt`** — self-contained UI units: composables that have meaning independently of the screen, own their internal structure, or have private sub-composables/helper functions

Example annotation in the blueprint component tree:
```
// → {Feature}Screen.kt
- LazyColumn scaffold (sequences all sections)

// → components/ProductCard.kt
- ProductCard (owns image, title, price layout + ratingColor helper)
```

---

## Extraction Prompt Template

Feed this prompt with:
1. Raw HTML content for **selected states only** (labeled by state) — read `features[featurename].states` to know which states are selected:
   - success: `.claude/docs/{featurename}/designs/extracted/stitch_success.html` (always)
   - loading: `.claude/docs/_shared/designs/extracted/stitch_loading.html` **(shared state — include only if `states.loading == true`)**
   - failed: `.claude/docs/_shared/designs/extracted/stitch_failed.html` **(shared state — include only if `states.failed == true`)**
   - empty: `.claude/docs/{featurename}/designs/extracted/stitch_empty.html` (include only if `states.empty == true`)
2. **Token inventories** from `extract_tokens.py` for the same selected states (labeled by state) — authoritative for already-converted classes
   - success: `.claude/docs/{featurename}/designs/extracted/tokens_success.md` (always)
   - loading: `.claude/docs/_shared/designs/extracted/tokens_loading.md` **(shared — only if `states.loading == true`)**
   - failed: `.claude/docs/_shared/designs/extracted/tokens_failed.md` **(shared — only if `states.failed == true`)**
   - empty: `.claude/docs/{featurename}/designs/extracted/tokens_empty.md` (only if `states.empty == true`)
3. Icons manifest at `.claude/docs/{featurename}/designs/extracted/icons.json` (from Phase 1 Step 1.15 sub-step 5, written in manifest-only mode by `/ui-designer`) — authoritative for every Material Symbols `<span>` in the design; resolves each to its predicted drawable path and the exact Compose `res_reference` the blueprint must emit. Every entry's `download_status` is `pending` at design time; implementation skills will flip it to `downloaded` when they materialize the file.
3b. Images manifest at `.claude/docs/{featurename}/designs/extracted/images.json` (from Phase 1 Step 1.15 sub-step 6, also manifest-only) — authoritative for every `<img>` tag. Each entry carries a **`delivery`** field that decides how the blueprint renders it:
   - **`delivery: "bundled"`** (static design asset — hero, decorative background, logo) → `Image(painter = painterResource({res_reference}))`. The raster is downloaded + bundled by the implementation skill.
   - **`delivery: "remote"`** (dynamic content — avatar, flag, thumbnail, repeated list image) → `AsyncImage(url = {data_binding}, loadingResId = DesignSystemResources.drawable.ds_image_placeholder, …)` using the **design-system `AsyncImage`** (`url=` param). The Stitch CDN URL is an ephemeral placeholder and is **never** bundled or shipped; the runtime URL comes from the data layer (the `data_binding` field is a suggested DTO/UiModel field name). Each remote image adds a Post-Implementation Checklist item to wire that field.
4. X-component mapping table (from [stitch-guide.md](stitch-guide.md#mapping-stitch-designs-to-kmp-x-components))
5. Color Audit M3 role mappings (from Phase 1 Step 1.16)
6. The `states` map (`{ loading, failed, empty }`) from `stitch-project.json.features[featurename]` — used to decide which state sections the blueprint emits (see rule 22 below)
7. Typography Audit (from Phase 1 Step 1.16) + `fonts.json` (from Phase 1 Step 1.15 sub-step 6b) — the design typeface, its comparison to the theme's current font (match vs swap), and the per-node M3 type-scale role mapping. `fonts.json` records the font source (css2 URL / family) for the swap.

```
You are a design-to-code translator. Convert this Stitch HTML export into a Compose Implementation Blueprint.

INPUT:
- HTML content (Tailwind CSS classes encode all visual properties)
- Token inventories (one per state) from extract_tokens.py — pre-resolves dp/sp/colors deterministically
- X-Component mapping table (maps HTML elements to Compose X-components)
- Color Audit (maps hex colors to M3 roles)

RULES:
1. Use the dp/sp/color values from the token inventory directly. The script handles px→dp,
   font-size→sp, opacity slashes (`bg-x/N`), arbitrary values (`text-[40px]`), tailwind config
   color resolution, and custom border-radius config. Do NOT re-derive these from raw HTML.
   Only consult the HTML directly when the inventory leaves a class unannotated (the script's
   auto-conversion rate is ~65%; the rest are layout primitives and unrecognised classes).
2. Map structural Tailwind classes to Compose containers and arrangements (the script does not
   handle these — they require translation, not conversion):
   - flex/flex-col → Column, flex-row → Row
   - items-center → CenterVertically/CenterHorizontally
     ⚠ Compose caveat: In CSS, `items-center` on a full-width flex container centers
       children automatically. In Compose, `CenterHorizontally` is a no-op unless the
       Column itself has `fillMaxWidth()`. Similarly, `textAlign = TextAlign.Center` on
       XText is invisible unless that XText has `fillMaxWidth()`. When the HTML has
       `flex-col items-center text-center`, translate as:
       Column(Modifier.fillMaxWidth(), horizontalAlignment = CenterHorizontally)
       with fillMaxWidth() on each XText child.
   - justify-center → Arrangement.Center, justify-between → Arrangement.SpaceBetween
   - gap-{N} / space-y-{N} → Arrangement.spacedBy(...) — value already in inventory
   - w-full → Modifier.fillMaxWidth()
   - overflow-y-auto → LazyColumn or verticalScroll
   - grid grid-cols-{N} → LazyVerticalGrid(columns = GridCells.Fixed(N)). If grid items have
     different heights (no uniform aspect-* or h-*), use LazyVerticalStaggeredGrid instead.
   - grid gap-{N} → horizontalArrangement + verticalArrangement spacedBy(...)
   - fixed/sticky bottom → `XScreen` bottomBar slot (Rule 13 — never a Scaffold bottomBar in a feature)
   - shadow-sm/shadow/shadow-md/shadow-lg/shadow-xl/shadow-2xl → Modifier.shadow(elevation, shape).
     Map: sm=1dp, default=2dp, md=4dp, lg=8dp, xl=12dp, 2xl=16dp.
     Custom `shadow-[...]` glow effects → note as `[decorative shadow — omit or use drawBehind]`
   - shrink-0 → no effect if using fixed-size Modifier.size(); for flex children use weight()
   - divide-y divide-{color} → XHorizontalDivider between items with explicit color param
   - mb-{N} on LazyColumn items → absorbed by spacedBy. Standalone: Spacer or padding
   **Compound padding override** (judgment, not in inventory): when shorthand + directional
   coexist (e.g., `p-4 pt-8`), the directional class overrides ONLY that side — other sides
   keep the shorthand value. The inventory lists each class separately; the LLM must compose
   them into a single PaddingValues(...) call.
3. Map HTML elements to X-components using the provided mapping table.
4. Map all colors to M3 roles using the Color Audit. Use MaterialTheme.colorScheme.{role}, never hex.
4b. Map all typography to M3 type-scale roles using the Typography Audit. Every text node → `style = MaterialTheme.typography.{role}` (or an `XTextDefaults` preset), never raw `fontSize`. Fill the `## Typography Scale` table's `M3 Role` column for each node. Only emit a raw `fontSize`/`fontWeight` (as a `.copy(...)` override) when a node's measured size/weight diverges enough from the chosen role's stock value — and record that override in the contract's *Type-scale role overrides* table. Do not invent per-feature font families: the typeface is app-global.
4b. **Component visual fidelity verification (MANDATORY for every component)**:
    For every component in the blueprint, verify two things — never assume X-component defaults match:
    (a) **Colors**: Use the inventory's resolved color value for every visual state of every component (bg-*, text-*, border-*, etc.). Look up the M3 role via the Color Audit, then verify that role's hex in `XTheme.kt` equals the inventory hex. If it matches → use `MaterialTheme.colorScheme.{role}`. If it diverges → write an explicit color override using the inventory's hex, annotated with the mismatch reason.
    (b) **Sizing**: Use the inventory's resolved dp values for every component. Compare to the X-component's actual rendered default. If they differ → write an explicit size override in the blueprint.
    The principle: the inventory (derived from the HTML) is the source of truth. X-component defaults are assumptions that must be verified, not trusted.
5. Identify shared scaffold common across all states — describe ONCE.
6. Per state, describe only the differing content area.
7. Extract repeated patterns (2+ occurrences) as named components.
8. Output ONLY the blueprint markdown.
9. **Wrapper container padding propagation**: When a wrapper element (div/main/section
   that contains a list or scrollable content) has px-{N}/py-{N}/p-{N}, do NOT move
   that padding down to each child. Apply it as `contentPadding` on the LazyColumn/LazyRow,
   or as `Modifier.padding()` on the outer Column/Box. Children start from zero offset.
   Example: outer div with px-6 wrapping cards →
     LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp))
   NOT → each card with Modifier.padding(horizontal = 24.dp)
10. **Explicit background on every colored surface**: Every element with an explicit
    background color (bg-{color}) must get Modifier.background(MaterialTheme.colorScheme.{role})
    in Compose. CSS backgrounds are not inherited by child composables — if a card has
    bg-surface, the Compose Column/Box for that card MUST declare .background(surface)
    explicitly, even if the parent already has a different background set.
11. **Border radius**: Use the dp value from the inventory directly — the script resolves
    `tailwind.config.borderRadius` overrides and falls back to standard Tailwind defaults when
    no custom config exists. `rounded-full` resolves to `CircleShape`. Map the inventory's dp
    value to `RoundedCornerShape(N.dp)`.
12. **System inset padding**: Stitch HTML assumes padding starts from the screen edge (no system
    bars). On device, the **app-shell `Scaffold`** (`App.kt`) already pads the whole NavHost by the
    system bar insets (status bar + navigation bar) — `XScreen` and `XTopAppBar` add **none** (Rule 13).
    When the HTML has a top padding like `pt-6` (24dp) or `pt-12` (48dp) on the outermost container,
    that value INCLUDES the status bar area. In Compose, subtract the system inset since the shell
    already handles it. Typically use `8.dp` top padding for the first content element.
13. **Image mapping**: how a Stitch HTML `<img>` is rendered depends on the images-manifest
    `delivery` field (rule 21b below) — the Stitch CDN URL (`lh3.googleusercontent.com/aida-public/*`)
    is **never** emitted in code either way (it is an ephemeral placeholder).
    - **`delivery: "bundled"`** (static design asset) → `Image(painter = painterResource({res_reference}))`.
    - **`delivery: "remote"`** (dynamic content) → `AsyncImage(url = {data_binding}, loadingResId = DesignSystemResources.drawable.ds_image_placeholder, …)` (design-system `AsyncImage`, `url=` param), bound to a data-layer field — never to the Stitch URL. Add a Post-Implementation Checklist item to wire `{data_binding}`.
    In the blueprint, note the image dimensions from the HTML
    (`w-full h-[200px]` → `Modifier.fillMaxWidth().height(200.dp)`),
    shape (`rounded-full` → `Modifier.clip(CircleShape)`, `rounded-lg` →
    `Modifier.clip(RoundedCornerShape(...))`), and content scale
    (`object-cover` → `ContentScale.Crop`, `object-contain` → `ContentScale.Fit`).
    Pass the manifest's `alt` field as `contentDescription`.
14. **Bottom sheet mapping**: When Stitch HTML shows a bottom sheet pattern (a container pinned to
    the bottom with `rounded-t-*` top corners, a drag handle bar, and a semi-transparent overlay/scrim),
    implement it using `XModalBottomSheet` — NOT a custom `Box` overlay. `XModalBottomSheet` wraps
    M3's `ModalBottomSheet` and provides drag-to-dismiss gesture, smooth animation, proper scrim,
    and accessibility out of the box. Extract only the sheet CONTENT into a separate composable.
    The sheet's shape, containerColor, and scrimColor are set via `XModalBottomSheet` params.
15. **Pager mapping**: When Stitch HTML shows pagination indicators (a row of dots/pills), step
    counters ("Step X of Y"), or back/next navigation buttons together, this is a **paged flow** —
    implement as `HorizontalPager` with `rememberPagerState`, NOT as a static `Column`. The HTML
    only shows one static page, but the indicators signal multiple swipeable pages. Page indicator
    dots sync with `pagerState.currentPage`. Back/Next buttons use `pagerState.animateScrollToPage()`.
    Create a data list for all pages and render each inside the pager lambda.
16. **Custom div patterns for interactive components**: Stitch renders interactive form components as
    styled divs. Recognize these HTML patterns and map to the correct interactive X-component:
    - **Slider**: A thin horizontal bar (`h-1 rounded-full`) with circular thumb elements
      (`h-5 w-5 rounded-full`) positioned along it → `XSlider`, NOT nested `Box` composables.
      The visual shows track + thumb, but the implementation needs drag interaction.
    - **Dropdown trigger**: A row with text + `expand_more`/`chevron_down` Material icon →
      `XExposedDropdownMenuBox` anchor with `XDropdownMenuItem` menu, NOT a plain clickable `Row`.
      The visual shows a static selector, but the implementation needs a popup menu.
17. **Independent state parsing**: Each state screen has its own HTML file with its own
    tailwind config. Do NOT carry values (border radius, padding, colors, font sizes)
    from one state's HTML to another. Re-read the `tailwind.config` `<script>` tag and
    CSS classes independently for each state file. A failed state may define
    `rounded-card: 12px` while the success state uses `rounded-xl: 1.5rem` — these are
    different values that must be translated separately.
18. **No silent omissions**: The mapping table above is not exhaustive. For ANY Tailwind
    class not listed, look up its CSS value and translate to the equivalent Compose modifier.
    Never silently skip a class. If a non-motion class has no Compose equivalent (e.g.,
    `cursor-pointer`), note it as `[omitted: {class} — no Compose equivalent]` in a comment.
    **Motion/animation classes are not "no Compose equivalent" omissions** — route them per
    rule 23 below (kept families → the `## Motion` table; interaction/hover → the Dropped note).
    Every visual property in the HTML must appear in the blueprint — either as a Compose value,
    an explicit omission note, or a `## Motion` row / Dropped entry.
19. **Pre-Implementation Contract**: After the Component Tree, emit a `## Pre-Implementation Contract`
    section. **Do not restate** project-wide architecture rules, color rules, or X-component defaults —
    they live in `_shared/patterns.md`, `m3-colors.md`, and `_shared/X_COMPONENTS_CATALOG.md`. Open the
    section with the boilerplate reference block from blueprint-spec.md, then include only the
    feature-specific tables:
    - **XTheme Updates Required**: Every M3 role from the Color Audit that is missing from XTheme.kt,
      with hex values for both active and counterpart schemes.
    - **Typography Updates Required**: From the Typography Audit — a *Font swap* row only if the design
      typeface differs from the theme's current `XFontFamily` (family + source + weights), and a
      *Type-scale role overrides* table for any node whose measured size/weight diverges from its M3 role.
      Omit a sub-table when empty.
    - **Color Audit**: Full color audit tables (Defined, Missing, Custom).
    - **Component Overrides**: One row per concrete divergence between the HTML inventory and the
      X-component default in `X_COMPONENTS_CATALOG.md`. `/verify-ui` reads this table directly and
      treats each missing override as a CRITICAL — keep it accurate and minimal.
20. **Post-Implementation Checklist**: After the Pre-Implementation Contract, emit a
    `## Post-Implementation Checklist` with verification items: XTheme updates, component completeness,
    modifier fidelity, color fidelity, component override application, image delivery (bundled
    `painterResource` / remote `AsyncImage` with each `data_binding` wired — one sub-item per remote
    image), string-key coverage, build validation, ktlint format.
20a. **String Inventory (Rule 12)**: After the Component Tree (before the Pre-Implementation Contract), emit a
    `## String Inventory` table — one row per user-facing text node in the HTML, mapping it to a proposed
    `{area}_{purpose}` string-resource key + the default English value. Exclude repository data, single-glyph
    symbols, and control sentinels. Do not list shared strings (Retry/Yes/No/common errors) — they come from
    `DesignSystemResources`. The implementer builds `composeResources/values/strings.xml` from this table.
21. **Material Symbols icons**: For every `<span class="material-symbols-*" data-icon="...">` in the HTML, look up the icon in the `icons.json` manifest and emit `XIcon(painter = painterResource({res_reference}))` using the manifest's exact `res_reference` value (e.g. `DesignSystemResources.drawable.arrow_back` for chrome icons, `Res.drawable.qr_code_scanner` for domain icons). The manifest already encodes the fill-variant naming (`_fill` suffix when `data-weight="fill"`) and the chrome/domain split — do not re-derive these from the HTML. **Never** emit `Icons.Default.{Name}` or any reference to `androidx.compose.material.icons.*` — that library is pinned/deprecated and produces a different glyph family from the Material Symbols Stitch renders.

   > **"Font glyph" is a Stitch HTML rendering detail, not a Compose implementation instruction.** Stitch renders Material Symbols as `<span>` font glyphs in HTML — that is how the web preview works. In Compose, every Material Symbol is a vector XML file downloaded via `download_assets.py` and referenced via `painterResource(res_reference)`. The blueprint must NEVER say "No XML download needed for font glyphs" or "use `Icons.Default.*`" — those statements are always wrong regardless of how the icon is rendered in the HTML. If an icon appears in the HTML as a `<span>` but is absent from `icons.json`, the manifest is incomplete (Step 1.15 missed it) — STOP and add it to `icons.json` manually before generating the blueprint.
21b. **`<img>` raster assets**: For every `<img src="https://lh3.googleusercontent.com/aida-public/...">` in the HTML, look up the entry in the `images.json` manifest (keyed by `url`) and branch on its **`delivery`**:
   - **`delivery: "bundled"`** → `Image(painter = painterResource({res_reference}), contentDescription = "{alt}", contentScale = ContentScale.Crop)` using the manifest's exact `res_reference`.
   - **`delivery: "remote"`** → `AsyncImage(url = {data_binding}, loadingResId = DesignSystemResources.drawable.ds_image_placeholder, contentDescription = "{alt}", contentScale = ContentScale.Crop)` using the design-system `AsyncImage` (`import {CORE_DESIGNSYSTEM_PKG}.AsyncImage`; `url=` param, **not** `coil3 model=`). Bind `url` to the manifest's suggested `data_binding` field (a data-layer field), **never** to the Stitch CDN URL, and add a Post-Implementation Checklist item to wire it.

   For both: pass the manifest's `alt` as `contentDescription` (truncate if very long) and apply CSS-derived modifiers from the HTML (size, shape, opacity) as Compose `Modifier` chains. The Stitch CDN URL itself is **never** emitted in code.
22. **Optional states**: The `states` map controls which sections appear in the Component Tree:
    - `states.loading == false` → emit the "Skipped" form of the Loading State section (see template).
    - `states.failed == false` → emit the "Skipped" form of the Failed State section.
    - `states.empty == false` → **omit the Empty State section entirely** (empty is a content variant, not a Rule-4 UI state).
    Only consume HTML/token inputs for states that are selected; do not invent content for skipped states.
23. **Motion**: Build the `## Motion` table from the token inventory's `## Motion Inventory` + the
    Motion Audit. Bucket every token via the Web-Motion Policy in [`_shared/motion.md`](../../_shared/motion.md)
    (do **not** restate the policy or the family→primitive mapping — link it):
    - **DROP** touch press (`active:*`, `ripple`) and pointer/hover (`hover:*`, `group-hover:*`,
      `.interactive-card:hover/:active`, `focus:`, `cursor-*`) — never a row; list them once in the
      table's trailing **Dropped** note for transparency.
    - **KEEP** the 4 families (Ambient bg, Loading/Attention loop, Entrance, Value-driven) — one row each:
      `Element | Family | Compose primitive | Params | Magnitude | Target file`. Pick the primitive from
      motion.md's mapping; pick the target file per the code-layout rule (generic, reusable → DS `motion/`;
      one-off, feature-specific → `presentation/ui/motion/{Feature}Motion.kt`).
    - **Magnitude** column: copy the animated value range (scale/translate/opacity/offset) **verbatim** from
      the inventory's `### Keyframe magnitudes`. This is the only source for these amounts — never invent them.
    - **Params** durations/easings come from `XMotion` tokens (motion.md), never ad-hoc `tween(<literal>)`.
    - Every kept row is gated by `rememberReducedMotion()` — an `expect/actual` reading the OS setting
      (state once below the table).
    - Omit the entire `## Motion` section when the inventory shows no motion (static design).

X-COMPONENT MAPPING TABLE:
{paste from stitch-guide.md}

COLOR AUDIT:
{paste from design description .md}

ICONS MANIFEST:
{paste contents of .claude/docs/{featurename}/designs/extracted/icons.json}

IMAGES MANIFEST:
{paste contents of .claude/docs/{featurename}/designs/extracted/images.json}

TOKEN INVENTORIES:
{paste contents of .claude/docs/{featurename}/designs/extracted/tokens_{state}.md for each state, labeled by state}

HTML CONTENT:
{paste all state HTML files, labeled by state}
```

---

## Edge Case Handling

| HTML Element | Blueprint Representation |
|-------------|--------------------------|
| `<svg>`/`<canvas>` (icon-like) | `XIcon` with descriptive name |
| `<svg>`/`<canvas>` (decorative) | `[Canvas/Path decoration]` with description |
| CSS transitions/animations (`transition-*`, `animate-*`, `@keyframes`, tailwind `animation` config) | A `## Motion` row in the kept family + primitive from [`_shared/motion.md`](../../_shared/motion.md) (see rule 23). Touch press / pointer-hover variants are **dropped** to the Motion table's trailing note, not given a row. Never `[omitted — no Compose equivalent]`. |
| CSS gradients | `Brush.linearGradient(...)` with direction based on CSS angle |
| `<svg><circle>` with `stroke-dasharray`/`stroke-dashoffset` | `XCircularProgressIndicator(progress = { 1 - (dashoffset / dasharray) })` in a `Box` with text overlay |
| `<img>` tags (Stitch CDN `lh3.googleusercontent.com/aida-public/*`) | Branch on the `extracted/images.json` `delivery` field. **`bundled`** → `Image(painter = painterResource({res_reference}), contentDescription = "{alt}", contentScale = ContentScale.Crop, modifier = ...)` (static design asset to bundle). **`remote`** → `AsyncImage(url = {data_binding}, loadingResId = DesignSystemResources.drawable.ds_image_placeholder, contentDescription = "{alt}", contentScale = ContentScale.Crop, modifier = ...)` (dynamic content; `url` bound to a data-layer field, **never** the Stitch URL; design-system `AsyncImage`, `url=` param). The Stitch CDN URL is never emitted in code either way. |
| Material Symbols `<span class="material-symbols-*" data-icon="{name}">` | `XIcon(painter = painterResource({res_reference}))` — look up `{res_reference}` in `extracted/icons.json` (chrome icons → `DesignSystemResources.drawable.{name}`; domain icons → `Res.drawable.{name}`). The manifest is authoritative. Chrome vs domain is decided declaratively by cross-feature usage count; the XML files themselves are materialized by `/creating-kmp-feature` or `/modifying-kmp-feature` in design-aware mode (not by `/ui-designer`). Filled variants (HTML has `data-weight="fill"`) get a `_fill` suffix in the resource name. **Never fall back to `Icons.Default.*` from the deprecated `material-icons-extended` library.** |
| `position: fixed` bottom | `XScreen` `bottomBar` slot (Rule 13 — never a Scaffold bottomBar in a feature) |
| CSS `box-shadow` | `Modifier.shadow(elevation, shape)` |
| `opacity: {N}` | `Modifier.alpha({N}f)` |
| `bg-clip-text text-transparent bg-gradient-to-*` | `XText(style = TextStyle(brush = Brush.linearGradient(...)))` — NOT a `Box` with gradient background behind transparent text |
| Multiple `<circle>` elements on same `<svg>` with different `stroke` colors and `stroke-dasharray` | `Canvas { drawArc() }` per segment — NOT multiple `XCircularProgressIndicator` stacked |
