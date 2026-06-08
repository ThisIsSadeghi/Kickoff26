package thisissadeghi.kickoff.home.data.datasource

import thisissadeghi.kickoff.common.Either
import thisissadeghi.kickoff.home.data.model.GameItem
import thisissadeghi.kickoff.home.data.model.GroupItem
import thisissadeghi.kickoff.home.data.model.TeamItem

interface HomeRemoteDataSource {
    suspend fun getGames(): Either<List<GameItem>>

    suspend fun getTeams(): Either<List<TeamItem>>

    suspend fun getGroups(): Either<List<GroupItem>>
}
