package thisissadeghi.kickoff.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamItem(
    val id: String,
    @SerialName("name_en") val nameEn: String,
    val flag: String,
    @SerialName("fifa_code") val fifaCode: String,
    val iso2: String,
    val groups: String,
)
