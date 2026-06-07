# M3 Color Roles Reference

**Single source of truth**: `XTheme.kt` — the actual repo path is discovered at Init-2 and persisted as `stitch-project.json.designSystem.xthemePath`. Read that field to get the live path.

`XTheme.kt` defines all active M3 roles in two complementary schemes: `XLightColors` (`lightColorScheme`) and `XDarkColors` (`darkColorScheme`). Before writing any Stitch prompt or implementing any UI, read this file. Use the scheme that matches the `defaultTheme` established in Phase 0 Step 0.1 as the design reference; both schemes must stay in sync whenever roles are added or updated.

## Complete M3 Role Catalog

Every color in a design **must** map to one of these roles. All roles are accessed via `MaterialTheme.colorScheme.{role}`.

#### Primary Group — Brand colors, key actions, prominent UI

| M3 Role | Usage |
|---------|-------|
| `primary` | Key interactive elements: FAB, prominent buttons, active states |
| `onPrimary` | Text/icons on `primary` backgrounds |
| `primaryContainer` | Less prominent primary areas: tonal buttons, selected chips |
| `onPrimaryContainer` | Text/icons on `primaryContainer` |
| `inversePrimary` | Primary color in inverse contexts (e.g., snackbar buttons) |

#### Secondary Group — Supporting elements, less prominent actions

| M3 Role | Usage |
|---------|-------|
| `secondary` | Filter chips, toggle buttons, supporting actions |
| `onSecondary` | Text/icons on `secondary` |
| `secondaryContainer` | Tonal fill for secondary elements: chips, input fields |
| `onSecondaryContainer` | Text/icons on `secondaryContainer` |

#### Tertiary Group — Accent, contrast, complementary elements

| M3 Role | Usage |
|---------|-------|
| `tertiary` | Accent color for contrast: badges, highlights |
| `onTertiary` | Text/icons on `tertiary` |
| `tertiaryContainer` | Container fill for tertiary elements |
| `onTertiaryContainer` | Text/icons on `tertiaryContainer` |

#### Surface Group — Backgrounds, cards, sheets, layered surfaces

| M3 Role | Usage |
|---------|-------|
| `background` | Page/screen background (behind all content) |
| `onBackground` | Text/icons on `background` |
| `surface` | Card, sheet, dialog, menu backgrounds |
| `onSurface` | Primary text and icons on surface/background |
| `surfaceVariant` | Alternative surface for visual distinction (e.g., search bars, input fields) |
| `onSurfaceVariant` | Secondary/muted text, helper text, placeholder icons |
| `surfaceTint` | Tint overlay for elevated surfaces |
| `surfaceBright` | Brighter surface for high-emphasis areas |
| `surfaceDim` | Dimmed surface for low-emphasis areas |
| `surfaceContainer` | Default container surface |
| `surfaceContainerHigh` | Higher-emphasis container |
| `surfaceContainerHighest` | Highest-emphasis container (e.g., text input fill) |
| `surfaceContainerLow` | Lower-emphasis container |
| `surfaceContainerLowest` | Lowest-emphasis container |
| `inverseSurface` | Surface in inverse contexts (e.g., snackbar background) |
| `inverseOnSurface` | Text on `inverseSurface` |

#### Error Group — Errors, destructive actions, validation

| M3 Role | Usage |
|---------|-------|
| `error` | Error indicators, destructive action buttons, validation errors |
| `onError` | Text/icons on `error` backgrounds |
| `errorContainer` | Error container fill (e.g., error banner background) |
| `onErrorContainer` | Text on `errorContainer` |

#### Outline Group — Borders, dividers, decorative lines

| M3 Role | Usage |
|---------|-------|
| `outline` | Borders, dividers, input field outlines |
| `outlineVariant` | Subtle/decorative borders, lower-emphasis dividers |

#### Utility

| M3 Role | Usage |
|---------|-------|
| `scrim` | Overlay behind modals/drawers |

## Color Rules (Strict)

1. **Every design color → M3 role**: Every color in the design MUST map to an M3 role from the catalog above. No exceptions except Rule 5.
2. **No silent defaults**: Every M3 role that a design uses **must** be explicitly defined in `lightColorScheme` in `XTheme.kt`. Do NOT rely on M3 default values.
3. **Grow the theme after approval**: Run a Color Audit (Phase 1 Step 1.16). Every missing M3 role must be added to **both** `XLightColors` and `XDarkColors` in `XTheme.kt` BEFORE any feature code is written.
4. **Feature code = `MaterialTheme.colorScheme.*` only**: Never use raw `Color()` hex values.
5. **`XTheme.Colors.*` is last resort**: Only for truly non-semantic colors (gradients, decorative accents). Must add a code comment explaining why.
