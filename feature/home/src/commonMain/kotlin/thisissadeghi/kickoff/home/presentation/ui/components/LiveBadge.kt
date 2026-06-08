package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kickoff26.feature.home.generated.resources.Res
import kickoff26.feature.home.generated.resources.home_live_label
import org.jetbrains.compose.resources.stringResource
import thisissadeghi.kickoff.designsystem.XText
import thisissadeghi.kickoff.designsystem.XTheme
import thisissadeghi.kickoff.home.presentation.ui.motion.livePulseAlpha

@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    val dotAlpha = livePulseAlpha

    Row(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                    shape = CircleShape,
                ).padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = dotAlpha),
                        shape = CircleShape,
                    ),
        )
        XText(
            text = stringResource(Res.string.home_live_label),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                ),
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Preview
@Composable
private fun LiveBadgePreview() {
    XTheme { LiveBadge() }
}
