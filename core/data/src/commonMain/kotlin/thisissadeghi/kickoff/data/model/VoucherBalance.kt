package thisissadeghi.kickoff.data.model

import thisissadeghi.kickoff.common.Currency
import thisissadeghi.kickoff.common.Money

/**
 * Domain model for storing voucher balance information
 */
data class VoucherBalance(
    val remaining: Money,
    val currency: Currency,
)
