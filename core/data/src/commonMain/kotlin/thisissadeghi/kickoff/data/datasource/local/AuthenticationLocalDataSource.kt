package thisissadeghi.kickoff.data.datasource.local

import kotlinx.coroutines.flow.Flow

/**
 * Created by Ali Sadeghi
 * on 27,Aug,2023
 */
interface AuthenticationLocalDataSource {
    fun getAccessToken(): Flow<String>

    suspend fun setAccessToken(token: String)

    suspend fun clearAuthData()

    fun isLoggedIn(): Flow<Boolean>
}
