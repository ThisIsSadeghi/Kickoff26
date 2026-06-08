package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kickoff26.feature.home.generated.resources.Res
import kickoff26.feature.home.generated.resources.home_group_label_template
import org.jetbrains.compose.resources.stringResource
import thisissadeghi.kickoff.designsystem.XText
import thisissadeghi.kickoff.designsystem.XTheme

@Composable
fun GroupBadge(
    group: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
) {
    val textColor =
        if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    XText(
        text = stringResource(Res.string.home_group_label_template, group),
        style =
            MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            ),
        color = textColor,
        modifier =
            modifier
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Preview
@Composable
private fun GroupBadgePreview() {
    XTheme { GroupBadge(group = "A") }
}

@Preview
@Composable
private fun GroupBadgeHighlightedPreview() {
    XTheme { GroupBadge(group = "B", isHighlighted = true) }
}
