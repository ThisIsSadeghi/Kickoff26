package thisissadeghi.kickoff.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupItem(
    @SerialName("_id") val id: String,
    val name: String,
    val teams: List<GroupTeamEntry>,
)

@Serializable
data class GroupTeamEntry(
    @SerialName("team_id") val teamId: String,
    @SerialName("mp") val matchesPlayed: String,
    @SerialName("w") val won: String,
    @SerialName("l") val lost: String,
    @SerialName("d") val drawn: String,
    @SerialName("pts") val points: String,
    @SerialName("gf") val goalsFor: String,
    @SerialName("ga") val goalsAgainst: String,
    @SerialName("gd") val goalDifference: String,
)
