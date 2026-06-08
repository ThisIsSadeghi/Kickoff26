# Home Screen — Compose Implementation Blueprint

**Stitch Project**: 18114147449396774731
**Screen ID (success)**: 1c657134db2746fa95456c95fd220783
**Feature**: `home`
**Package prefix**: `thisissadeghi.kickoff`
**Generated**: 2026-06-07

---

## Pre-Implementation Contract

### Missing M3 Roles (add to BOTH XLightColors and XDarkColors in XTheme.kt BEFORE writing feature code)

| Role | XDarkColors hex | XLightColors hex | Usage |
|------|-----------------|------------------|-------|
| `tertiary` | `#4FC3F7` | `#0284C7` | LIVE badge text + animated dot color |

```kotlin
// XDarkColors — add:
tertiary = Color(0xFF4FC3F7),
onTertiary = Color(0xFF003547),
tertiaryContainer = Color(0xFF004D63),
onTertiaryContainer = Color(0xFFB8EAFF),

// XLightColors — add:
tertiary = Color(0xFF0284C7),
onTertiary = Color(0xFFFFFFFF),
tertiaryContainer = Color(0xFFBAE6FD),
onTertiaryContainer = Color(0xFF002232),
```

### Typography Updates Required
Font family: **Outfit** — matches current `XFontFamily()`. No font swap needed.

### Component Overrides
| Component | Property | Design value | Compose |
|-----------|----------|--------------|---------|
| Hero card corner radius | 20dp | `MaterialTheme.shapes.large` | ✓ |
| Match card corner radius | 12dp | `MaterialTheme.shapes.medium` | ✓ |
| Nav bar corner radius | 12dp top-only | `RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)` | — |

### Icons Required

**Feature-domain icons** (downloaded to `feature/home/src/commonMain/composeResources/drawable/`):

| `name` | `drawable_name` | `res_reference` | Usage |
|--------|-----------------|-----------------|-------|
| `calendar_today` | `calendar_today` | `Res.drawable.calendar_today` | countdown date row |
| `sports_soccer` | `sports_soccer` | `Res.drawable.sports_soccer` | shared failed screen error icon; Matches nav tab (app-shell) |

**App-shell nav tab icons** (Integration Point 5 — place in `composeApp/composeResources/drawable/`, NOT in feature):

| `name` | `drawable_name` | Tab | Filled |
|--------|-----------------|-----|--------|
| `home` | `home_fill` | Home (active) | yes — `FILL 1` |
| `sports_soccer` | `sports_soccer` | Matches (inactive) | no |
| `person` | `person` | Profile (inactive) | no |

> `sports_soccer` is shared between the app-shell Matches tab and the shared failed screen. The implementation skill materializes it once at `feature/home/.../drawable/sports_soccer.xml` (domain scope for now); when a second feature opts into the shared failed state the script will promote it to `core/designsystem/.../drawable/sports_soccer.xml` automatically.

### Images Required

| Asset | `drawable_name` | `res_reference` | Delivery |
|-------|-----------------|-----------------|----------|
| Stadium hero background | `success_hero` | `Res.drawable.success_hero` | bundled (user-confirmed) |

Download at implementation time via `download_assets.py` full run. Extension determined from Content-Type (JPEG/WebP expected).

---

## Design Tokens

### Color Roles Used
| Token | Hex | Compose reference |
|-------|-----|-------------------|
| background | #0A1209 | `MaterialTheme.colorScheme.background` |
| surface | #141E12 | `MaterialTheme.colorScheme.surface` |
| surfaceVariant | #1E3020 | `MaterialTheme.colorScheme.surfaceVariant` |
| primary | #86E8AB | `MaterialTheme.colorScheme.primary` |
| onSurface | #E2EEDF | `MaterialTheme.colorScheme.onSurface` |
| onSurfaceVariant | #A5C0A0 | `MaterialTheme.colorScheme.onSurfaceVariant` |
| outlineVariant | #2E4A2C | `MaterialTheme.colorScheme.outlineVariant` |
| tertiary | #4FC3F7 | `MaterialTheme.colorScheme.tertiary` *(add to XTheme.kt first)* |
| Gold (custom) | #FFD700 | `XTheme.Colors.Gold` |

