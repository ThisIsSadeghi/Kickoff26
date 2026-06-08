package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import thisissadeghi.kickoff.designsystem.XCard
import thisissadeghi.kickoff.designsystem.XTheme
import thisissadeghi.kickoff.home.data.model.MatchDto
import thisissadeghi.kickoff.home.data.model.TeamDto

@Composable
fun MatchCard(
    match: MatchDto,
    modifier: Modifier = Modifier,
) {
    XCard(
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border =
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GroupBadge(group = match.group, isHighlighted = match.isLive)
                if (match.isLive) LiveBadge() else KickoffBadge(kickoffTime = match.kickoffTime)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamColumn(team = match.homeTeam)
                ScoreColumn(match = match)
                TeamColumn(team = match.awayTeam)
            }
        }
    }
}

@Preview
@Composable
private fun MatchCardPreview() {
    XTheme {
        MatchCard(
            match =
                MatchDto(
                    group = "A",
                    homeTeam = TeamDto(code = "MEX", flagUrl = ""),
                    awayTeam = TeamDto(code = "POL", flagUrl = ""),
                    score = null,
                    kickoffTime = "13:00",
                    isLive = false,
                ),
        )
    }
}

@Preview
@Composable
private fun MatchCardLivePreview() {
    XTheme {
        MatchCard(
            match =
                MatchDto(
                    group = "B",
                    homeTeam = TeamDto(code = "USA", flagUrl = ""),
                    awayTeam = TeamDto(code = "ENG", flagUrl = ""),
                    score = "1 - 0",
                    kickoffTime = "",
                    isLive = true,
                ),
        )
    }
}
