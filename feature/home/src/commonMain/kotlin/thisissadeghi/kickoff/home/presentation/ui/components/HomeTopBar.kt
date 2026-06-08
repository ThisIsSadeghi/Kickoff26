package thisissadeghi.kickoff.home.presentation.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kickoff26.feature.home.generated.resources.Res
import kickoff26.feature.home.generated.resources.home_app_name
import org.jetbrains.compose.resources.stringResource
import thisissadeghi.kickoff.designsystem.XText
import thisissadeghi.kickoff.designsystem.toolbar.XTopAppBar
import thisissadeghi.kickoff.designsystem.toolbar.XTopAppBarAlignment

@Composable
fun HomeTopBar() {
    XTopAppBar(
        title = {
            XText(
                text = stringResource(Res.string.home_app_name),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp),
                color = MaterialTheme.colorScheme.primary,
            )
        },
        backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
        alignment = XTopAppBarAlignment.Start,
    )
}
