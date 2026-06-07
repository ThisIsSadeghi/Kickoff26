package thisissadeghi.kickoff.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import thisissadeghi.kickoff.common.Money

@Serializable
class TransactionsSum(
    @Contextual
    @SerialName("sum_amount")
    val sum: Money?,
)
