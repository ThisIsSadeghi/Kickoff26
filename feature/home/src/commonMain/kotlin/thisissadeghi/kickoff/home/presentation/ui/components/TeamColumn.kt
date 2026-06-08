package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import thisissadeghi.kickoff.designsystem.AsyncImage
import thisissadeghi.kickoff.designsystem.XText
import thisissadeghi.kickoff.designsystem.XTheme
import thisissadeghi.kickoff.home.data.model.TeamDto

@Composable
fun TeamColumn(
    team: TeamDto,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                url = team.flagUrl,
                contentDescription = team.code,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp),
            )
        }
        XText(
            text = team.code,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun TeamColumnPreview() {
    XTheme { TeamColumn(team = TeamDto(code = "MEX", flagUrl = "")) }
}
