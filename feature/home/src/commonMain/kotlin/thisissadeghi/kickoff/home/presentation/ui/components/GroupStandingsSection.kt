package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kickoff26.feature.home.generated.resources.Res
import kickoff26.feature.home.generated.resources.home_group_standings
import org.jetbrains.compose.resources.stringResource
import thisissadeghi.kickoff.designsystem.XText
import thisissadeghi.kickoff.designsystem.XTheme
import thisissadeghi.kickoff.home.data.model.GroupDto
import thisissadeghi.kickoff.home.data.model.StandingRowDto

@Composable
fun GroupStandingsSection(
    groups: List<GroupDto>,
    selectedGroup: GroupDto,
    onGroupSelected: (GroupDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        XText(
            text = stringResource(Res.string.home_group_standings),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp),
        ) {
            items(groups) { group ->
                GroupTab(
                    label = group.label,
                    selected = group.id == selectedGroup.id,
                    onClick = { onGroupSelected(group) },
                )
            }
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(24.dp),
                    ),
        ) {
            StandingsTable(
                rows = selectedGroup.standings,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

private val previewStandings =
    listOf(
        StandingRowDto(
            position = 1,
            teamCode = "MEX",
            flagUrl = "",
            matchesPlayed = 2,
            won = 1,
            drawn = 1,
            lost = 0,
            points = 4,
            isLeader = true,
        ),
        StandingRowDto(
            position = 2,
            teamCode = "POL",
            flagUrl = "",
            matchesPlayed = 2,
            won = 1,
            drawn = 0,
            lost = 1,
            points = 3,
            isLeader = false,
        ),
        StandingRowDto(
            position = 3,
            teamCode = "ARG",
            flagUrl = "",
            matchesPlayed = 2,
            won = 0,
            drawn = 1,
            lost = 1,
            points = 1,
            isLeader = false,
        ),
        StandingRowDto(
            position = 4,
            teamCode = "SAU",
            flagUrl = "",
            matchesPlayed = 2,
            won = 0,
            drawn = 0,
            lost = 2,
            points = 0,
            isLeader = false,
        ),
    )

private val previewGroups =
    listOf(
        GroupDto(id = "A", label = "Group A", standings = previewStandings),
        GroupDto(id = "B", label = "Group B", standings = emptyList()),
        GroupDto(id = "C", label = "Group C", standings = emptyList()),
    )

@Preview
@Composable
private fun GroupStandingsSectionPreview() {
    XTheme {
        GroupStandingsSection(
            groups = previewGroups,
            selectedGroup = previewGroups.first(),
            onGroupSelected = {},
        )
    }
}