### Typography Scale
| Node | M3 Role | Override |
|------|---------|----------|
| "KICKOFF 26" wordmark | `titleLarge` | `.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.05).em)` |
| "TOURNAMENT STARTS IN" | `labelSmall` | `.copy(fontSize = 10.sp, letterSpacing = 0.2.em)` |
| Countdown digits | `displaySmall` | `.copy(fontSize = 36.sp, fontWeight = FontWeight.Black)` |
| Date sublabel | `bodySmall` | none |
| Section headers | `titleMedium` | `.copy(fontWeight = FontWeight.Bold)` |
| "VIEW ALL" | `labelSmall` | `.copy(fontWeight = FontWeight.SemiBold)` |
| Group badge | `labelSmall` | `.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)` |
| "LIVE" badge | `labelSmall` | `.copy(fontSize = 10.sp, fontWeight = FontWeight.Black)` |
| Score ("2 - 1") | `displaySmall` | `.copy(fontSize = 30.sp, fontWeight = FontWeight.Black)` |
| "VS" | `titleLarge` | `.copy(fontWeight = FontWeight.Black)` |
| Team code | `labelSmall` | `.copy(fontWeight = FontWeight.Bold)` |
| Kickoff time | `labelSmall` | `.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)` |
| Standings header | `labelSmall` | `.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.05.em)` |
| Nav tab label | `labelSmall` | `.copy(fontSize = 10.sp)` |

### Spacing Grid
| Token | Value | Compose |
|-------|-------|---------|
| Screen horizontal margin | 16dp | `Modifier.padding(horizontal = 16.dp)` |
| Section vertical gap | 24dp | `Arrangement.spacedBy(24.dp)` |
| Card internal padding | 16dp | `Modifier.padding(16.dp)` |
| Card horizontal gap | 16dp | `Arrangement.spacedBy(16.dp)` |
| Countdown unit gap | 24dp | `Arrangement.spacedBy(24.dp)` |
| Hero height | 200dp | `Modifier.height(200.dp)` |
| Top app bar height | 64dp | `Modifier.height(64.dp)` |
| Nav bar height | 80dp | `Modifier.height(80.dp)` |

---

## Component Tree

### Top-Level Structure

```
HomeScreenRoot
└── XScreen(topBar = { HomeTopBar() })
    └── HomeContent(uiModel, onGroupSelected, onViewAllMatches)  ← LazyColumn
```

> **Tab nav bar**: The bottom navigation (Home / Matches / Profile) is **app-shell chrome — Integration Point 5**. Do NOT put it in `XScreen(bottomBar = {...})`. Wire via `TopLevelDestination` enum + `XNavigationBar` in `App.kt`. Tab icons go in `composeApp/composeResources/drawable/`; tab labels in `composeApp/composeResources/values/strings.xml`.

---

### HomeTopBar
```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f))
        .padding(horizontal = 16.dp),
    contentAlignment = Alignment.CenterStart
) {
    XText(
        text = stringResource(Res.string.home_app_name),
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
        color = MaterialTheme.colorScheme.primary
    )
}
```

---

### HomeContent (LazyColumn)
```kotlin
LazyColumn(
    contentPadding = PaddingValues(top = 64.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp)
) {
    item { HeroCountdownCard(uiModel.countdown, uiModel.eventDateLabel) }
    item { UpcomingMatchesSection(uiModel.matches, onViewAllMatches) }
    item { GroupStandingsSection(uiModel.groups, uiModel.selectedGroup, onGroupSelected) }
}
```

---

### HeroCountdownCard
```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .height(200.dp)
        .clip(MaterialTheme.shapes.large)
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.large)
) {
    Image(
        painter = painterResource(Res.drawable.success_hero),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize().alpha(0.4f)
    )
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))
        )
    )
    Column(
        modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        XText(
            text = stringResource(Res.string.home_tournament_starts_in),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.2.em),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally  // center the row
        ) {
            CountdownUnit(uiModel.countdown.days,    "D")
            CountdownUnit(uiModel.countdown.hours,   "H")
            CountdownUnit(uiModel.countdown.minutes, "M")
            CountdownUnit(uiModel.countdown.seconds, "S")
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.calendar_today),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            XText(
                text = uiModel.eventDateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CountdownUnit(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(targetState = value, /* see Motion section */) { v ->
            XText(
                text = "$v$label",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 36.sp, fontWeight = FontWeight.Black
                ),
                color = XTheme.Colors.Gold
            )
        }
    }
}
```

---

### UpcomingMatchesSection
```kotlin
Column(
    modifier = Modifier.padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    SectionHeader(
        title = stringResource(Res.string.home_upcoming_matches),
        actionLabel = stringResource(Res.string.home_view_all),
        onAction = onViewAllMatches
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(uiModel.matches) { match -> MatchCard(match) }
    }
}

@Composable
private fun MatchCard(match: MatchDto) {
    XCard(
        modifier = Modifier.width(280.dp),
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GroupBadge(match.group, isActive = match.isLive)
                if (match.isLive) LiveBadge() else KickoffBadge(match.kickoffTime)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TeamColumn(match.homeTeam)
                ScoreColumn(match)
                TeamColumn(match.awayTeam)
            }
        }
    }
}
```

---

### LiveBadge
```kotlin
@Composable
private fun LiveBadge() {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val alpha by rememberInfiniteTransition(label = "live_pulse")
            .animateFloat(
                initialValue = 1f, targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse
                ),
                label = "live_dot_alpha"
            )
        Box(Modifier.size(6.dp).background(
            MaterialTheme.colorScheme.tertiary.copy(alpha = alpha), CircleShape
        ))
        XText(
            text = stringResource(Res.string.home_live_label),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}
```

