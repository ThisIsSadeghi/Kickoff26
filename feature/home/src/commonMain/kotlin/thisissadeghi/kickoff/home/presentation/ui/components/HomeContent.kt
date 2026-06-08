package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import thisissadeghi.kickoff.home.data.model.GroupDto
import thisissadeghi.kickoff.home.presentation.HomeUiModel

@Composable
fun HomeContent(
    uiModel: HomeUiModel,
    onGroupSelected: (GroupDto) -> Unit,
    onViewAllMatches: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            HeroCountdownCard(
                countdown = uiModel.countdown,
                eventDateLabel = uiModel.eventDateLabel,
            )
        }
        item {
            UpcomingMatchesSection(
                matches = uiModel.matches,
                onViewAllMatches = onViewAllMatches,
            )
        }
        item {
            GroupStandingsSection(
                groups = uiModel.groups,
                selectedGroup = uiModel.selectedGroup,
                onGroupSelected = onGroupSelected,
            )
        }
    }
}
