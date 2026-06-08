package thisissadeghi.kickoff.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kickoff26.core.designsystem.generated.resources.Res
import kickoff26.core.designsystem.generated.resources.outfit_bold
import kickoff26.core.designsystem.generated.resources.outfit_medium
import kickoff26.core.designsystem.generated.resources.outfit_regular
import org.jetbrains.compose.resources.Font

object XTheme {
    object Icons

    object Colors {
        // Semantic status colors — no M3 role equivalent
        val Success = Color(0xFF4ADE80)
        val Danger = Color(0xFFFF6B6B)
        val Gold = Color(0xFFFFD700) // Trophy gold — highlights, winner badges, countdown accents
    }
}

@Composable
fun XTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content,
        colorScheme = XDarkColors,
        shapes = Shapes,
        typography = XTypography(),
    )
}

private val Shapes =
    Shapes(
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(20.dp),
    )

internal val XLightColors =
    lightColorScheme(
        primary = Color(0xFF00A651),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFCCEEDD),
        onPrimaryContainer = Color(0xFF001F0A),
        secondary = Color(0xFF7A6200),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFE57A),
        onSecondaryContainer = Color(0xFF261A00),
        background = Color(0xFFF2FAF5),
        surface = Color(0xFFF7FCF9),
        onBackground = Color(0xFF091710),
        onSurface = Color(0xFF091710),
        onSurfaceVariant = Color(0xFF3A5441),
        surfaceVariant = Color(0xFFD3ECDC),
        surfaceContainer = Color(0xFFE8F5ED),
        surfaceContainerHigh = Color(0xFFDEEFE5),
        surfaceContainerHighest = Color(0xFFD4E9DC),
        surfaceContainerLow = Color(0xFFEFF9F3),
        surfaceContainerLowest = Color(0xFFF7FCF9),
        outline = Color(0xFF587860),
        outlineVariant = Color(0xFF9EC2A9),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color(0xFF410E0B),
        tertiary = Color(0xFF0284C7),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFBAE6FD),
        onTertiaryContainer = Color(0xFF002232),
    )

internal val XDarkColors =
    darkColorScheme(
        primary = Color(0xFF86E8AB),
        onPrimary = Color(0xFF003919),
        primaryContainer = Color(0xFF005227),
        onPrimaryContainer = Color(0xFFC8FFD7),
        secondary = Color(0xFFFFD700),
        onSecondary = Color(0xFF3B2F00),
        secondaryContainer = Color(0xFF564500),
        onSecondaryContainer = Color(0xFFFFDF7B),
        background = Color(0xFF0A1209),
        surface = Color(0xFF141E12),
        onBackground = Color(0xFFE2EEDF),
        onSurface = Color(0xFFE2EEDF),
        onSurfaceVariant = Color(0xFFA5C0A0),
        surfaceVariant = Color(0xFF1E3020),
        surfaceContainer = Color(0xFF192317),
        surfaceContainerHigh = Color(0xFF1E2C1B),
        surfaceContainerHighest = Color(0xFF243320),
        surfaceContainerLow = Color(0xFF141E12),
        surfaceContainerLowest = Color(0xFF0A1209),
        outline = Color(0xFF5C7A5A),
        outlineVariant = Color(0xFF2E4A2C),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        tertiary = Color(0xFF4FC3F7),
        onTertiary = Color(0xFF003547),
        tertiaryContainer = Color(0xFF004D63),
        onTertiaryContainer = Color(0xFFB8EAFF),
    )

/**
 * App-global type scale: the Material 3 [Typography] with every role re-pointed at
 * the project [FontFamily] (Outfit). M3 has no `defaultFontFamily`, so the family is
 * applied per-role via `.copy(fontFamily = …)`; each role keeps its M3 default
 * size/weight/line-height and the family resolves the matching weight file.
 *
 * Design-driven font swap: when a Stitch design uses a different typeface, the
 * implementation skills (`/creating-kmp-feature` / `/modifying-kmp-feature`) download
 * the new `.ttf` set into `composeResources/font/` and rewire [XFontFamily] below.
 */
@Composable
private fun XFontFamily(): FontFamily =
    FontFamily(
        Font(Res.font.outfit_regular, FontWeight.Normal),
        Font(Res.font.outfit_medium, FontWeight.Medium),
        Font(Res.font.outfit_bold, FontWeight.Bold),
    )

@Composable
private fun XTypography(): Typography {
    val fontFamily = XFontFamily()
    return with(MaterialTheme.typography) {
        copy(
            displayLarge = displayLarge.copy(fontFamily = fontFamily),
            displayMedium = displayMedium.copy(fontFamily = fontFamily),
            displaySmall = displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = titleLarge.copy(fontFamily = fontFamily),
            titleMedium = titleMedium.copy(fontFamily = fontFamily),
            titleSmall = titleSmall.copy(fontFamily = fontFamily),
            bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = bodySmall.copy(fontFamily = fontFamily),
            labelLarge = labelLarge.copy(fontFamily = fontFamily),
            labelMedium = labelMedium.copy(fontFamily = fontFamily),
            labelSmall = labelSmall.copy(fontFamily = fontFamily),
        )
    }
}
