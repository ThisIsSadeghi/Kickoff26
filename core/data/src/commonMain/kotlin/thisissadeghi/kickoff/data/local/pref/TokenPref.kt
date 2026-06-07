package thisissadeghi.kickoff.data.local.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by Ali Sadeghi
 * on 27,Aug,2023
 */
class TokenPref(
    private val dataStorePreference: DataStore<Preferences>,
) {
    companion object {
        // KEYS
        val ACCESS_TOKEN_KEY = stringPreferencesKey("KEY_ACCESS_TOKEN")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("KEY_REFRESH_TOKEN")

        // DEFAULTS
        const val DEFAULT_TOKEN = ""
    }

    val getAccessToken: Flow<String> =
        dataStorePreference.data.map { preference ->
            preference[ACCESS_TOKEN_KEY] ?: DEFAULT_TOKEN
        }

    suspend fun setAccessToken(token: String) {
        dataStorePreference.edit { preference ->
            preference[ACCESS_TOKEN_KEY] = token
        }
    }

    val getRefreshToken: Flow<String> =
        dataStorePreference.data.map { preference ->
            preference[REFRESH_TOKEN_KEY] ?: DEFAULT_TOKEN
        }

    suspend fun setRefreshToken(token: String) {
        dataStorePreference.edit { preference ->
            preference[REFRESH_TOKEN_KEY] = token
        }
    }

    suspend fun clear() {
        dataStorePreference.edit { preference ->
            preference.clear()
        }
    }
}
