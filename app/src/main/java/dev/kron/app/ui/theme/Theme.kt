package dev.kron.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.kron.app.application.settings.AppAppearance

private val KronPrimary = Color(0xFFF1377E)
private val Light = lightColorScheme(
    primary = KronPrimary,
    onPrimary = Color.White,
    background = Color(0xFFF4F4F4),
    onBackground = Color(0xFF3A3A3A),
    surface = Color.White,
    onSurface = Color(0xFF1E1E1E),
    secondary = Color(0xFF393E41)
)
private val Dark = darkColorScheme(
    primary = KronPrimary,
    onPrimary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE2E2E2),
    surface = Color(0xFF1F1F1F),
    onSurface = Color(0xFFECECEC),
    secondary = Color(0xFFBDBDBD)
)

@Composable
fun KronTheme(appearance: AppAppearance, content: @Composable () -> Unit) {
    val dark = when (appearance) {
        AppAppearance.SYSTEM -> isSystemInDarkTheme()
        AppAppearance.DARK -> true
        AppAppearance.LIGHT -> false
    }
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
