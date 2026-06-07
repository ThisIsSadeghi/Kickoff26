package thisissadeghi.kickoff

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import thisissadeghi.kickoff.common.locale.ProvideAppLocale
import thisissadeghi.kickoff.designsystem.SnackbarController
import thisissadeghi.kickoff.designsystem.Toast
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

        // The single app-shell Scaffold : owns shared chrome + the screen-frame insets.
        // contentWindowInsets = 0 so the Scaffold consumes nothing. The NavHost is padded by the
        // TOP + HORIZONTAL safe area (status bar + display cutout) plus imePadding, but NOT the
        // bottom — so bottom action bars can bleed their background to the screen edge and inset
        // their own content via navigationBarsPadding (standard edge-to-edge pattern).
        XScaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                )
            },
            bottomBar = {},
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { _ ->
            BaseAppNavHost(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                        ).imePadding(),
            )

            Toast(state = toastState)

            SnackbarController(snackbarHostState = snackbarHostState)
        }
    }
}
