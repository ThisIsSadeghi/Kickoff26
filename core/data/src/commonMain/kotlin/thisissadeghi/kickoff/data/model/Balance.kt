package thisissadeghi.kickoff.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import thisissadeghi.kickoff.common.Money

@Serializable
data class Balance(
    @Contextual
    val remaining: Money?,
)
