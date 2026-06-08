package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kickoff26.feature.home.generated.resources.Res
import kickoff26.feature.home.generated.resources.home_standings_col_drawn
import kickoff26.feature.home.generated.resources.home_standings_col_lost
import kickoff26.feature.home.generated.resources.home_standings_col_played
import kickoff26.feature.home.generated.resources.home_standings_col_pts
import kickoff26.feature.home.generated.resources.home_standings_col_team
import kickoff26.feature.home.generated.resources.home_standings_col_won
import org.jetbrains.compose.resources.stringResource
import thisissadeghi.kickoff.designsystem.AsyncImage
import thisissadeghi.kickoff.designsystem.XHorizontalDivider
import thisissadeghi.kickoff.designsystem.XText
import thisissadeghi.kickoff.designsystem.XTheme
import thisissadeghi.kickoff.home.data.model.StandingRowDto

@Composable
fun StandingsTable(
    rows: List<StandingRowDto>,
    modifier: Modifier = Modifier,
) {
    val statW = 28.dp
    val headerStyle =
        MaterialTheme.typography.labelSmall.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
        )
    val cellStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            XText(text = "#", style = headerStyle, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(20.dp))
            XText(
                text = stringResource(Res.string.home_standings_col_team),
                style = headerStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            XText(
                text = stringResource(Res.string.home_standings_col_played),
                style = headerStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(statW),
                textAlign = TextAlign.Center,
            )
            XText(
                text = stringResource(Res.string.home_standings_col_won),
                style = headerStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(statW),
                textAlign = TextAlign.Center,
            )
            XText(
                text = stringResource(Res.string.home_standings_col_drawn),
                style = headerStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(statW),
                textAlign = TextAlign.Center,
            )
            XText(
                text = stringResource(Res.string.home_standings_col_lost),
                style = headerStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(statW),
                textAlign = TextAlign.Center,
            )
            XText(
                text = stringResource(Res.string.home_standings_col_pts),
                style = headerStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(statW),
                textAlign = TextAlign.Center,
            )
        }
        XHorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        rows.forEach { row ->
            val textColor = if (row.isLeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            val primaryColor = MaterialTheme.colorScheme.primary
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .then(
                            if (row.isLeader) {
                                Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                    .drawBehind {
                                        drawRect(color = primaryColor, size = Size(4.dp.toPx(), size.height))
                                    }
                            } else {
                                Modifier
                            },
                        ).padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                XText(text = "${row.position}", style = cellStyle, color = textColor, modifier = Modifier.width(20.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        url = row.flagUrl,
                        contentDescription = row.teamCode,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(20.dp),
                    )
                    XText(text = row.teamCode, style = cellStyle, color = textColor)
                }
                XText(
                    text = "${row.matchesPlayed}",
                    style = cellStyle,
                    color = textColor,
                    modifier = Modifier.width(statW),
                    textAlign = TextAlign.Center,
                )
                XText(
                    text = "${row.won}",
                    style = cellStyle,
                    color = textColor,
                    modifier = Modifier.width(statW),
                    textAlign = TextAlign.Center,
                )
                XText(
                    text = "${row.drawn}",
                    style = cellStyle,
                    color = textColor,
                    modifier = Modifier.width(statW),
                    textAlign = TextAlign.Center,
                )
                XText(
                    text = "${row.lost}",
                    style = cellStyle,
                    color = textColor,
                    modifier = Modifier.width(statW),
                    textAlign = TextAlign.Center,
                )
                XText(
                    text = "${row.points}",
                    style = cellStyle.copy(fontWeight = if (row.isLeader) FontWeight.Black else FontWeight.Bold),
                    color = textColor,
                    modifier = Modifier.width(statW),
                    textAlign = TextAlign.Center,
                )
            }
        }
    } // Column
}

@Preview
@Composable
private fun StandingsTablePreview() {
    XTheme {
        StandingsTable(
            rows =
                listOf(
                    StandingRowDto(
                        position = 1,
                        teamCode = "MEX",
                        flagUrl = "",
                        matchesPlayed = 3,
                        won = 2,
                        drawn = 1,
                        lost = 0,
                        points = 7,
                        isLeader = true,
                    ),
                    StandingRowDto(
                        position = 2,
                        teamCode = "POL",
                        flagUrl = "",
                        matchesPlayed = 3,
                        won = 1,
                        drawn = 1,
                        lost = 1,
                        points = 4,
                        isLeader = false,
                    ),
                    StandingRowDto(
                        position = 3,
                        teamCode = "ARG",
                        flagUrl = "",
                        matchesPlayed = 3,
                        won = 1,
                        drawn = 0,
                        lost = 2,
                        points = 3,
                        isLeader = false,
                    ),
                    StandingRowDto(
                        position = 4,
                        teamCode = "SAU",
                        flagUrl = "",
                        matchesPlayed = 3,
                        won = 0,
                        drawn = 0,
                        lost = 3,
                        points = 0,
                        isLeader = false,
                    ),
                ),
        )
    }
}
