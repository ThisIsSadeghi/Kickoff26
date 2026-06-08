package thisissadeghi.kickoff

import thisissadeghi.kickoff.home.presentation.navigation.HomeRoute

enum class TopLevelDestination(
    val route: Any,
) {
    HOME(HomeRoute),
    MATCHES(HomeRoute),
    PROFILE(HomeRoute),
}
