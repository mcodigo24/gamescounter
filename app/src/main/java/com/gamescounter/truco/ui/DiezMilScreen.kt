@file:OptIn(ExperimentalMaterial3Api::class)

package com.gamescounter.truco.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamescounter.truco.diezmil.DEFAULT_DIEZMIL_PLAYERS
import com.gamescounter.truco.diezmil.DIEZMIL_ENTRY_THRESHOLD
import com.gamescounter.truco.diezmil.DIEZMIL_TARGET
import com.gamescounter.truco.diezmil.DiezMilViewModel
import com.gamescounter.truco.diezmil.MAX_DIEZMIL_PLAYERS
import com.gamescounter.truco.diezmil.MIN_DIEZMIL_PLAYERS
import com.gamescounter.truco.ui.components.GameTopBar
import com.gamescounter.truco.ui.components.GridLabel
import com.gamescounter.truco.ui.components.GridTotalCell
import com.gamescounter.truco.ui.components.PlayerCountActions
import com.gamescounter.truco.ui.components.PlayerSetupScreen
import com.gamescounter.truco.ui.components.KountaHint
import com.gamescounter.truco.ui.components.UnsignedScoreField
import com.gamescounter.truco.ui.components.formatUnsignedInput
import com.gamescounter.truco.ui.theme.KountaBackground
import com.gamescounter.truco.ui.theme.WinGreen
import com.gamescounter.truco.ui.theme.WinGreenContainer

@Composable
fun DiezMilScreen(
    viewModel: DiezMilViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) return

    if (uiState.showSetup) {
        PlayerSetupScreen(
            gameTitle = "10Mil",
            minPlayers = MIN_DIEZMIL_PLAYERS,
            maxPlayers = MAX_DIEZMIL_PLAYERS,
            defaultPlayers = DEFAULT_DIEZMIL_PLAYERS,
            onBack = onBack,
            onStart = viewModel::startGame,
            modifier = modifier,
        )
        return
    }

    var showResetDialog by remember { mutableStateOf(false) }
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("¿Empezar una nueva partida?") },
            text = { Text("Se borraran todos los puntajes de 10Mil.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetGame()
                        showResetDialog = false
                    },
                ) { Text("Reiniciar") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }

    val score = uiState.score
    val horizontal = rememberScrollState()
    val vertical = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KountaBackground,
        topBar = {
            GameTopBar(
                title = "10Mil",
                saveStatus = uiState.saveStatus,
                onBack = onBack,
                onReset = { showResetDialog = true },
                actions = {
                    PlayerCountActions(
                        playerCount = score.playerInitials.size,
                        minPlayers = MIN_DIEZMIL_PLAYERS,
                        maxPlayers = MAX_DIEZMIL_PLAYERS,
                        onDecrease = viewModel::decreasePlayers,
                        onIncrease = viewModel::increasePlayers,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(vertical)
                .horizontalScroll(horizontal)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.cellError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GridLabel(text = "Ronda")
                score.playerInitials.forEachIndexed { index, initial ->
                    val started = score.hasStarted(index)
                    val won = score.hasWon(index)
                    val placement = score.placementForPlayer(index)
                    val active = started || won
                    val label = buildString {
                        append(initial)
                        placement?.let { append(" #$it") }
                    }
                    OutlinedTextField(
                        value = if (placement != null) label else initial,
                        onValueChange = { viewModel.updateInitial(index, it) },
                        singleLine = true,
                        readOnly = placement != null,
                        modifier = Modifier.width(72.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center,
                            color = if (active) WinGreen else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }

            score.rounds.forEachIndexed { roundIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GridLabel(text = (roundIndex + 1).toString())
                    score.playerInitials.indices.forEach { playerIndex ->
                        val won = score.hasWon(playerIndex)
                        val started = score.hasStarted(playerIndex)
                        val active = started || won
                        val colors = if (won) {
                            WinGreenContainer to WinGreen
                        } else if (active) {
                            WinGreenContainer.copy(alpha = 0.35f) to WinGreen
                        } else {
                            MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
                        }
                        UnsignedScoreField(
                            value = formatUnsignedInput(row.getOrNull(playerIndex)),
                            onValueChange = { viewModel.updateCell(roundIndex, playerIndex, it) },
                            containerColor = colors.first,
                            contentColor = colors.second,
                            onFocusLost = if (roundIndex == 0) {
                                { viewModel.commitFirstRoundCell(playerIndex) }
                            } else {
                                null
                            },
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GridLabel(text = "Total")
                score.playerInitials.indices.forEach { playerIndex ->
                    val won = score.hasWon(playerIndex)
                    val total = score.totalForPlayer(playerIndex)
                    GridTotalCell(
                        value = total.toString(),
                        containerColor = if (won) WinGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (won) WinGreen else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GridLabel(text = "Faltan")
                score.playerInitials.indices.forEach { playerIndex ->
                    val won = score.hasWon(playerIndex)
                    val remaining = if (won) 0 else score.remainingForPlayer(playerIndex)
                    GridTotalCell(
                        value = remaining.toString(),
                        containerColor = if (won) WinGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (won) WinGreen else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            KountaHint(
                text = "La primera casilla debe ser de $DIEZMIL_ENTRY_THRESHOLD o mas para empezar. " +
                    "Las casillas vacias cuentan como 0. Hay que llegar justo a $DIEZMIL_TARGET.",
            )
        }
    }
}
