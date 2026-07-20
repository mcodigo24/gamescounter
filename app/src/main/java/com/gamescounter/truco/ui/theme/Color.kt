package com.gamescounter.truco.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta Kounta — Claymorfismo oscuro, cálido (ámbar/durazno)
val KountaPrimary = Color(0xFFF0B27A)
val KountaSecondary = Color(0xFFE8A56C)
val KountaBackground = Color(0xFF3D3128)
val KountaText = Color(0xFFF5E6D8)

// Derivados para legibilidad y estados (armonía con la paleta)
val KountaTextStrong = Color(0xFFFBF3EA)
val KountaTextMuted = Color(0xFFB8A493)
val KountaSurface = Color(0xFF45382E)
val KountaNavBackground = Color(0xFF2E241D)
val KountaSurfaceVariant = Color(0xFF4E3E32)
val KountaSurfaceEmpty = Color(0xFF6E5847)
val KountaBorder = Color(0x33F0B27A)
val KountaBorderFocus = Color(0x99F0B27A)

val LostRed = Color(0xFFF08A8A)
val LostRedContainer = Color(0xFF3A2626)
val WinAccent = KountaPrimary
val WinAccentContainer = Color(0xFF3A2E22)

// Claymorfismo: sombra oscura (abajo-derecha) + luz (arriba-izquierda)
val ClayShadowDark = Color(0xFF000000)
val ClayHighlight = Color(0xFFFFFFFF)
val ClayOnAccent = Color(0xFF241E1A)

// Compatibilidad con referencias existentes
val GreenDeep = KountaPrimary
val GreenLight = KountaSecondary
val Cream = KountaBackground
val Charcoal = KountaTextStrong
val Slate = KountaTextMuted
val DividerLine = KountaBorder
val WinGreen = WinAccent
val WinGreenContainer = WinAccentContainer
