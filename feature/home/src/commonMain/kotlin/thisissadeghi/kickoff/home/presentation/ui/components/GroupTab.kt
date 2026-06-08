package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import thisissadeghi.kickoff.designsystem.XText
import thisissadeghi.kickoff.designsystem.XTheme

@Composable
fun GroupTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(4.dp))
                .background(bgColor)
                .drawBehind {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2.dp.toPx(),
                    )
                }.clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        XText(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
            color = textColor,
        )
    }
}

@Preview
@Composable
private fun GroupTabSelectedPreview() {
    XTheme { GroupTab(label = "Group A", selected = true, onClick = {}) }
}

@Preview
@Composable
private fun GroupTabUnselectedPreview() {
    XTheme { GroupTab(label = "Group B", selected = false, onClick = {}) }
}
