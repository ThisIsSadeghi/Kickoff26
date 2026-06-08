# Home Screen

**Approved**: 2026-06-07

## Design Description

WC2026 Home screen — primary bottom-nav tab. Dark stadium atmosphere with:
- Full-bleed hero countdown card with gradient overlay and bundled stadium background image
- Countdown showing days/hours/minutes/seconds in trophy gold, horizontally centered with generous spacing
- Horizontal-scroll upcoming match cards (LIVE + scheduled), each with group badge, team flags/codes, score/VS, kick-off time
- Group A–H selectable tab row above a standings mini-table (Pos/Team/P/W/D/L/Pts)
- Bottom navigation bar (Home active, Matches, Profile)

## Visual Specifications

- Colors: all XDarkColors M3 roles + XTheme.Colors.Gold (#FFD700) for countdown + tertiary (#4FC3F7) for LIVE badge
- Typography: Outfit throughout (matches current XFontFamily)
- Layout: 16dp horizontal margins, 24dp section spacing, scrollable vertically
- Components: hero card, horizontal-scroll match cards, tab row, standings table, bottom nav

## Screenshots
- Success: `home.png`
- Loading: `.claude/docs/_shared/designs/loading.png` (shared)
- Failed: `.claude/docs/_shared/designs/failed.png` (shared)

<!-- COLOR_AUDIT:BEGIN -->
## Color Audit

Default theme for design: dark

### Defined M3 Roles (already in XDarkColors)
| Role | Hex (inventory) | Usage in Design |
|------|-----------------|-----------------|
| background | #0A1209 | screen background, hero gradient end |
| surface | #141E12 | match cards, standings table, nav bar |
| surfaceVariant | #1E3020 | flag circle bg, group badge bg, standings header |
| primary | #86E8AB | active nav tab, group badge text, standings leader border, Pts leader text, View All link, group tab active underline |
| onPrimary | #003919 | — |
| onSurface | #E2EEDF | section headers, team names, score, countdown labels |
| onSurfaceVariant | #A5C0A0 | muted text, team codes, kickoff time, standings meta, countdown sublabel |
| outlineVariant | #2E4A2C | card borders (@ 30–50% alpha), dividers, nav border |
| error | #FFB4AB | shared failed screen soccer icon |

### Missing M3 Roles (must add to BOTH XLightColors and XDarkColors before implementation)
| Role | Active Scheme Hex | Counterpart Scheme Hex | Usage in Design |
|------|-------------------|------------------------|-----------------|
| tertiary | #4FC3F7 | #0284C7 (derived light counterpart) | LIVE badge text + dot color |

### Custom Colors (XTheme.Colors.* — justified exceptions only)
| Name | Hex | Justification |
|------|-----|---------------|
| Gold | #FFD700 | Trophy gold — countdown numbers, score display, Pts leader. Non-semantic WC brand accent, no M3 role equivalent. Already added to XTheme.Colors. |
<!-- COLOR_AUDIT:END -->

<!-- TYPOGRAPHY_AUDIT:BEGIN -->
## Typography Audit

**Design typeface**: Outfit (static weights; route: github-variable in manifest but current XTheme ships static — no swap needed)
**Theme font**: Outfit (XFontFamily) — **matches current**

### Text node → M3 role
| Node (usage) | M3 Role | Measured (size/weight) | Override needed? |
|--------------|---------|------------------------|------------------|
| "KICKOFF 26" wordmark | titleLarge | 20sp / 900 Black | yes — `.copy(fontWeight = FontWeight.Black)` |
| "TOURNAMENT STARTS IN" label | labelSmall | 10sp / 700 Bold | yes — `.copy(fontSize = 10.sp)` |
| Countdown numbers (4D 11H 23M) | displaySmall | 36sp / 900 Black | yes — `.copy(fontWeight = FontWeight.Black)` |
| "June 11, 2026 · Mexico City" | bodySmall | 14sp / 400 Normal | no |
| Section headers ("Upcoming Matches", "Group Standings") | titleMedium | 18sp / 700 Bold | yes — `.copy(fontWeight = FontWeight.Bold)` |
| "VIEW ALL" link | labelSmall | 12sp / 600 SemiBold | yes — `.copy(fontWeight = FontWeight.SemiBold)` |
| Group badge pill ("GROUP A") | labelSmall | 10sp / 700 Bold | yes — `.copy(fontSize = 10.sp)` |
| "● LIVE" badge | labelSmall | 10sp / 900 Black | yes — `.copy(fontWeight = FontWeight.Black)` |
| Score ("2 - 1") | displaySmall | 30sp / 900 Black | yes — `.copy(fontSize = 30.sp, fontWeight = FontWeight.Black)` |
| Team code ("MEX", "USA") | labelSmall | 12sp / 700 Bold | no |
| Standings table content | bodySmall | 12sp / various | yes — per-cell weight overrides |
| Nav tab label | labelSmall | 10sp / 700 Bold | yes — `.copy(fontSize = 10.sp)` |
<!-- TYPOGRAPHY_AUDIT:END -->

<!-- MOTION_AUDIT:BEGIN -->
## Motion Audit

**Motion present**: yes

### Kept motion → Compose
| Element | Family | Compose primitive | Params (dur/easing/repeat/trigger) | Magnitude | Target file |
|---------|--------|-------------------|------------------------------------|-----------|-------------|
| LIVE dot pulse | Loading/Attention loop | `rememberInfiniteTransition` + `animateFloat` (alpha 1.0→0.3→1.0) | dur: 1000ms / EaseInOut / Forever / auto-start | opacity 1.0↔0.3 | feature motion/HomeMotion.kt |
| Countdown tick | Value-driven | `AnimatedContent` with slide-up transition on each digit change | dur: 300ms / EaseOut / on value change | slide 8dp up + fade | feature motion/HomeMotion.kt |

**Reduced motion**: all kept rows gated by `rememberReducedMotion()` (DS `XMotion.kt` — `expect/actual`, reads OS setting). Durations/easings via `XMotion` tokens, not ad-hoc `tween(<literal>)`.

**Dropped (interaction + web-only)**: `active:scale-[0.98]` (match card press), `transition-transform` on nav items, `hover:text-primary-container` on nav items, `active:scale-100` on nav tabs, `transition-colors` on header.
<!-- MOTION_AUDIT:END -->
