package thisissadeghi.kickoff.designsystem.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import thisissadeghi.kickoff.designsystem.DesignSystemResources
import thisissadeghi.kickoff.designsystem.Placeholder
import thisissadeghi.kickoff.designsystem.XButton
import thisissadeghi.kickoff.designsystem.XIcon
import thisissadeghi.kickoff.designsystem.XText

/**
 * Shared, project-level **Failed** state — one design per project, reused by every feature for
 * Rule 4's `UiState.Failed`. Features call this instead of re-implementing a per-screen
 * `FailedContent`.
 *
 * Copy and navigation are **parameters**, so nothing app-specific is baked in:
 *  - [title]/[message] come from the calling feature's own string resources (feature-specific copy);
 *  - [onRetry] is the primary action; [retryLabel] defaults to the shared design-system label;
 *  - [secondaryAction] is an optional slot for feature navigation (e.g. a "Return to …" button).
 *
 * Lives in the [thisissadeghi.kickoff.designsystem.app] tier (project-owned composed UI): `install.sh`
 * resets it to a neutral default for downstream projects, which redesign it via the design
 * pipeline. Built only from generic primitives ([Placeholder], [XButton], [XIcon], [XText]);
 * never imported by generic (root) design-system code.
 */
@Composable
fun AppErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = stringResource(DesignSystemResources.string.retry_label),
    secondaryAction: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Placeholder(
            modifier = Modifier.widthIn(max = 320.dp).padding(horizontal = 24.dp),
            icon = {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier =
                            Modifier
                                .size(96.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    shape = CircleShape,
                                ),
                    )
                    XIcon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp),
                    )
                }
            },
            title = {
                XText(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            },
            subtitle = {
                XText(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                )
            },
            action = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    XButton(
                        onClick = onRetry,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .widthIn(max = 220.dp)
                                .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    ) {
                        XText(text = retryLabel, fontWeight = FontWeight.Bold)
                    }
                    secondaryAction?.invoke()
                }
            },
        )
    }
}
