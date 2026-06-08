package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kickoff26.feature.home.generated.resources.Res
import kickoff26.feature.home.generated.resources.home_upcoming_matches
import kickoff26.feature.home.generated.resources.home_view_all
import org.jetbrains.compose.resources.stringResource
import thisissadeghi.kickoff.designsystem.XTheme
import thisissadeghi.kickoff.home.data.model.MatchDto
import thisissadeghi.kickoff.home.data.model.TeamDto

@Composable
fun UpcomingMatchesSection(
    matches: List<MatchDto>,
    onViewAllMatches: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader(
            title = stringResource(Res.string.home_upcoming_matches),
            actionLabel = stringResource(Res.string.home_view_all),
            onAction = onViewAllMatches,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 0.dp),
        ) {
            items(matches) { match ->
                MatchCard(match = match)
            }
        }
    }
}

@Preview
@Composable
private fun UpcomingMatchesSectionPreview() {
    val team = TeamDto(code = "MEX", flagUrl = "")
    XTheme {
        UpcomingMatchesSection(
            matches =
                listOf(
                    MatchDto(
                        group = "A",
                        homeTeam = team,
                        awayTeam = TeamDto("POL", ""),
                        score = null,
                        kickoffTime = "13:00",
                        isLive = false,
                    ),
                    MatchDto(
                        group = "B",
                        homeTeam = TeamDto("USA", ""),
                        awayTeam = TeamDto("ENG", ""),
                        score = "1 - 0",
                        kickoffTime = "",
                        isLive = true,
                    ),
                ),
            onViewAllMatches = {},
        )
    }
}
