package thisissadeghi.kickoff.data.voucher

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import thisissadeghi.kickoff.common.Currency
import thisissadeghi.kickoff.common.format
import thisissadeghi.kickoff.common.toMoneyOrNull
import thisissadeghi.kickoff.data.model.VoucherBalance

/**
 * Implementation of VoucherManager using DataStore for persistent storage
 * Stores Money as formatted string and CurrencyDetail as separate fields
 */
class VoucherManagerImpl(
    private val dataStore: DataStore<Preferences>,
) : VoucherManager {
    companion object {
        private val VOUCHER_REMAINING_KEY = stringPreferencesKey("voucher_remaining")
        private val CURRENCY_ID_KEY = intPreferencesKey("currency_id")
        private val CURRENCY_CODE_KEY = stringPreferencesKey("currency_code")
        private val CURRENCY_SYMBOL_KEY = stringPreferencesKey("currency_symbol")
    }

    override suspend fun getVoucherBalance(): VoucherBalance? =
        dataStore.data
            .map { preferences ->
                val remainingStr = preferences[VOUCHER_REMAINING_KEY]
                val currencyId = preferences[CURRENCY_ID_KEY]

                if (remainingStr != null && currencyId != null) {
                    try {
                        val remaining = remainingStr.toMoneyOrNull()
                        if (remaining != null) {
                            val currency =
                                Currency(
                                    id = currencyId,
                                    code = preferences[CURRENCY_CODE_KEY] ?: "",
                                    symbol = preferences[CURRENCY_SYMBOL_KEY] ?: "",
                                )
                            VoucherBalance(remaining = remaining, currency = currency)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            }.first()

    override suspend fun saveVoucherBalance(balance: VoucherBalance) {
        dataStore.edit { preferences ->
            // Store Money as formatted string (e.g., "1000.00")
            preferences[VOUCHER_REMAINING_KEY] = balance.remaining.format(round = false)

            // Store currency fields separately
            balance.currency.id.let { preferences[CURRENCY_ID_KEY] = it }
            balance.currency.code.let { preferences[CURRENCY_CODE_KEY] = it }
            balance.currency.symbol.let { preferences[CURRENCY_SYMBOL_KEY] = it }
        }
    }

    override suspend fun clearVoucherBalance() {
        dataStore.edit { preferences ->
            preferences.remove(VOUCHER_REMAINING_KEY)
            preferences.remove(CURRENCY_ID_KEY)
            preferences.remove(CURRENCY_CODE_KEY)
            preferences.remove(CURRENCY_SYMBOL_KEY)
        }
    }
}
