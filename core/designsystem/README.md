# core/designsystem

X-component wrappers around Material3 + the `XTheme` palette/typography/shapes. Features depend on this module for all UI primitives instead of Material3 directly.

## Maintenance pointer

**X-component default constraints are catalogued at `.claude/skills/_shared/X_COMPONENTS_CATALOG.md`.** When you change a default value, internal modifier, or behavior override in any `X*.kt` file (e.g. `defaultMinSize`, default `colors`, `contentPadding`, hardcoded `padding`/`background`), update the catalog so `/verify-ui` audits stay correct. The catalog's theme tables (palette, shapes, semantic colors) auto-sync from `XTheme.kt` on every verify-ui run — those don't need manual updates.
