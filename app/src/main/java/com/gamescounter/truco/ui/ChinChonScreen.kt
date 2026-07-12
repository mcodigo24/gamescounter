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
import com.gamescounter.truco.chinchon.CHINCHON_LOSE_THRESHOLD
import com.gamescounter.truco.chinchon.ChinChonViewModel
import com.gamescounter.truco.chinchon.DEFAULT_CHINCHON_PLAYERS
import com.gamescounter.truco.chinchon.MAX_CHINCHON_PLAYERS
import com.gamescounter.truco.chinchon.MIN_CHINCHON_PLAYERS
import com.gamescounter.truco.ui.components.GameTopBar
import com.gamescounter.truco.ui.components.GridLabel
import com.gamescounter.truco.ui.components.GridTotalCell
import com.gamescounter.truco.ui.components.PlayerSetupScreen
import com.gamescounter.truco.ui.components.SignedScoreField
import com.gamescounter.truco.ui.components.formatSignedInput
import com.gamescounter.truco.ui.components.KountaHint
import com.gamescounter.truco.ui.theme.KountaBackground
import com.gamescounter.truco.ui.theme.LostRed
import com.gamescounter.truco.ui.theme.LostRedContainer

@Composable
fun ChinChonScreen(
    viewModel: ChinChonViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) return

    if (uiState.showSetup) {
        PlayerSetupScreen(
            gameTitle = "Chin Chon",
            minPlayers = MIN_CHINCHON_PLAYERS,
            maxPlayers = MAX_CHINCHON_PLAYERS,
            defaultPlayers = DEFAULT_CHINCHON_PLAYERS,
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
            text = { Text("Se borraran todos los puntajes de Chin Chon.") },
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
                title = "Chin Chon",
                saveStatus = uiState.saveStatus,
                onBack = onBack,
                onReset = { showResetDialog = true },
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GridLabel(text = "Ronda", modifier = Modifier.width(88.dp))
                score.playerInitials.forEachIndexed { index, initial ->
                    val lost = score.isPlayerLost(index)
                    OutlinedTextField(
                        value = initial,
                        onValueChange = { viewModel.updateInitial(index, it) },
                        singleLine = true,
                        modifier = Modifier.width(88.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center,
                            color = if (lost) LostRed else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }

            score.rounds.forEachIndexed { roundIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GridLabel(text = (roundIndex + 1).toString(), modifier = Modifier.width(88.dp))
                    score.playerInitials.indices.forEach { playerIndex ->
                        val lost = score.isPlayerLost(playerIndex)
                        val colors = if (lost) {
                            LostRedContainer to LostRed
                        } else {
                            MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
                        }
                        SignedScoreField(
                            value = formatSignedInput(row.getOrNull(playerIndex)),
                            onValueChange = { viewModel.updateCell(roundIndex, playerIndex, it) },
                            containerColor = colors.first,
                            contentColor = colors.second,
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GridLabel(text = "Total", modifier = Modifier.width(88.dp))
                score.playerInitials.indices.forEach { playerIndex ->
                    val lost = score.isPlayerLost(playerIndex)
                    val total = score.totalForPlayer(playerIndex)
                    GridTotalCell(
                        value = total.toString(),
                        modifier = Modifier.width(88.dp),
                        containerColor = if (lost) LostRedContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (lost) LostRed else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            KountaHint(
                text = "Al llegar a $CHINCHON_LOSE_THRESHOLD o mas, la columna se marca en rojo. " +
                    "El proximo valor ingresado reinicia el conteo desde ese numero. " +
                    "Toca el +/- junto al numero para ingresar valores negativos.",
            )
        }
    }
}
