package com.gamescounter.truco.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta Kounta (estricta)
val KountaPrimary = Color(0xFFAD8B73)
val KountaSecondary = Color(0xFFCEAB93)
val KountaBackground = Color(0xFFFFFBE9)
val KountaText = Color(0xFFE3CAA5)

// Derivados para legibilidad y estados (armonía con la paleta)
val KountaTextStrong = Color(0xFF6B5344)
val KountaTextMuted = Color(0xFFCEAB93)
val KountaSurface = Color(0xFFFFFCF2)
val KountaSurfaceVariant = Color(0xFFF5EDD9)
val KountaBorder = Color(0x33AD8B73)
val KountaBorderFocus = Color(0x99CEAB93)

val LostRed = Color(0xFF9E6B6B)
val LostRedContainer = Color(0xFFF5E8E8)
val WinAccent = KountaPrimary
val WinAccentContainer = Color(0xFFE8D5C8)

// Compatibilidad con referencias existentes
val GreenDeep = KountaPrimary
val GreenLight = KountaSecondary
val Cream = KountaBackground
val Charcoal = KountaTextStrong
val Slate = KountaTextMuted
val DividerLine = KountaBorder
val WinGreen = WinAccent
val WinGreenContainer = WinAccentContainer
