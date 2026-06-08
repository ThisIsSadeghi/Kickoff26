package thisissadeghi.kickoff.home.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import thisissadeghi.kickoff.common.UiState
import thisissadeghi.kickoff.home.data.model.GroupDto
import thisissadeghi.kickoff.home.data.model.MatchDto

data class HomeUiModel(
    val countdown: CountdownDto = CountdownDto(),
    val eventDateLabel: String = "",
    val matches: ImmutableList<MatchDto> = persistentListOf(),
    val groups: ImmutableList<GroupDto> = persistentListOf(),
    val selectedGroup: GroupDto = GroupDto(),
    val matchesState: UiState<List<MatchDto>> = UiState.Uninitialized,
    val standingsState: UiState<List<GroupDto>> = UiState.Uninitialized,
)
