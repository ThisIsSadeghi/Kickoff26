package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kickoff26.feature.home.generated.resources.Res
import kickoff26.feature.home.generated.resources.home_kickoff_label_template
import org.jetbrains.compose.resources.stringResource
import thisissadeghi.kickoff.designsystem.XText
import thisissadeghi.kickoff.designsystem.XTheme

@Composable
fun KickoffBadge(
    kickoffTime: String,
    modifier: Modifier = Modifier,
) {
    XText(
        text = stringResource(Res.string.home_kickoff_label_template, kickoffTime),
        style =
            MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun KickoffBadgePreview() {
    XTheme { KickoffBadge(kickoffTime = "13:00") }
}
