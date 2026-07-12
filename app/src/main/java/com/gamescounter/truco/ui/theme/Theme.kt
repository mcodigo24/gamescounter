package com.gamescounter.truco.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KountaColorScheme = lightColorScheme(
    primary = KountaPrimary,
    onPrimary = Color.White,
    primaryContainer = KountaSurfaceVariant,
    onPrimaryContainer = KountaTextStrong,
    secondary = KountaSecondary,
    onSecondary = KountaTextStrong,
    secondaryContainer = Color(0xFFF0E4D6),
    onSecondaryContainer = KountaTextStrong,
    tertiary = KountaText,
    onTertiary = KountaTextStrong,
    background = KountaBackground,
    onBackground = KountaTextStrong,
    surface = KountaSurface,
    onSurface = KountaTextStrong,
    surfaceVariant = KountaSurfaceVariant,
    onSurfaceVariant = KountaTextMuted,
    outline = KountaBorder,
    outlineVariant = Color(0x1ACEAB93),
    error = LostRed,
    onError = Color.White,
    errorContainer = LostRedContainer,
    onErrorContainer = LostRed,
)

@Composable
fun KountaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = KountaColorScheme,
        typography = KountaTypography,
        content = content,
    )
}

/** Alias para compatibilidad con referencias previas. */
@Composable
fun GamesCounterTheme(
    content: @Composable () -> Unit,
) = KountaTheme(content = content)
