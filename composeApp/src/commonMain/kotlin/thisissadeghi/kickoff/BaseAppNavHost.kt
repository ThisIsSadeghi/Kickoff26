package thisissadeghi.kickoff

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import thisissadeghi.kickoff.designsystem.XNavHost
import thisissadeghi.kickoff.home.presentation.navigation.HomeRoute
import thisissadeghi.kickoff.home.presentation.navigation.home

/**
 * Main app navigation host.
 */
@Composable
fun BaseAppNavHost(modifier: Modifier) {
    val navController = rememberNavController()

    XNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = HomeRoute,
    ) {
        home()
    }
}
