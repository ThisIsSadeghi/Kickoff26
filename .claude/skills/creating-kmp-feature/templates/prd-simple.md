# PRD Template: Simple Feature

Use for: UI-only features, no API, < 3 screens

---

```markdown
# PRD: {Feature Name}

## Overview
{1-2 sentence description of what the feature does}

## Goals
- {Goal 1: What this feature should achieve}
- {Goal 2: User benefit}

## Non-Goals
{Explicitly state what this feature will NOT do}

- {Non-goal 1: Out of scope for this implementation}
- {Non-goal 2: Future consideration}

## Background & Rationale
{Brief explanation of why this feature is needed - 1-2 sentences}

## Platform Profile & Capabilities
<!-- Rule 14 — set in Phase 2 Step 2.1b. For a plain UI/REST feature this is just `network`. -->
| Field | Value |
|-------|-------|
| Platform Profile | network / platform-capability / native-view / mixed |
| Capabilities | {none, or e.g. location, camera, biometrics} |
| Native view | {No, or e.g. map / camera preview} |
| Sourcing option | {n/a, or 1 multiplatform lib / 2 expect-actual / 3 iOS-Swift bridge} |

## Design Decisions
| Decision | Choice | Rationale |
|----------|--------|-----------|
| {Decision 1} | {What we chose} | {Why} |

## Implementation Plan

| Aspect | Value |
|--------|-------|
| Complexity | Simple |
| Estimated Tasks | 3-5 |
| Layers | UI + Integration (no data layer) |
| API Required | No |

## UI Requirements

### Screens
1. **{ScreenName}Screen** - {purpose}

> Top-level tab? {No (pushed screen) | Yes — label / icon / order}. If Yes, see Integration Point 5 (bottom-bar tab).

### Components
- {Component1} - {purpose}
- {Component2} - {purpose}

### State
- Local state managed in ViewModel
- No API calls required

## Acceptance Criteria

### Test Scenarios

| Scenario | Given | When | Then |
|----------|-------|------|------|
| Display correctly | User navigates to screen | Screen initializes | UI displayed correctly |
| User interaction | User on screen | Interacts with {element} | Expected action occurs |
| Navigation back | User on screen | Taps back | Navigates to previous |

### Functional Scenarios (Detailed)

#### Scenario: {Feature} displays correctly
- GIVEN the user navigates to {Feature} screen
- WHEN the screen initializes
- THEN the UI MUST be displayed correctly
- AND all interactive elements MUST be functional

#### Scenario: {Feature} handles user interactions
- GIVEN the user is on {Feature} screen
- WHEN the user interacts with {element}
- THEN the expected action MUST occur
- AND the UI MUST update appropriately

### Technical Verification
- [ ] Build passes: `./gradlew assembleDebug`
- [ ] Navigation works correctly
- [ ] X-components used (no Material3)
- [ ] Code formatted: `./gradlew ktlintFormat`

## Integration Points

| File | Change Type | Description |
|------|-------------|-------------|
| settings.gradle.kts | MODIFIED | Add module include |
| composeApp/build.gradle.kts | MODIFIED | Add feature dependency |
| initKoin.kt | MODIFIED | Add DI initialization |
| BaseAppNavHost.kt | MODIFIED | Add navigation wiring |

## Dependencies
- `:core:common`
- `:core:designsystem`
```

---

## Usage Notes

- No data layer needed (no Repository, DataSource, Ktor Resources)
- ViewModel manages local state only
- Focus on UI implementation and navigation
- Minimal task count (3-5)
