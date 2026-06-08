package thisissadeghi.kickoff.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import thisissadeghi.kickoff.common.Either
import thisissadeghi.kickoff.common.UiState
import thisissadeghi.kickoff.common.setState
import thisissadeghi.kickoff.home.data.model.GroupDto
import thisissadeghi.kickoff.home.data.repository.HomeRepository
import kotlin.time.Clock
import kotlin.time.Instant

class HomeViewModel(
    private val repository: HomeRepository,
) : ViewModel() {
    private val _uiModel =
        MutableStateFlow(
            HomeUiModel(eventDateLabel = "June 11, 2026 · 13:00 CST"),
        )
    val uiModel: StateFlow<HomeUiModel> = _uiModel.asStateFlow()

    init {
        startCountdown()
        loadMatches()
        loadStandings()
    }

    private fun startCountdown() {
        viewModelScope.launch {
            val target = Instant.parse("2026-06-11T19:00:00Z")
            while (true) {
                val now = Clock.System.now()
                val diff = (target - now).inWholeSeconds.coerceAtLeast(0)
                val days = diff / 86400
                val hours = (diff % 86400) / 3600
                val minutes = (diff % 3600) / 60
                val secs = diff % 60
                _uiModel.setState {
                    copy(
                        countdown =
                            CountdownDto(
                                days = days.toString().padStart(2, '0'),
                                hours = hours.toString().padStart(2, '0'),
                                minutes = minutes.toString().padStart(2, '0'),
                                seconds = secs.toString().padStart(2, '0'),
                            ),
                    )
                }
                delay(1000)
            }
        }
    }

    private fun loadMatches() {
        viewModelScope.launch {
            _uiModel.setState { copy(matchesState = UiState.Loading) }
            when (val result = repository.getMatches()) {
                is Either.Success ->
                    _uiModel.setState {
                        copy(
                            matchesState = UiState.Success(result.data),
                            matches = result.data.toImmutableList(),
                        )
                    }
                is Either.Failure ->
                    _uiModel.setState {
                        copy(matchesState = UiState.Failed(result.error))
                    }
            }
        }
    }

    private fun loadStandings() {
        viewModelScope.launch {
            _uiModel.setState { copy(standingsState = UiState.Loading) }
            when (val result = repository.getStandings()) {
                is Either.Success ->
                    _uiModel.setState {
                        copy(
                            standingsState = UiState.Success(result.data),
                            groups = result.data.toImmutableList(),
                            selectedGroup = result.data.firstOrNull() ?: GroupDto(),
                        )
                    }
                is Either.Failure ->
                    _uiModel.setState {
                        copy(standingsState = UiState.Failed(result.error))
                    }
            }
        }
    }

    fun onGroupSelected(group: GroupDto) {
        _uiModel.setState { copy(selectedGroup = group) }
    }

    fun retry() {
        loadMatches()
        loadStandings()
    }
}
