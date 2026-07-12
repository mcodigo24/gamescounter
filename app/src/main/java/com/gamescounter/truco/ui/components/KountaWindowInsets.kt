package com.gamescounter.truco.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

/**
 * Top/horizontal-only safe drawing insets for per-game Scaffolds.
 * The bottom inset is owned by the app-level [KountaBottomNavBar] (in MainActivity's
 * outer Scaffold), so per-screen Scaffolds must not reserve it a second time.
 */
@Composable
fun kountaScreenInsets(): WindowInsets =
    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
