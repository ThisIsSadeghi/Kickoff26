package thisissadeghi.kickoff.home.presentation.ui.motion

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import thisissadeghi.kickoff.designsystem.motion.XMotion
import thisissadeghi.kickoff.designsystem.motion.rememberReducedMotion

val livePulseAnimSpec: InfiniteRepeatableSpec<Float> =
    infiniteRepeatable(
        animation = tween<Float>(durationMillis = XMotion.LIVE_PULSE, easing = XMotion.Standard),
        repeatMode = RepeatMode.Reverse,
    )

val livePulseAlpha: Float
    @Composable get() {
        val reducedMotion = rememberReducedMotion()
        if (reducedMotion) return 1f
        val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = livePulseAnimSpec,
            label = "live_dot_alpha",
        )
        return alpha
    }

fun countdownTransition(reducedMotion: Boolean): ContentTransform =
    if (reducedMotion) {
        fadeIn(tween(XMotion.INSTANT)) togetherWith fadeOut(tween(XMotion.INSTANT))
    } else {
        (slideInVertically(tween(XMotion.TICK, easing = FastOutLinearInEasing)) { -it } + fadeIn(tween(XMotion.TICK)))
            .togetherWith(
                slideOutVertically(tween(XMotion.TICK, easing = FastOutLinearInEasing)) { it } + fadeOut(tween(XMotion.TICK)),
            )
    }
