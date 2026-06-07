package thisissadeghi.kickoff.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Category model for product categorization
 * Consolidated from multiple feature modules
 */
@Serializable
data class Category(
    val id: Int,
    val name: String? = null,
    val image: String? = null,
    val sort: Int? = null,
    @SerialName("parent_detail")
    val parentDetail: Category? = null,
    @SerialName("created_time")
    val createdTime: String? = null,
    @SerialName("updated_time")
    val updatedTime: String? = null,
)
