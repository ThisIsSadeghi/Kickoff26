package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import thisissadeghi.kickoff.designsystem.XText
import thisissadeghi.kickoff.designsystem.XTheme
import thisissadeghi.kickoff.designsystem.motion.rememberReducedMotion
import thisissadeghi.kickoff.home.presentation.ui.motion.countdownTransition

@Composable
fun CountdownUnit(
    value: String,
    label: String,
) {
    val reducedMotion = rememberReducedMotion()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(
            targetState = value,
            transitionSpec = { countdownTransition(reducedMotion) },
            label = "countdown_$label",
        ) { v ->
            XText(
                text = "$v$label",
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                    ),
                color = XTheme.Colors.Gold,
            )
        }
    }
}

@Preview
@Composable
private fun CountdownUnitPreview() {
    XTheme { CountdownUnit(value = "03", label = "D") }
}
