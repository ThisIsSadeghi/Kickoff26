package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kickoff26.feature.home.generated.resources.Res
import kickoff26.feature.home.generated.resources.home_vs_label
import org.jetbrains.compose.resources.stringResource
import thisissadeghi.kickoff.designsystem.XText
import thisissadeghi.kickoff.designsystem.XTheme
import thisissadeghi.kickoff.home.data.model.MatchDto
import thisissadeghi.kickoff.home.data.model.TeamDto

@Composable
fun ScoreColumn(
    match: MatchDto,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (match.score != null) {
            XText(
                text = match.score,
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                    ),
                color = if (match.isLive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
            )
        } else {
            XText(
                text = stringResource(Res.string.home_vs_label),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val previewTeam = TeamDto(code = "MEX", flagUrl = "")

@Preview
@Composable
private fun ScoreColumnVsPreview() {
    XTheme {
        ScoreColumn(
            match =
                MatchDto(
                    group = "A",
                    homeTeam = previewTeam,
                    awayTeam = previewTeam,
                    score = null,
                    kickoffTime = "13:00",
                    isLive = false,
                ),
        )
    }
}

@Preview
@Composable
private fun ScoreColumnLivePreview() {
    XTheme {
        ScoreColumn(
            match = MatchDto(group = "A", homeTeam = previewTeam, awayTeam = previewTeam, score = "2 - 1", kickoffTime = "", isLive = true),
        )
    }
}
