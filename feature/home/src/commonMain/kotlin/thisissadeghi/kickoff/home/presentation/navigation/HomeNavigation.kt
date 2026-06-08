package thisissadeghi.kickoff.home.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import thisissadeghi.kickoff.home.presentation.HomeScreen

@Serializable
object HomeRoute

fun NavGraphBuilder.home() {
    composable<HomeRoute> {
        HomeScreen(
            viewModel = koinViewModel(),
        )
    }
}
