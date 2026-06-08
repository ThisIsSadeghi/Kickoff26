package thisissadeghi.kickoff.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameItem(
    val id: String,
    @SerialName("home_team_id") val homeTeamId: String,
    @SerialName("away_team_id") val awayTeamId: String,
    @SerialName("home_score") val homeScore: String,
    @SerialName("away_score") val awayScore: String,
    val group: String,
    val matchday: String,
    @SerialName("local_date") val localDate: String,
    val finished: String,
    @SerialName("time_elapsed") val timeElapsed: String,
    val type: String,
    @SerialName("home_team_name_en") val homeTeamNameEn: String? = null,
    @SerialName("away_team_name_en") val awayTeamNameEn: String? = null,
)
