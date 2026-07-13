@file:OptIn(ExperimentalMaterial3Api::class)

package com.gamescounter.truco.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
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
import com.gamescounter.truco.ui.components.PlayerInitialField
import com.gamescounter.truco.ui.components.PlayerSetupScreen
import com.gamescounter.truco.ui.components.KountaHint
import com.gamescounter.truco.ui.components.UnsignedScoreField
import com.gamescounter.truco.ui.components.formatUnsignedInput
import com.gamescounter.truco.ui.components.kountaScreenInsets
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
            gameTitle = "10 mil",
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
            text = { Text("Se borraran todos los puntajes de 10 mil.") },
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

    val labelWidth = 56.dp
    val minCellWidth = 104.dp
    val spacing = 8.dp
    val rowHeight = 52.dp

    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    focusManager.clearFocus()
                }
            },
        containerColor = KountaBackground,
        contentWindowInsets = kountaScreenInsets(),
        topBar = {
            GameTopBar(
                title = "10 mil",
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
        ) {
            val playerCount = score.playerInitials.size.coerceAtLeast(1)
            val requiredWidth = labelWidth + spacing +
                minCellWidth * playerCount + spacing * (playerCount - 1).coerceAtLeast(0)
            val fitsWithoutScroll = requiredWidth <= maxWidth
            val availableWidth = maxWidth

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(vertical)
                    .let { if (fitsWithoutScroll) it else it.horizontalScroll(horizontal) },
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.cellError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Row(
                    modifier = (if (fitsWithoutScroll) Modifier.fillMaxWidth() else Modifier).height(rowHeight),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    GridLabel(text = "Ronda", modifier = Modifier.width(labelWidth).fillMaxHeight())
                    score.playerInitials.forEachIndexed { index, initial ->
                        val started = score.hasStarted(index)
                        val won = score.hasWon(index)
                        val placement = score.placementForPlayer(index)
                        val active = started || won
                        val label = buildString {
                            append(initial)
                            placement?.let { append(" #$it") }
                        }
                        PlayerInitialField(
                            value = if (placement != null) label else initial,
                            onValueChange = { viewModel.updateInitial(index, it) },
                            readOnly = placement != null,
                            modifier = cellModifier(fitsWithoutScroll, minCellWidth),
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                textAlign = TextAlign.Center,
                                color = if (active) WinGreen else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }

                score.rounds.forEachIndexed { roundIndex, row ->
                    Row(
                        modifier = (if (fitsWithoutScroll) Modifier.fillMaxWidth() else Modifier).height(rowHeight),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                    ) {
                        GridLabel(text = (roundIndex + 1).toString(), modifier = Modifier.width(labelWidth).fillMaxHeight())
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
                                modifier = cellModifier(fitsWithoutScroll, minCellWidth),
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

                Row(
                    modifier = (if (fitsWithoutScroll) Modifier.fillMaxWidth() else Modifier).height(rowHeight),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    GridLabel(text = "Total", modifier = Modifier.width(labelWidth).fillMaxHeight())
                    score.playerInitials.indices.forEach { playerIndex ->
                        val won = score.hasWon(playerIndex)
                        val total = score.totalForPlayer(playerIndex)
                        GridTotalCell(
                            value = total.toString(),
                            modifier = cellModifier(fitsWithoutScroll, minCellWidth),
                            containerColor = if (won) WinGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (won) WinGreen else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Row(
                    modifier = (if (fitsWithoutScroll) Modifier.fillMaxWidth() else Modifier).height(rowHeight),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    GridLabel(text = "Faltan", modifier = Modifier.width(labelWidth).fillMaxHeight())
                    score.playerInitials.indices.forEach { playerIndex ->
                        val won = score.hasWon(playerIndex)
                        val remaining = if (won) 0 else score.remainingForPlayer(playerIndex)
                        GridTotalCell(
                            value = remaining.toString(),
                            modifier = cellModifier(fitsWithoutScroll, minCellWidth),
                            containerColor = if (won) WinGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (won) WinGreen else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                KountaHint(
                    text = "La primera casilla debe ser de $DIEZMIL_ENTRY_THRESHOLD o mas para empezar. " +
                        "Las casillas vacias cuentan como 0. Hay que llegar justo a $DIEZMIL_TARGET.",
                    modifier = Modifier.width(availableWidth),
                )
            }
        }
    }
}

private fun RowScope.cellModifier(fitsWithoutScroll: Boolean, minCellWidth: Dp): Modifier =
    (if (fitsWithoutScroll) Modifier.weight(1f) else Modifier.width(minCellWidth)).fillMaxHeight()
