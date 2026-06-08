package thisissadeghi.kickoff.designsystem.app

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import thisissadeghi.kickoff.designsystem.DesignSystemResources
import thisissadeghi.kickoff.designsystem.XButton
import thisissadeghi.kickoff.designsystem.XIcon
import thisissadeghi.kickoff.designsystem.XText
import thisissadeghi.kickoff.designsystem.motion.XMotion
import thisissadeghi.kickoff.designsystem.motion.rememberReducedMotion

@Composable
fun AppErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = stringResource(DesignSystemResources.string.retry_label),
    secondaryAction: (@Composable () -> Unit)? = null,
) {
    val reduceMotion = rememberReducedMotion()
    val infiniteTransition = rememberInfiniteTransition(label = "error-icon")

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (reduceMotion) 1f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = XMotion.FLOAT, easing = XMotion.Standard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "icon-scale",
    )
    val iconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = if (reduceMotion) 0.8f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = XMotion.FLOAT, easing = XMotion.Standard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "icon-alpha",
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            XIcon(
                painter = painterResource(DesignSystemResources.drawable.sports_soccer),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(96.dp)
                    .scale(iconScale)
                    .alpha(iconAlpha),
            )

            Spacer(Modifier.height(32.dp))

            XText(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            XText(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                XButton(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    XText(
                        text = retryLabel.uppercase(),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                }
                secondaryAction?.invoke()
            }
        }
    }
}
