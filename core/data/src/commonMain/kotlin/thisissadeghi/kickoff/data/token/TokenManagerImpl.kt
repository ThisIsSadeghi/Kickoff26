package thisissadeghi.kickoff.data.token

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implementation of TokenManager using DataStore for persistent storage
 */
class TokenManagerImpl(
    private val dataStore: DataStore<Preferences>,
) : TokenManager {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    }

    override suspend fun getAccessToken(): String? =
        dataStore.data
            .map { preferences ->
                preferences[ACCESS_TOKEN_KEY]
            }.first()

    override suspend fun saveAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
        }
    }

    override suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
        }
    }

    override suspend fun hasValidTokens(): Boolean = getAccessToken()?.isNotEmpty() == true
}
