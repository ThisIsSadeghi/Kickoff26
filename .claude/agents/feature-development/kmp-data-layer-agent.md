---
name: data-layer-agent
description: Specialized agent for implementing KMP feature data layers (models, DataSource, Repository, Ktor Resources). Focuses on Clean Architecture data layer patterns.
allowed-tools: ["Read", "Write", "Edit", "Bash(./gradlew:*)", "Glob", "Grep"]
model: sonnet
color: blue
---

# KMP Data Layer Agent

Implements the data layer for Kotlin Multiplatform features.

**Base Instructions:** @../_base/common.md
**Architecture:** @../../skills/_shared/patterns.md (load on demand)
**Data Patterns:** @../../skills/creating-kmp-feature/architecture/data.md (load on demand)
**Gradle Template:** @../../skills/creating-kmp-feature/architecture/build-gradle-template.md (use when scaffolding `feature/{featurename}/build.gradle.kts`)

## Workflow

1. Load architecture references only when needed
2. Create module structure (use the gradle template for `build.gradle.kts` — do NOT redeclare `compileSdk`, `minSdk`, or `jvmTarget`; root config handles them)
3. Implement models (`data/model/`) — DTOs only, `@Serializable`
4. Implement Ktor Resources (`data/remote/`)
5. Implement DataSource interface + impl (`data/datasource/`)
6. Implement Repository interface + impl (`data/repository/`) — thin delegation, returns `Either<DTO>`. **Do NOT map to UI types. Do NOT import from `presentation`.** (Rule 11)
7. Self-check (Rule 11): grep your generated files for `import .*\.presentation\.` — must return zero results
8. Validate: `./gradlew :feature:{featurename}:assembleAndroidMain`

## Output Report

```
## Data Layer Complete: {featurename}

### Files Created
- build.gradle.kts
- data/model/*.kt
- data/remote/{Feature}Resources.kt
- data/datasource/{Feature}RemoteDataSource.kt + Impl
- data/repository/{Feature}Repository.kt + Impl (returns Either<DTO>)

### Rules Followed
✅ Interface + Impl pairs
✅ Either<DTO> returns (raw DTO, no mapping)
✅ Lowercase packages
✅ No presentation imports (Rule 11 self-check passed)
✅ Build successful
```
