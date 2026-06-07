# Phase 2: Prompt Analysis & PRD Generation

**Purpose**: Analyze user's request and generate an adaptive PRD (Product Requirements Document).

**Prerequisites**: Phase 0 (Context Discovery) and Phase 1 (Design Artifact Detection) completed.

---

## Checklist

```
PRD Generation Progress:
- [ ] Step 2.1: Analyze the prompt
- [ ] Step 2.1b: Classify Platform Profile (network / platform-capability / native-view / mixed)
- [ ] Step 2.2: Determine complexity (Simple/Medium/Complex)
- [ ] Step 2.3: Generate PRD using appropriate template
- [ ] Step 2.4: Save PRD and request confirmation
```

---

## Step 2.1: Analyze the Prompt

Extract the following from the user's prompt:

### 1. Feature Name
- Identify the core feature name (e.g., "settings", "productcatalog", "chat")
- Convert to **lowercase**, no spaces/hyphens/underscores
- This becomes `{featurename}` for packages

### 2. Feature Scope
- Core functionality
- User interactions
- Data operations

### 3. Data Requirements
- Does it need API integration? (Ktor Resources + DataSource + API models)
- Does it use local data only? (Local models + mocked data)
- Does it need a **device capability or native view**? (GPS, camera, BLE, biometrics, map, WebView → Rule 14, see Step 2.1b)
- What are the data entities? (User, Product, Message, etc.)

### 4. UI Requirements
- How many screens?
- What components are needed?
- Navigation flows?
- Top-level bottom-bar tab, or a pushed screen? **If a Stitch design blueprint exists and its Component Tree contains a multi-tab bottom navigation bar (≥2 persistent tabs), the answer is top-level tab — do NOT default to pushed screen.** For all other cases, default is pushed screen. If a tab: capture label, icon, order; record in Navigation section as "top-level tab".
- Form inputs or read-only displays?

### 5. Dependencies
- Which core modules? (common, data, designsystem)
- External libraries? (DataStore, etc.)

---

## Step 2.1b: Classify Platform Profile (Rule 14)

Before choosing a template, tag **how the feature gets its data / draws its UI**. This routes the right architecture docs and Phase 4 agents — and is the tripwire that stops a map/camera/sensor feature from silently falling into the REST path.

| Tag | Meaning | Telltale prompt words |
|-----|---------|------------------------|
| `network` | data over HTTP only (the default) | "list", "fetch from API", "submit form", "profile" |
| `platform-capability` | uses a device/native API, no native view on screen | "location", "GPS", "biometrics", "scan", "bluetooth", "sensor", "contacts" |
| `native-view` | embeds a native view Compose can't draw | "map", "camera preview", "QR scanner view", "WebView" |
| `mixed` | both REST **and** a capability/native view | "map of nearby <API results>", "scan then upload" |

**Procedure:**
1. Derive the tag from the prompt.
2. **If unambiguous → proceed** (do not ask). Record the tag.
3. **If ambiguous** (e.g. "show stores near me" — map view, or just a list?) → ask **once** with `AskUserQuestion`, options = the candidate tags.
4. For `platform-capability` / `native-view` / `mixed`: load [architecture/platform.md](../architecture/platform.md) and pick a **sourcing option** (1 multiplatform lib / 2 expect-actual / 3 iOS-Swift bridge) per its decision tree — gate that choice with `AskUserQuestion` when more than one option is viable.
5. Write the tag + capabilities + chosen sourcing option into the PRD's **Platform Profile & Capabilities** section (both templates have it).

A `network` tag changes nothing downstream — the existing 3-agent flow runs unchanged.

---

## Step 2.2: Determine Complexity

| Complexity | Criteria | Task Count |
|------------|----------|------------|
| **Simple** | UI-only, no API, < 3 screens | 3-5 tasks |
| **Medium** | CRUD with API, single-entity, basic business logic | 6-10 tasks |
| **Complex** | Multiple screens, complex logic, multiple entities | 10-15 tasks |

### Template Selection

```
If Simple:
  → Use [templates/prd-simple.md](../templates/prd-simple.md)
  → Skip architecture loading (lightweight)

If Medium/Complex:
  → Use [templates/prd-complex.md](../templates/prd-complex.md)
  → Reference architecture principles
  → Scan :core:designsystem for reusable X-components
```

---

## Step 2.3: Generate PRD

Load the appropriate template and fill in:

- Feature name and description
- Requirements (from prompt analysis)
- Data architecture (if API needed)
- UI design (screens, components)
- Implementation plan (complexity, task count, groups)
- Acceptance criteria (functional scenarios)
- Integration points summary

### Key Sections

**Acceptance Criteria** must include scenarios in this format:
```markdown
#### Scenario: {Feature} loads successfully
- GIVEN the user navigates to {Feature} screen
- WHEN the data loads successfully
- THEN the content MUST be displayed
- AND the loading state MUST transition to success
```

**Integration Points** must list the 4 required changes:
| File | Change Type | Description |
|------|-------------|-------------|
| settings.gradle.kts | MODIFIED | Add module include |
| composeApp/build.gradle.kts | MODIFIED | Add feature dependency |
| initKoin.kt | MODIFIED | Add DI initialization |
| BaseAppNavHost.kt | MODIFIED | Add navigation wiring |

---

## Step 2.4: Save PRD and Request Confirmation

1. **Create directory**:
   ```bash
   mkdir -p .claude/docs/{featurename}
   ```

2. **Save PRD**:
   ```
   Write to: .claude/docs/{featurename}/prd.md
   ```

3. **Display PRD to user** (Read tool)

4. **Show summary**:
   ```
   PRD generated ({complexity}, {X} estimated tasks, API: {yes/no})
   - Simple: "Used lightweight template (no architecture loading)"
   - Complex: "Used detailed template with architecture context"
   ```

5. **Request confirmation** — emit as the very last line of output, styled to catch the eye:
   ```
   ---

   > **Next step —** review the PRD at `.claude/docs/{featurename}/prd.md` and confirm to proceed with task generation, or request changes.
   ```

6. **Wait for user approval** before proceeding to Phase 3

---

## Token Efficiency

| Complexity | Architecture Tokens |
|------------|---------------------|
| Simple | ~0 (template only) |
| Complex | ~990 (loads architecture files when needed) |

---

## Output

After user confirms PRD:
- PRD saved to `.claude/docs/{featurename}/prd.md`
- User has approved the approach
- Ready to proceed to **Phase 3: Task Generation**
