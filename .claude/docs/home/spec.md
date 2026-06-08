# Home Feature Spec

## Overview
World Cup 2026 home screen. Countdown to opening match + upcoming matches list + group standings.

## API
Base URL: `https://worldcup26.ir/`
- `GET /get/games` → `{"games": [GameItem]}`
- `GET /get/teams` → `{"teams": [TeamItem]}`
- `GET /get/groups` → `{"groups": [GroupItem]}`

## Countdown target
`2026-06-11T19:00:00Z` (Mexico City opening match: June 11, 2026 13:00 CST)

## Architecture
- Platform profile: `network`
- Data layer: `HomeRemoteDataSource` (3 endpoints) → `HomeRepository` (maps to `MatchDto`/`GroupDto`)
- Envelope classes (`GamesEnvelope`, `TeamsEnvelope`, `GroupsEnvelope`) are `private` inside `HomeRemoteDataSourceImpl` — DataSource interface exposes `List<X>` only
- `GroupTeamEntry` uses meaningful field names (`matchesPlayed`, `won`, `lost`, `drawn`, `points`, `goalsFor`, `goalsAgainst`, `goalDifference`) with `@SerialName` for API keys

## Design Decisions
- Blueprint deviation: `TeamDto.flagUrl: String` (CDN URL) instead of blueprint's `flagEmoji` — API returns flag image URLs, not emoji
- `CountdownDto` lives in presentation layer (not data layer) — pure UI concern
- Countdown uses `kotlin.time.Clock.System.now()` (kotlinx-datetime 0.8.0 typealias compat)
- Bottom nav bar (Integration Point 5): Home/Matches/Profile tabs; Matches and Profile route to HomeRoute as placeholders
- Layout fix (v1.1.1): LazyColumn `contentPadding.top` reduced 64.dp → 8.dp; the 64dp Stitch fixed-header offset was double-counted against the `XScreen` topBar height.
- Fragment composable fix (v1.1.1): `StandingsTable` wraps all emitted children in a `Column(modifier)`, making it layout-safe regardless of caller context (prevents z-stacking when a `Box` caller is used).
- Overflow fix (v1.1.1): `HeroCountdownCard` countdown Row uses `Arrangement.SpaceEvenly` instead of `spacedBy(24.dp)` to distribute 4 digit units across available width without overflow.

## Integration
5-point integration including bottom nav bar (Case B shell).

## Last Updated
- 2026-06-08 - Layout bug fixes (v1.1.1): double top-padding (64dp→8dp), standings z-stack merge (StandingsTable now owns Column), countdown digit overflow (SpaceEvenly).
- 2026-06-08 - Audit fix pass (v1.1.0): all 14 critical + 4 minor UI token mismatches resolved, X-Components compliance (XHorizontalDivider, drop CardDefaults import), string hardcodes extracted, motion refactored (livePulseAlpha extracted to HomeMotion, XMotion.LIVE_PULSE/TICK tokens added). Source: designs/home_audit.md.
