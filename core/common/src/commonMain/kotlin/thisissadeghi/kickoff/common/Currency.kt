package thisissadeghi.kickoff.common

import kotlinx.serialization.Serializable

@Serializable
data class Currency(
    val id: Int,
    val symbol: String,
    val code: String,
) {
    companion object {
        val Undefined: Currency = Currency(-1, "", "")
    }
}
