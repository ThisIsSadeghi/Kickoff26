package thisissadeghi.kickoff.designsystem.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import thisissadeghi.kickoff.designsystem.XCircularProgressIndicator

/**
 * Shared, project-level **Loading** state — one design per project, reused by every feature for
 * Rule 4's `UiState.Loading`. Features call this instead of re-implementing a per-screen
 * `LoadingContent`.
 *
 * Lives in the [thisissadeghi.kickoff.designsystem.app] tier: this is the *project's own* composed UI
 * (not a generic primitive), so `install.sh` resets it to a neutral default for downstream
 * projects, which then redesign it via the design pipeline. Built only from generic primitives
 * ([XCircularProgressIndicator]); never imported by generic (root) design-system code.
 */
@Composable
fun AppLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        XCircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
