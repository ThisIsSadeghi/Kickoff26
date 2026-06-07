# Kickoff26

Kotlin Multiplatform project scaffolded from [KMPilot](https://github.com/ThisIsSadeghi/KMPilot).

## Quick start

```bash
./gradlew assembleDebug
```

The app opens on a Welcome screen. Add your first feature with Claude Code —
it wires navigation and removes the Welcome screen for you.

## AI-assisted development

This project ships with Claude Code skills for feature scaffolding, testing, and review.

```bash
claude
> /creating-kmp-feature ...
```

Available skills are in `.claude/skills/`.

## Project structure

- `composeApp/` — shared Compose Multiplatform UI
- `androidApp/` — Android entry point
- `iosApp/` — iOS entry point (Xcode project)
- `core/` — `common`, `data`, `designsystem` modules
- `feature/` — feature modules (one per business domain)

See `CLAUDE.md` for architecture conventions.
