package thisissadeghi.kickoff.data.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

@Serializable
data class Descriptions(
    val content: ImmutableList<DescriptionItem>? = null,
)

@Serializable
data class DescriptionItem(
    val title: String?,
    val description: String?,
)
