package thisissadeghi.kickoff.data.model

import kotlinx.serialization.Serializable

/**
 * Region model for geographical regions
 * Consolidates region models from product features
 * Represents sub-country geographical divisions (states, provinces, etc.)
 */
@Serializable
data class Region(
    val id: Int? = null,
    val name: String? = null,
    val code: String? = null,
)
