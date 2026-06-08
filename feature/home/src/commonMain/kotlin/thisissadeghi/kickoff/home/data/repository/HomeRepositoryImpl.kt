package thisissadeghi.kickoff.home.data.repository

import thisissadeghi.kickoff.common.Either
import thisissadeghi.kickoff.home.data.datasource.HomeRemoteDataSource
import thisissadeghi.kickoff.home.data.model.GroupDto
import thisissadeghi.kickoff.home.data.model.GroupTeamEntry
import thisissadeghi.kickoff.home.data.model.MatchDto
import thisissadeghi.kickoff.home.data.model.StandingRowDto
import thisissadeghi.kickoff.home.data.model.TeamDto

class HomeRepositoryImpl(
    private val dataSource: HomeRemoteDataSource,
) : HomeRepository {
    override suspend fun getMatches(): Either<List<MatchDto>> {
        val teamsResult = dataSource.getTeams()
        if (teamsResult is Either.Failure) return Either.Failure(teamsResult.error)
        val teamsMap = (teamsResult as Either.Success).data.associateBy { it.id }

        return when (val result = dataSource.getGames()) {
            is Either.Failure -> Either.Failure(result.error)
            is Either.Success ->
                Either.Success(
                    result.data.map { game ->
                        val home = teamsMap[game.homeTeamId]
                        val away = teamsMap[game.awayTeamId]
                        MatchDto(
                            group = game.group,
                            homeTeam =
                                TeamDto(
                                    code = home?.fifaCode ?: game.homeTeamNameEn?.take(3)?.uppercase() ?: "",
                                    flagUrl = home?.flag ?: "",
                                ),
                            awayTeam =
                                TeamDto(
                                    code = away?.fifaCode ?: game.awayTeamNameEn?.take(3)?.uppercase() ?: "",
                                    flagUrl = away?.flag ?: "",
                                ),
                            score =
                                if (game.timeElapsed == "notstarted") {
                                    null
                                } else {
                                    "${game.homeScore} - ${game.awayScore}"
                                },
                            kickoffTime = game.localDate.takeLast(5),
                            isLive = game.timeElapsed == "playing",
                        )
                    },
                )
        }
    }

    override suspend fun getStandings(): Either<List<GroupDto>> {
        val teamsResult = dataSource.getTeams()
        if (teamsResult is Either.Failure) return Either.Failure(teamsResult.error)
        val teamsMap = (teamsResult as Either.Success).data.associateBy { it.id }

        return when (val result = dataSource.getGroups()) {
            is Either.Failure -> Either.Failure(result.error)
            is Either.Success ->
                Either.Success(
                    result.data.sortedBy { it.name }.map { group ->
                        val sorted =
                            group.teams.sortedWith(
                                compareByDescending<GroupTeamEntry> { it.points.toIntOrNull() ?: 0 }
                                    .thenByDescending { it.goalDifference.toIntOrNull() ?: 0 },
                            )
                        GroupDto(
                            id = group.id,
                            label = "Group ${group.name}",
                            standings =
                                sorted.mapIndexed { pos, entry ->
                                    val team = teamsMap[entry.teamId]
                                    StandingRowDto(
                                        position = pos + 1,
                                        teamCode = team?.fifaCode ?: entry.teamId,
                                        flagUrl = team?.flag ?: "",
                                        matchesPlayed = entry.matchesPlayed.toIntOrNull() ?: 0,
                                        won = entry.won.toIntOrNull() ?: 0,
                                        drawn = entry.drawn.toIntOrNull() ?: 0,
                                        lost = entry.lost.toIntOrNull() ?: 0,
                                        points = entry.points.toIntOrNull() ?: 0,
                                        isLeader = pos == 0,
                                    )
                                },
                        )
                    },
                )
        }
    }
}
