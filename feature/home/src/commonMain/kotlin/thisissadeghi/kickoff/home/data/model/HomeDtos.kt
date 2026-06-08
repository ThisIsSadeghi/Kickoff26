package thisissadeghi.kickoff.home.data.model

data class MatchDto(
    val group: String,
    val homeTeam: TeamDto,
    val awayTeam: TeamDto,
    val score: String?,
    val kickoffTime: String,
    val isLive: Boolean,
)

data class TeamDto(
    val code: String,
    val flagUrl: String,
)

data class GroupDto(
    val id: String = "",
    val label: String = "",
    val standings: List<StandingRowDto> = emptyList(),
)

data class StandingRowDto(
    val position: Int,
    val teamCode: String,
    val flagUrl: String,
    val matchesPlayed: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val points: Int,
    val isLeader: Boolean,
)
