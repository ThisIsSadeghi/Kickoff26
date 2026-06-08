package thisissadeghi.kickoff.designsystem.app

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import thisissadeghi.kickoff.designsystem.XCircularProgressIndicator
import thisissadeghi.kickoff.designsystem.motion.XMotion
import thisissadeghi.kickoff.designsystem.motion.rememberReducedMotion

@Composable
fun AppLoadingState(modifier: Modifier = Modifier) {
    val reduceMotion = rememberReducedMotion()
    val infiniteTransition = rememberInfiniteTransition(label = "loading-glow")

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (reduceMotion) 1f else 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = XMotion.PULSE, easing = XMotion.Standard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow-scale",
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = if (reduceMotion) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = XMotion.PULSE, easing = XMotion.Standard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow-alpha",
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary

        // Atmospheric glow behind the spinner — pulses scale 1→1.5 and alpha 0.5→1
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(glowScale)
                .alpha(glowAlpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.15f),
                            primaryColor.copy(alpha = 0f),
                        ),
                    ),
                    shape = CircleShape,
                ),
        )

        XCircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        )
    }
}
