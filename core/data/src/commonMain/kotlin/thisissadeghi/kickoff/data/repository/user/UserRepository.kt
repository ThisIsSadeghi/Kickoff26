package thisissadeghi.kickoff.data.repository.user

import thisissadeghi.kickoff.common.Either
import thisissadeghi.kickoff.data.model.Balance

/**
 * Repository for user-related data
 */
interface UserRepository {
    // Balance
    suspend fun getBalance(): Either<Balance>
}
