package thisissadeghi.kickoff.data.repository.user

import thisissadeghi.kickoff.common.Either
import thisissadeghi.kickoff.data.model.Balance

/**
 * Implementation of UserRepository for managing user data
 */
class UserRepositoryImpl : UserRepository {
    override suspend fun getBalance(): Either<Balance> {
        // TODO: Implement balance fetching from remote data source when needed
        return Either.Failure(thisissadeghi.kickoff.common.ErrorModel.Message("Balance API not implemented yet"))
    }
}
