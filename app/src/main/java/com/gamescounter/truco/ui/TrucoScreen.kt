@file:OptIn(ExperimentalMaterial3Api::class)

package com.gamescounter.truco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamescounter.truco.R
import com.gamescounter.truco.SaveStatus
import com.gamescounter.truco.TrucoViewModel
import com.gamescounter.truco.data.MAX_SCORE
import com.gamescounter.truco.data.MIN_SCORE
import com.gamescounter.truco.ui.components.kountaScreenInsets
import com.gamescounter.truco.ui.theme.KountaBackground
import com.gamescounter.truco.ui.theme.KountaBorder
import com.gamescounter.truco.ui.theme.KountaPrimary
import com.gamescounter.truco.ui.theme.KountaSecondary
import com.gamescounter.truco.ui.theme.KountaSurface

@Composable
fun TrucoScreen(
    viewModel: TrucoViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_confirm_title)) },
            text = { Text(stringResource(R.string.reset_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetGame()
                        showResetDialog = false
                    },
                ) {
                    Text(stringResource(R.string.reset_confirm_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.reset_confirm_cancel))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = KountaBackground,
        contentWindowInsets = kountaScreenInsets(),
        topBar = {
            TopBar(
                saveStatus = uiState.saveStatus,
                onResetClick = { showResetDialog = true },
                onBack = onBack,
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TeamPanel(
                teamName = stringResource(R.string.team_nosotros),
                score = uiState.score.nosotros,
                onIncrement = viewModel::incrementNosotros,
                onDecrement = viewModel::decrementNosotros,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = KountaBorder,
            ) {}

            TeamPanel(
                teamName = stringResource(R.string.team_ellos),
                score = uiState.score.ellos,
                onIncrement = viewModel::incrementEllos,
                onDecrement = viewModel::decrementEllos,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TopBar(
    saveStatus: SaveStatus,
    onResetClick: () -> Unit,
    onBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.truco_title),
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
                )
            }
        },
        actions = {
            FilledTonalIconButton(onClick = onResetClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.reset_game),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = KountaBackground,
        ),
    )
}

@Composable
private fun TeamPanel(
    teamName: String,
    score: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canDecrease = score > MIN_SCORE
    val canIncrease = score < MAX_SCORE

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = teamName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = score.toString(),
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScoreButton(
                enabled = canDecrease,
                onClick = onDecrement,
                icon = Icons.Default.Remove,
                contentDescription = stringResource(R.string.decrease),
                filled = false,
            )

            ScoreButton(
                enabled = canIncrease,
                onClick = onIncrement,
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.increase),
                filled = true,
            )
        }
    }
}

@Composable
private fun ScoreButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    filled: Boolean,
) {
    val colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = if (filled) {
            KountaSecondary
        } else {
            KountaSurface
        },
        contentColor = if (filled) {
            MaterialTheme.colorScheme.onSecondary
        } else {
            KountaPrimary
        },
        disabledContainerColor = KountaSurface.copy(alpha = 0.5f),
        disabledContentColor = KountaPrimary.copy(alpha = 0.35f),
    )

    if (filled) {
        FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            colors = colors,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(32.dp),
            )
        }
    } else {
        FilledTonalIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            colors = colors,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
