package com.gamescounter.truco.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun isLandscape(): Boolean =
    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

/**
 * Total top-bar height for the compact/landscape layout: enough room for the
 * status bar inset (which the bar's own windowInsets still reserve) plus a
 * fixed content height for the title/icons row, so nothing gets clipped.
 */
@Composable
fun compactTopBarHeight(contentHeight: Dp = 44.dp): Dp =
    WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + contentHeight
