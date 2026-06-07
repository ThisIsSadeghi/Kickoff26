package thisissadeghi.kickoff.data.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Created by Ali Sadeghi
 * on 28,Apr,2025
 */

@Serializable
data class ASampleModel(
    val id: Int,
    val name: String,
    val description: String,
    val birthdate: LocalDate,
)
