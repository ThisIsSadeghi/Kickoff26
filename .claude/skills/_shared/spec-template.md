# {FeatureName} Specification

## Metadata
| Field | Value |
|-------|-------|
| Version | 1.0.0 |
| Status | Active |
| Created | {date} |
| Updated | {date} |

## Purpose
{Extracted from implementation or inferred from functionality}

## Goals
<!-- TODO: Fill in - what should this feature achieve? -->
- {Goal 1}
- {Goal 2}

## Non-Goals
<!-- TODO: Fill in - what is explicitly out of scope? -->
- {Non-goal 1}

## Background & Rationale
<!-- TODO: Fill in - why does this feature exist? -->
{Brief rationale for why this feature exists and key design decisions.}

## Platform Profile & Capabilities
<!-- Rule 14 — carried from the PRD. `network` for plain UI/REST features. The modify skill reads this. -->
| Field | Value |
|-------|-------|
| Platform Profile | network / platform-capability / native-view / mixed |
| Capabilities | {none, or e.g. location, camera} |
| Native view | {No, or which view} |
| Sourcing option | {n/a, or 1 multiplatform lib / 2 expect-actual / 3 iOS-Swift bridge} |
| iOS-Swift bridge | {No / Yes — {Feature}Bridge} |

## Design Decisions
<!-- TODO: Fill in - key architectural choices -->
| Decision | Choice | Alternatives | Rationale |
|----------|--------|--------------|-----------|
| {Decision} | {Choice} | {Alternatives} | {Why} |

## Last Updated
- {date} - Generated from existing implementation

## Requirements

### Requirement: {Core Capability}
The system SHALL {behavior description}.

#### Scenario: {Feature} loads successfully
- GIVEN the user navigates to {Feature} screen
- WHEN the data loads successfully
- THEN the content MUST be displayed
- AND the loading state MUST transition to success

#### Scenario: {Feature} handles errors
- GIVEN the user is on {Feature} screen
- WHEN an error occurs
- THEN an error overlay MUST be displayed
- AND a retry option MUST be available

## Architecture

### Package Structure
```
feature/{featurename}/src/commonMain/kotlin/{pkg_prefix}/{featurename}/
├── data/
│   ├── model/
│   ├── datasource/
│   ├── repository/
│   └── remote/
├── presentation/
│   ├── {Feature}ViewModel.kt
│   ├── {Feature}UiModel.kt
│   ├── ui/
│   └── navigation/
└── di/
    └── {Feature}Modules.kt
```

### Data Flow
```
[API] → RemoteDataSource → Repository → ViewModel → [UI]
         Either<Error,DTO>  Either<DTO>  UiState<DTO> (inside *UiModel)
```
(Repository returns DTOs unchanged; ViewModel stores them in `UiState<DTO>` slots of `*UiModel`. No mapping to a presentation-layer mirror type — Rule 11.)

### Key Classes
| Class | Purpose | Location |
|-------|---------|----------|
| {Feature}RemoteDataSource | API operations interface | data/datasource/ |
| {Feature}RemoteDataSourceImpl | API implementation | data/datasource/ |
| {Feature}Repository | Data coordination interface | data/repository/ |
| {Feature}RepositoryImpl | Thin delegation; returns `Either<DTO>` (Rule 11) | data/repository/ |
| {Feature}ViewModel | State management | presentation/ |
| {Feature}Screen | UI composition | presentation/ui/ |

### Data Models
```kotlin
{Actual data class definitions from code}
```

## Integration Points

| Point | File | Status |
|-------|------|--------|
| Module include | settings.gradle.kts | ✅ |
| Dependency | composeApp/build.gradle.kts | ✅ |
| DI init | initKoin.kt | ✅ |
| Navigation | BaseAppNavHost.kt | ✅ |
| Bottom-bar tab (optional) | App.kt + navigation/TopLevelDestination.kt | ✅ / N/A |

## State Management

### UiModel Structure
```kotlin
{Actual *UiModel definition from code — single container with plain fields + UiState<DTO> slots}
```

### State Transitions
{Inferred from ViewModel implementation}

## Navigation

- **Route:** `{Actual route object}`
- **Entry:** `navController.navigate({Route})`
- **Callbacks:** {From Screen parameters}
