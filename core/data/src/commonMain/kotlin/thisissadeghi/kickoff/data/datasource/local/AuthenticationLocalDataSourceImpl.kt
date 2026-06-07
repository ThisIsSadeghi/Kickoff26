package thisissadeghi.kickoff.data.datasource.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import thisissadeghi.kickoff.data.token.TokenManager

/**
 * Created by Ali Sadeghi
 * on 28,Aug,2023
 */

class AuthenticationLocalDataSourceImpl(
    private val tokenManager: TokenManager,
) : AuthenticationLocalDataSource {
    override fun getAccessToken(): Flow<String> =
        flow {
            emit(tokenManager.getAccessToken().orEmpty())
        }

    override suspend fun setAccessToken(token: String) {
        tokenManager.saveAccessToken(token)
    }

    override fun isLoggedIn(): Flow<Boolean> =
        flow {
            emit(tokenManager.hasValidTokens())
        }

    override suspend fun clearAuthData() = tokenManager.clearTokens()
}
