package thisissadeghi.kickoff.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kickoff26.feature.home.generated.resources.Res
import kickoff26.feature.home.generated.resources.home_error_message
import kickoff26.feature.home.generated.resources.home_error_title
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import thisissadeghi.kickoff.common.UiState
import thisissadeghi.kickoff.common.asStringOrNull
import thisissadeghi.kickoff.designsystem.XScreen
import thisissadeghi.kickoff.designsystem.XTheme
import thisissadeghi.kickoff.designsystem.app.AppErrorState
import thisissadeghi.kickoff.designsystem.app.AppLoadingState
import thisissadeghi.kickoff.home.data.model.GroupDto
import thisissadeghi.kickoff.home.data.model.MatchDto
import thisissadeghi.kickoff.home.data.model.StandingRowDto
import thisissadeghi.kickoff.home.data.model.TeamDto
import thisissadeghi.kickoff.home.presentation.ui.components.HomeContent
import thisissadeghi.kickoff.home.presentation.ui.components.HomeTopBar

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiModel by viewModel.uiModel.collectAsStateWithLifecycle()
    HomeScreenRoot(
        uiModel = uiModel,
        onGroupSelected = viewModel::onGroupSelected,
        onViewAllMatches = {},
        onRetry = viewModel::retry,
    )
}

@Composable
fun HomeScreenRoot(
    uiModel: HomeUiModel,
    onGroupSelected: (GroupDto) -> Unit,
    onViewAllMatches: () -> Unit,
    onRetry: () -> Unit,
) {
    val bothLoading = uiModel.matchesState is UiState.Loading && uiModel.standingsState is UiState.Loading
    val anyFailed = uiModel.matchesState is UiState.Failed || uiModel.standingsState is UiState.Failed
    val noData = uiModel.matches.isEmpty() && uiModel.groups.isEmpty()

    XScreen(
        topBar = { HomeTopBar() },
    ) {
        when {
            bothLoading && noData -> AppLoadingState()
            anyFailed && noData -> {
                val errorModel =
                    (uiModel.matchesState as? UiState.Failed)?.error
                        ?: (uiModel.standingsState as? UiState.Failed)?.error
                AppErrorState(
                    title = errorModel?.asStringOrNull() ?: stringResource(Res.string.home_error_title),
                    message = stringResource(Res.string.home_error_message),
                    onRetry = onRetry,
                )
            }
            else ->
                HomeContent(
                    uiModel = uiModel,
                    onGroupSelected = onGroupSelected,
                    onViewAllMatches = onViewAllMatches,
                )
        }
    }
}

@Preview
@Composable
private fun HomeScreenRootLoadingPreview() {
    XTheme {
        HomeScreenRoot(
            uiModel = HomeUiModel(matchesState = UiState.Loading, standingsState = UiState.Loading),
            onGroupSelected = {},
            onViewAllMatches = {},
            onRetry = {},
        )
    }
}

@Preview
@Composable
private fun HomeScreenRootSuccessPreview() {
    val team = TeamDto(code = "MEX", flagUrl = "")
    val group =
        GroupDto(
            id = "A",
            label = "Group A",
            standings =
                listOf(
                    StandingRowDto(1, "MEX", "", 2, 1, 1, 0, 4, true),
                    StandingRowDto(2, "POL", "", 2, 1, 0, 1, 3, false),
                ),
        )
    XTheme {
        HomeScreenRoot(
            uiModel =
                HomeUiModel(
                    countdown = CountdownDto("03", "14", "22", "07"),
                    eventDateLabel = "June 11, 2026 · 13:00 CST",
                    matches =
                        persistentListOf(
                            MatchDto("A", team, TeamDto("POL", ""), null, "13:00", false),
                        ),
                    groups = persistentListOf(group),
                    selectedGroup = group,
                    matchesState = UiState.Success(emptyList()),
                    standingsState = UiState.Success(emptyList()),
                ),
            onGroupSelected = {},
            onViewAllMatches = {},
            onRetry = {},
        )
    }
}
