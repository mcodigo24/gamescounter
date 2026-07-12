@file:OptIn(ExperimentalMaterial3Api::class)

package com.gamescounter.truco.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.gamescounter.truco.R
import com.gamescounter.truco.SaveStatus
import com.gamescounter.truco.generala.GeneralaRow
import com.gamescounter.truco.generala.GeneralaViewModel
import com.gamescounter.truco.ui.theme.KountaBackground
import com.gamescounter.truco.generala.MAX_GENERALA_PLAYERS
import com.gamescounter.truco.generala.MIN_GENERALA_PLAYERS

@Composable
fun GeneralaScreen(
    viewModel: GeneralaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "¿Empezar una nueva planilla?") },
            text = { Text(text = "Se borran todos los puntajes cargados en Generala.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetGame()
                        showResetDialog = false
                    },
                ) { Text(text = "Reiniciar") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = "Cancelar")
                }
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KountaBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Generala")
                        when (uiState.saveStatus) {
                            SaveStatus.Pending -> {
                                Text(
                                    text = "Guardando en 30 s…",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            SaveStatus.Saved -> {
                                Text(
                                    text = "Progreso guardado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            SaveStatus.Idle -> Unit
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = { viewModel.decreasePlayers() },
                        enabled = uiState.score.playerInitials.size > MIN_GENERALA_PLAYERS,
                    ) {
                        Text("-")
                    }
                    Text(
                        text = uiState.score.playerInitials.size.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    FilledTonalIconButton(
                        onClick = { viewModel.increasePlayers() },
                        enabled = uiState.score.playerInitials.size < MAX_GENERALA_PLAYERS,
                    ) {
                        Text("+")
                    }
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = stringResource(R.string.reset_game))
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) return@Scaffold

        val horizontal = rememberScrollState()
        val vertical = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(vertical)
                .horizontalScroll(horizontal)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HeaderRow(
                initials = uiState.score.playerInitials,
                onInitialChange = viewModel::updateInitial,
            )

            GeneralaRow.entries.forEachIndexed { rowIndex, row ->
                ScoreRow(
                    label = row.label,
                    values = uiState.score.board.map { it[rowIndex] },
                    onCellTap = { playerIndex -> viewModel.cycleCell(playerIndex, rowIndex) },
                )
            }

            TotalRow(totals = uiState.score.board.indices.map { uiState.score.totalForPlayer(it) })

            if (uiState.score.playerInitials.size <= MIN_GENERALA_PLAYERS) {
                Text(
                    text = "Minimo de jugadores alcanzado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HeaderRow(
    initials: List<String>,
    onInitialChange: (Int, String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CellLabel(text = "#")
        initials.forEachIndexed { index, initial ->
            OutlinedTextField(
                value = initial,
                onValueChange = { onInitialChange(index, it) },
                singleLine = true,
                modifier = Modifier.width(64.dp),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
            )
        }
    }
}

@Composable
private fun ScoreRow(
    label: String,
    values: List<Int?>,
    onCellTap: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CellLabel(text = label)
        values.forEachIndexed { playerIndex, value ->
            ElevatedCard(
                onClick = { onCellTap(playerIndex) },
                modifier = Modifier.width(64.dp),
            ) {
                Text(
                    text = formatCell(value),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun TotalRow(totals: List<Int>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CellLabel(text = "Total")
        totals.forEach { total ->
            Surface(
                tonalElevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(64.dp),
            ) {
                Text(
                    text = total.toString(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun CellLabel(text: String) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(72.dp),
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun formatCell(value: Int?): String = when (value) {
    null -> "-"
    0 -> "X"
    else -> value.toString()
}
