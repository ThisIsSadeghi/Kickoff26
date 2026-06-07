package thisissadeghi.kickoff.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Country model representing geographical information
 * Consolidated from multiple feature modules
 */
@Serializable
data class Country(
    val id: Int,
    val name: String? = null,
    val code: String? = null,
    @SerialName("phone_prefix")
    val phonePrefix: String? = null,
    val image: String? = null,
    @SerialName("has_region")
    val hasRegion: Boolean? = null,
    @SerialName("region_detail")
    val regionDetail: String? = null,
    @SerialName("is_country_of_request")
    val isCountryOfRequest: Boolean? = null,
)
