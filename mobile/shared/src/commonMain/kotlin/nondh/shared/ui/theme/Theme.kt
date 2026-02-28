package nondh.shared.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WarmScheme = lightColorScheme(
    background = WarmBackground,
    surface = WarmSurface,
    primary = WarmPrimary,
    onPrimary = WarmOnPrimary,
    onSurface = WarmOnSurface,
    onSurfaceVariant = WarmOnSurfaceVariant,
    error = WarmError
)

@Composable
fun NondhTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WarmScheme,
        typography = NondhTypography,
        content = content
    )
}
