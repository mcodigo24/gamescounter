@file:OptIn(ExperimentalMaterial3Api::class)

package com.gamescounter.truco.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.gamescounter.truco.R
import com.gamescounter.truco.SaveStatus
import com.gamescounter.truco.ui.theme.KountaBackground
import com.gamescounter.truco.ui.theme.KountaPrimary
import com.gamescounter.truco.ui.theme.KountaSecondary
import com.gamescounter.truco.ui.theme.KountaSurface

@Composable
fun GameTopBar(
    title: String,
    saveStatus: SaveStatus,
    onBack: () -> Unit,
    onReset: () -> Unit,
    actions: @Composable () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                when (saveStatus) {
                    SaveStatus.Pending -> Text(
                        text = stringResource(R.string.save_pending),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SaveStatus.Saved -> Text(
                        text = stringResource(R.string.save_saved),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    SaveStatus.Idle -> Unit
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        actions = {
            actions()
            IconButton(onClick = onReset) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.reset_game),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = KountaBackground,
            scrolledContainerColor = KountaSurface,
        ),
    )
}

@Composable
fun PlayerCountActions(
    playerCount: Int,
    minPlayers: Int,
    maxPlayers: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    val tonalColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = KountaSecondary.copy(alpha = 0.35f),
        contentColor = KountaPrimary,
    )
    FilledTonalIconButton(
        onClick = onDecrease,
        enabled = playerCount > minPlayers,
        colors = tonalColors,
    ) {
        Text("-")
    }
    Text(
        text = playerCount.toString(),
        modifier = Modifier.padding(horizontal = 6.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    FilledTonalIconButton(
        onClick = onIncrease,
        enabled = playerCount < maxPlayers,
        colors = tonalColors,
    ) {
        Text("+")
    }
}
