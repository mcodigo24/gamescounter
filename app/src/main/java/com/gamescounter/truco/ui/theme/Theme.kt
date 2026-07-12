package com.gamescounter.truco.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KountaColorScheme = darkColorScheme(
    primary = KountaPrimary,
    onPrimary = ClayOnAccent,
    primaryContainer = KountaSurfaceVariant,
    onPrimaryContainer = KountaTextStrong,
    secondary = KountaSecondary,
    onSecondary = ClayOnAccent,
    secondaryContainer = Color(0xFF3A2E22),
    onSecondaryContainer = KountaTextStrong,
    tertiary = KountaText,
    onTertiary = ClayOnAccent,
    background = KountaBackground,
    onBackground = KountaTextStrong,
    surface = KountaSurface,
    onSurface = KountaTextStrong,
    surfaceVariant = KountaSurfaceVariant,
    onSurfaceVariant = KountaTextMuted,
    outline = KountaBorder,
    outlineVariant = Color(0x1AF0B27A),
    error = LostRed,
    onError = ClayOnAccent,
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
