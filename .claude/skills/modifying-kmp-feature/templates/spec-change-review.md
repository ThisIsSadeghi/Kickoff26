# Spec Change Review Template

Use this template when presenting proposed spec changes to the user for approval (Step 6).

---

## Template

```markdown
## Spec Change Review: {featurename}

**Requested modification:** "{user's original request}"
**Current Spec Version:** {X.Y.Z}
**Proposed Spec Version:** {new version}

---

### Proposed Changes

#### Section {N}: {Section Name}

**Current:**
```
{existing content}
```

**Proposed:**
```diff
  {unchanged content}
+ {added content}
- {removed content}
```

#### Section {M}: {Section Name}

{repeat for each affected section}

---

### New Content (if applicable)

#### New Scenario: {scenario name}
- GIVEN {precondition}
- WHEN {action}
- THEN {expected result}

#### New Data Model/Field
```kotlin
{new or modified data class}
```

---

### Summary of Changes

| Section | Change Type | Description |
|---------|-------------|-------------|
| {section} | {Addition/Modification/Removal} | {brief description} |

---

### Rationale

{Brief explanation of why these changes are needed and how they support the user's request}

---

### Proposed Changelog Entry

```markdown
- {YYYY-MM-DD} - {Brief description of change}
```

---

## Review Required

Respond with one of:
- **Approved** - Proceed with implementation
- **Approved with changes** - (specify what to modify)
- **Reject** - Do not proceed (explain concerns)

---

> **Next step —** review the proposed spec changes above and reply with one of the options.
```

---

## Guidelines

### When to Use This Template

- **Always** before implementing any modification
- Even for "small" changes - the spec is the source of truth

### Change Types

| Type | Use When | Version Bump |
|------|----------|--------------|
| Addition | New requirement, scenario, field, flow | Minor (X.Y+1.0) |
| Modification | Changing existing behavior | Minor or Major |
| Removal | Removing functionality | Major (X+1.0.0) |
| Clarification | Making existing content clearer | Patch (X.Y.Z+1) |

### Good Rationale Examples

**Good:**
> "Adding sort capability allows users to find recently added favorites faster,
> addressing the user request. Using local-only storage keeps it simple since
> sort preference doesn't need to sync across devices."

**Bad:**
> "User asked for sorting so we're adding it."

### Handling User Responses

| Response | Next Step |
|----------|-----------|
| "Approved" | Proceed to Step 7 (Implement) |
| "Approved, but change X" | Update draft, re-present if significant |
| "Approved, but change X" (minor) | Note change, proceed to implement |
| "Let me think about it" | Wait for explicit approval |
| "No" / "Reject" | End workflow, ask if user wants different approach |

---

## Example

```markdown
## Spec Change Review: favorites

**Requested modification:** "Add sorting by date to favorites"
**Current Spec Version:** 1.2.0
**Proposed Spec Version:** 1.3.0

---

### Proposed Changes

#### Section 3.1: Functional Requirements

**Current:**
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-1.1 | Display all favorited items | Must |
| FR-1.2 | Show item title and image | Must |
| FR-1.3 | Show empty state when no favorites | Must |

**Proposed:**
```diff
  | ID | Requirement | Priority |
  |----|-------------|----------|
  | FR-1.1 | Display all favorited items | Must |
  | FR-1.2 | Show item title and image | Must |
  | FR-1.3 | Show empty state when no favorites | Must |
+ | FR-1.4 | Sort favorites by date added | Should |
```

#### Section 5.1: UiState Definition

**Proposed:**
```diff
  data class FavoritesUiModel(
      val favorites: ImmutableList<FavoriteItem>,
      val isRefreshing: Boolean = false,
+     val sortOrder: SortOrder = SortOrder.NEWEST_FIRST
  )

+ enum class SortOrder {
+     NEWEST_FIRST,
+     OLDEST_FIRST
+ }
```

---

### New Content

#### New Scenario: Sort favorites by date
- GIVEN the user has multiple favorites with different dates
- WHEN the user taps the sort toggle
- THEN favorites MUST be reordered by date
- AND the sort icon MUST reflect current sort direction

#### New Flow: Change Sort Order
1. User taps sort toggle on Favorites screen
2. Sort order toggles between Newest First / Oldest First
3. List re-orders immediately (no loading state)
4. Sort preference persists locally

---

### Summary of Changes

| Section | Change Type | Description |
|---------|-------------|-------------|
| 3.1 Requirements | Addition | Added FR-1.4 for sort capability |
| 5.1 UiState | Addition | Added SortOrder enum and sortOrder field |
| 6.1 User Flows | Addition | Added "Change Sort Order" flow |
| 7.1 Test Scenarios | Addition | Added sort scenario |

---

### Rationale

Adding sort capability allows users to find recently added favorites faster.
Using local-only storage (not synced) keeps implementation simple and aligns
with typical user expectation that sort is a view preference, not data.

Default to "Newest First" since users typically want to see recently added items.

---

### Proposed Changelog Entry

```markdown
- 2025-01-17 - Added sort by date capability (FR-1.4)
```

---

## Review Required

Respond with one of:
- **Approved** - Proceed with implementation
- **Approved with changes** - (specify what to modify)
- **Reject** - Do not proceed

---

> **Next step —** review the proposed spec changes above and reply with one of the options.
```
