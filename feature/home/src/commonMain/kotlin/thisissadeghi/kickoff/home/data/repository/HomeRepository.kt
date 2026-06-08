package thisissadeghi.kickoff.home.data.repository

import thisissadeghi.kickoff.common.Either
import thisissadeghi.kickoff.home.data.model.GroupDto
import thisissadeghi.kickoff.home.data.model.MatchDto

interface HomeRepository {
    suspend fun getMatches(): Either<List<MatchDto>>

    suspend fun getStandings(): Either<List<GroupDto>>
}
