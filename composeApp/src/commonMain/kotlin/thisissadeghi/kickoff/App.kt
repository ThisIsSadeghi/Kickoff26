package thisissadeghi.kickoff

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kickoff26.composeapp.generated.resources.Res
import kickoff26.composeapp.generated.resources.home_fill
import kickoff26.composeapp.generated.resources.person
import kickoff26.composeapp.generated.resources.sports_soccer
import kickoff26.composeapp.generated.resources.tab_home
import kickoff26.composeapp.generated.resources.tab_matches
import kickoff26.composeapp.generated.resources.tab_profile
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import thisissadeghi.kickoff.common.locale.ProvideAppLocale
import thisissadeghi.kickoff.designsystem.SnackbarController
import thisissadeghi.kickoff.designsystem.Toast
import thisissadeghi.kickoff.designsystem.XNavigationBar
import thisissadeghi.kickoff.designsystem.XNavigationBarItem
import thisissadeghi.kickoff.designsystem.XScaffold
import thisissadeghi.kickoff.designsystem.XTheme
import thisissadeghi.kickoff.designsystem.rememberToastState

@Composable
fun App() {
    ProvideAppLocale {
        AppContent()
    }
}

@Composable
private fun AppContent() {
    XTheme {
        val toastState = rememberToastState()
        val snackbarHostState = remember { SnackbarHostState() }
        var selectedDestination by remember { mutableStateOf(TopLevelDestination.HOME) }

        XScaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                )
            },
            bottomBar = {
                XNavigationBar(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    containerColor = MaterialTheme.colorScheme.surface,
                    windowInsets = NavigationBarDefaults.windowInsets,
                ) {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = selectedDestination == destination
                        XNavigationBarItem(
                            selected = selected,
                            onClick = { selectedDestination = destination },
                            colors =
                                NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            icon = {
                                when (destination) {
                                    TopLevelDestination.HOME ->
                                        Icon(
                                            painter = painterResource(Res.drawable.home_fill),
                                            contentDescription = stringResource(Res.string.tab_home),
                                        )
                                    TopLevelDestination.MATCHES ->
                                        Icon(
                                            painter = painterResource(Res.drawable.sports_soccer),
                                            contentDescription = stringResource(Res.string.tab_matches),
                                        )
                                    TopLevelDestination.PROFILE ->
                                        Icon(
                                            painter = painterResource(Res.drawable.person),
                                            contentDescription = stringResource(Res.string.tab_profile),
                                        )
                                }
                            },
                            label = {
                                Text(
                                    text =
                                        when (destination) {
                                            TopLevelDestination.HOME -> stringResource(Res.string.tab_home)
                                            TopLevelDestination.MATCHES -> stringResource(Res.string.tab_matches)
                                            TopLevelDestination.PROFILE -> stringResource(Res.string.tab_profile)
                                        },
                                )
                            },
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { innerPadding ->
            BaseAppNavHost(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                        ).imePadding()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
            )

            Toast(state = toastState)

            SnackbarController(snackbarHostState = snackbarHostState)
        }
    }
}
