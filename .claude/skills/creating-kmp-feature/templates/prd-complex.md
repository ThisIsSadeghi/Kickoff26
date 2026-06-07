# PRD Template: Medium/Complex Feature

Use for: Features with API integration, multiple screens, business logic

---

```markdown
# PRD: {Feature Name}

## Overview
{2-3 sentence description of what the feature does and its purpose}

## Goals
- {Goal 1: What this feature should achieve}
- {Goal 2: Measurable outcome if possible}
- {Goal 3: User benefit}

## Non-Goals
{Explicitly state what this feature will NOT do - prevents scope creep}

- {Non-goal 1: Feature or capability explicitly out of scope}
- {Non-goal 2: Future consideration, not this implementation}
- {Non-goal 3: Related feature handled elsewhere}

## Background & Rationale
{Why is this feature needed? What problem does it solve?}

{2-3 sentences explaining the business or user need driving this feature.
Reference any user feedback, analytics, or strategic goals if applicable.}

## Platform Profile & Capabilities
<!-- Rule 14 — set in Phase 2 Step 2.1b. Drives which Phase 4 agents run. -->
| Field | Value |
|-------|-------|
| Platform Profile | network / platform-capability / native-view / mixed |
| Capabilities | {none, or e.g. location (GPS), camera, biometrics, BLE} |
| Native view | {No, or e.g. map (MapLibre) / camera preview / WebView} |
| Sourcing option | {n/a, or 1 multiplatform lib / 2 expect-actual / 3 iOS-Swift bridge} — see architecture/platform.md decision tree |
| iOS-Swift bridge needed | {No / Yes — run /bridging-swift-kotlin for {Feature}Bridge} |

## Design Decisions
{Key architectural or UX decisions and why they were made}

| Decision | Choice | Alternatives Considered | Rationale |
|----------|--------|------------------------|-----------|
| {Decision 1} | {What we chose} | {Other options} | {Why this choice} |
| {Decision 2} | {What we chose} | {Other options} | {Why this choice} |

## Feature Scope
{Detailed explanation of functionality}

- Core functionality: {description}
- User interactions: {description}
- Data operations: {description}

## Requirements Analysis

### User Requirements
- {Requirement 1}
- {Requirement 2}
- {Requirement 3}

### Technical Requirements
- API integration with {endpoint}
- State management for {entities}
- Navigation to/from {screens}

## Data Architecture

### Data Models

#### {Entity}Response
```kotlin
@Serializable
data class {Entity}Response(
    val id: Int,
    val field1: String,
    val field2: Type,
)
```

### API Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/v1/{entity}/{id}` | Fetch single entity |
| GET | `/api/v1/{entity}` | Fetch list |
| POST | `/api/v1/{entity}` | Create entity |

### Data Flow
```
API → Ktor Resources → DataSource → Repository → ViewModel → UI
         Either<Error,T>    Either<T>    UiState<T>
```

## UI Design

### Screens

| Screen | Purpose | Components |
|--------|---------|------------|
| {Feature}Screen | Main screen | {list components} |
| {Secondary}Screen | Detail/form | {list components} |

### Navigation
- Entry: `navController.navigate({Feature}Route)`
- Exit: `onBackClick` callback
- Internal: {describe any internal navigation}
- Top-level tab? {No | Yes — label: "{Tab Label}", icon: {icon_name}, order: {N}} — if Yes, this feature is a bottom-bar destination (Integration Point 5); if No, it is a pushed screen

## Technical Architecture

### Module
- Path: `feature/{featurename}/`
- Namespace: `{PKG_PREFIX}.{featurename}`

### Layers
| Layer | Components |
|-------|------------|
| Data | Models (DTOs), Ktor Resources, DataSource, Repository (returns `Either<DTO>`) |
| Presentation | ViewModel, single `*UiModel` (plain fields + `UiState<DTO>` slots), Screens |
| DI | `{featurename}Module` (top-level Koin `val`) |
| Navigation | {Feature}Route, NavGraphBuilder extension |

