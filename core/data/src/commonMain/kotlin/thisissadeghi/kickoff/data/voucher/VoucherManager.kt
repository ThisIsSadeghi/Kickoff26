package thisissadeghi.kickoff.data.voucher

import thisissadeghi.kickoff.data.model.VoucherBalance

/**
 * Interface for managing voucher balance data
 * Provides methods to store, retrieve, and clear voucher balance
 */
interface VoucherManager {
    /**
     * Get the stored voucher balance
     * @return voucher balance or null if not available
     */
    suspend fun getVoucherBalance(): VoucherBalance?

    /**
     * Save voucher balance
     * @param balance the voucher balance to save
     */
    suspend fun saveVoucherBalance(balance: VoucherBalance)

    /**
     * Clear stored voucher balance
     */
    suspend fun clearVoucherBalance()
}