---

### GroupStandingsSection
```kotlin
Column(
    modifier = Modifier.padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    XText(
        text = stringResource(Res.string.home_group_standings),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(uiModel.groups) { group ->
            GroupTab(
                label = group.label,
                selected = group.id == uiModel.selectedGroup.id,
                onClick = { onGroupSelected(group) }
            )
        }
    }
    StandingsTable(rows = uiModel.selectedGroup.standings)
}

@Composable
private fun GroupTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .drawBehind {
                if (selected) drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        XText(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}
```

---

## UiModel

```kotlin
data class HomeUiModel(
    val countdown: CountdownDto = CountdownDto(),
    val eventDateLabel: String = "",
    val matches: List<MatchDto> = emptyList(),
    val groups: List<GroupDto> = emptyList(),
    val selectedGroup: GroupDto = GroupDto(),
    val matchesState: UiState<List<MatchDto>> = UiState.Uninitialized,
    val standingsState: UiState<List<GroupDto>> = UiState.Uninitialized,
)

data class CountdownDto(val days: String = "00", val hours: String = "00", val minutes: String = "00", val seconds: String = "00")
data class MatchDto(val group: String, val homeTeam: TeamDto, val awayTeam: TeamDto, val score: String?, val kickoffTime: String, val isLive: Boolean)
data class TeamDto(val code: String, val flagEmoji: String)
data class GroupDto(val id: String = "", val label: String = "", val standings: List<StandingRowDto> = emptyList())
data class StandingRowDto(val position: Int, val teamCode: String, val flagEmoji: String, val played: Int, val won: Int, val drawn: Int, val lost: Int, val points: Int, val isLeader: Boolean)
```

---

## String Inventory (Rule 12)

| Key | Value (English) |
|-----|-----------------|
| `home_app_name` | KICKOFF 26 |
| `home_tournament_starts_in` | TOURNAMENT STARTS IN |
| `home_upcoming_matches` | Upcoming Matches |
| `home_view_all` | View All |
| `home_group_standings` | Group Standings |
| `home_live_label` | LIVE |
| `home_kickoff_label_template` | %1$s KICKOFF |
| `home_group_label_template` | Group %1$s |
| `home_vs_label` | VS |

---

## Motion

| Element | Family | Compose primitive | Params | Magnitude | Target file |
|---------|--------|-------------------|--------|-----------|-------------|
| LIVE dot pulse | Loading/Attention loop | `rememberInfiniteTransition` + `animateFloat` | 1000ms / FastOutSlowInEasing / Forever / auto-start | alpha 1.0 ↔ 0.3 | `ui/motion/HomeMotion.kt` |
| Countdown digit tick | Value-driven | `AnimatedContent` with `slideInVertically + fadeIn` | 300ms / FastOutLinearInEasing / on value change | slide 8dp up + fade | `ui/motion/HomeMotion.kt` |

**Reduced motion**: gate both via `rememberReducedMotion()`. When reduced — LIVE dot: static alpha 1.0; Countdown: `ContentTransform.None`.
**Dropped**: `active:scale-[0.98]` (card press), `hover:*` (nav), `transition-colors` (header).

---

## Loading State

`UiState.Loading` → `AppLoadingState()` from `thisissadeghi.kickoff.designsystem.app`
Shared screen: `.claude/docs/_shared/designs/loading.png`

## Failed State

`UiState.Failed` → `AppErrorState(title, message, onRetry = viewModel::retry)` from `thisissadeghi.kickoff.designsystem.app`
Shared screen: `.claude/docs/_shared/designs/failed.png`

---

## Post-Implementation Checklist

- [ ] Add `tertiary` (+ `onTertiary`, `tertiaryContainer`, `onTertiaryContainer`) to both `XLightColors` and `XDarkColors` in `XTheme.kt`
- [ ] Run `download_assets.py` (full, no `--manifest-only`) to download `calendar_today.xml` and `sports_soccer.xml` to `feature/home/.../composeResources/drawable/`
- [ ] Download `success_hero` image; place at `feature/home/src/commonMain/composeResources/drawable/success_hero.{ext}`
- [ ] Download app-shell nav tab icons (`home_fill.xml`, `sports_soccer.xml`, `person.xml`) to `composeApp/composeResources/drawable/`
- [ ] Add tab labels (`home_tab`, `matches_tab`, `profile_tab`) to `composeApp/composeResources/values/strings.xml`
- [ ] Wire nav tabs via `TopLevelDestination` enum + `XNavigationBar` in `App.kt` (Integration Point 5)
- [ ] Verify countdown ticks every second on device
- [ ] Verify LIVE pulse respects `Reduce Motion` OS setting
- [ ] Verify group tab selection switches standings table content
- [ ] Run `/verify-ui home` after implementation
