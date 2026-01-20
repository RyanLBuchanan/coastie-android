package ai.adaskids.coastie.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Pulled from your web CSS vars / vibe:
// --ink #0b1220, --ocean #1e4e8c, --sea #24b0b8, --sun #ffb347, --foam #f3f6fb
private val CoastieLightColors = lightColorScheme(
    primary = Color(0xFF1E4E8C),        // ocean
    secondary = Color(0xFF24B0B8),      // sea
    tertiary = Color(0xFFFFB347),       // sun
    background = Color(0xFFF3F6FB),     // foam
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color(0xFF0B1220),
    onBackground = Color(0xFF0B1220),   // ink
    onSurface = Color(0xFF0B1220)
)

@Composable
fun CoastieTheme(content: @Composable () -> Unit) {
    // Force LIGHT mode for demo (ignores system theme)
    MaterialTheme(
        colorScheme = CoastieLightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
