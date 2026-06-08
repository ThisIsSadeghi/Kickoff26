package thisissadeghi.kickoff.home.data.datasource

import kotlinx.serialization.Serializable
import thisissadeghi.kickoff.common.Either
import thisissadeghi.kickoff.data.remote.network.ktor.ApiClient
import thisissadeghi.kickoff.home.data.model.GameItem
import thisissadeghi.kickoff.home.data.model.GroupItem
import thisissadeghi.kickoff.home.data.model.TeamItem
import thisissadeghi.kickoff.home.data.remote.GamesResource
import thisissadeghi.kickoff.home.data.remote.GroupsResource
import thisissadeghi.kickoff.home.data.remote.TeamsResource

@Serializable private data class GamesEnvelope(
    val games: List<GameItem>,
)

@Serializable private data class TeamsEnvelope(
    val teams: List<TeamItem>,
)

@Serializable private data class GroupsEnvelope(
    val groups: List<GroupItem>,
)

class HomeRemoteDataSourceImpl(
    private val apiClient: ApiClient,
) : HomeRemoteDataSource {
    override suspend fun getGames(): Either<List<GameItem>> =
        when (val r: Either<GamesEnvelope> = apiClient.get(GamesResource())) {
            is Either.Success -> Either.Success(r.data.games)
            is Either.Failure -> Either.Failure(r.error)
        }

    override suspend fun getTeams(): Either<List<TeamItem>> =
        when (val r: Either<TeamsEnvelope> = apiClient.get(TeamsResource())) {
            is Either.Success -> Either.Success(r.data.teams)
            is Either.Failure -> Either.Failure(r.error)
        }

    override suspend fun getGroups(): Either<List<GroupItem>> =
        when (val r: Either<GroupsEnvelope> = apiClient.get(GroupsResource())) {
            is Either.Success -> Either.Success(r.data.groups)
            is Either.Failure -> Either.Failure(r.error)
        }
}