### Dependencies
- `:core:common`
- `:core:designsystem`
- `:core:data` (for ApiClient)

## Implementation Plan

| Aspect | Value |
|--------|-------|
| Complexity | Medium / Complex |
| Estimated Tasks | {6-10 / 10-15} |
| Groups | Data ({N}) / UI ({M}) / Integration ({K}) |

### Task Groups

**Group 1: Data Layer** (data-layer-agent) — *network / mixed only*
- Module structure + build.gradle.kts
- Data models (@Serializable)
- Ktor Resources (type-safe endpoints)
- RemoteDataSource (interface + impl)
- Repository (interface + impl)

**Group 1b: Platform Layer** (platform-agent) — *Rule 14, tag ≠ `network` only*
- Capability DataSource interface (commonMain) → `Either<DTO>`
- Per-platform actuals (android / ios / **desktop** fallback)
- `expect/actual val platformModule` (DI)
- *(If `data-layer-agent` is absent, this group also owns module structure + build.gradle.kts)*

**Group 2: UI Layer** (ui-layer-agent)
- Single `{Feature}UiModel` (plain UI fields + `UiState<DTO>` slots) — Rule 11
- ViewModel with state management (public flow: `uiModel`)
- Screen composables (with ScreenRoot pattern — `uiModel` param only)
- Component composables
- Navigation (routes + extension)

**Group 3: Integration** (integration-agent)
- DI module (`val {featurename}Module`)
- 4 integration points
- Final build validation
- spec.md generation

## Acceptance Criteria

### Test Scenarios

| Scenario | Given | When | Then |
|----------|-------|------|------|
| Load success | User navigates to screen | API returns data | Data displayed, state = Success |
| Load empty | User navigates to screen | API returns empty list | Empty state displayed |
| Load error | User navigates to screen | Network error occurs | Error overlay with retry button |
| Retry success | Error state displayed | User taps retry | Data loads successfully |
| Navigation back | User on feature screen | User taps back | Navigates to previous screen |
| {Additional scenario} | {precondition} | {action} | {expected result} |

### Functional Scenarios (Detailed)

#### Scenario: {Feature} data loads successfully
- GIVEN the user navigates to {Feature} screen
- WHEN the API call completes successfully
- THEN the data MUST be displayed in the UI
- AND the loading state MUST transition to success

#### Scenario: {Feature} handles network errors
- GIVEN the user is on {Feature} screen
- WHEN a network error occurs
- THEN an error overlay MUST be displayed
- AND a retry button MUST be visible and functional

#### Scenario: {Feature} navigation works correctly
- GIVEN the user is on {Feature} screen
- WHEN the user initiates back navigation
- THEN the app MUST navigate to the previous screen

### Technical Verification
- [ ] Build passes: `./gradlew assembleDebug`
- [ ] All 4 UI states handled (Uninitialized, Loading, Success, Failed)
- [ ] Navigation works correctly
- [ ] X-components used (no Material3)
- [ ] `_uiModel.setState {}` used (not direct assignment)
- [ ] `Either<DTO>` returned from Repository — no UI mapping in data layer (Rule 11)
- [ ] Single `{Feature}UiModel.kt` (no `{Feature}UiState.kt`); `UiState<>` slots wrap DTOs
- [ ] Interface + Impl pairs created
- [ ] Code formatted: `./gradlew ktlintFormat`

## Integration Points

| File | Change Type | Description |
|------|-------------|-------------|
| settings.gradle.kts | MODIFIED | Add `include(":feature:{featurename}")` |
| composeApp/build.gradle.kts | MODIFIED | Add feature dependency |
| initKoin.kt | MODIFIED | Add `{featurename}Module` to `modules(...)` |
| BaseAppNavHost.kt | MODIFIED | Add `{featurename}(...)` navigation |
```

---

## Usage Notes

- Requires full data layer (Repository, DataSource, Ktor Resources)
- ViewModel manages async state with UiState<T>
- Must handle all 4 UI states
- Typical task count: 6-15 depending on complexity
- Reference architecture files for detailed patterns
