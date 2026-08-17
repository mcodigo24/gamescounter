@file:OptIn(ExperimentalMaterial3Api::class)

package com.gamescounter.truco.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.gamescounter.truco.R
import com.gamescounter.truco.generala.GeneralaRow
import com.gamescounter.truco.generala.GeneralaViewModel
import com.gamescounter.truco.ui.components.KeepScreenAwake
import com.gamescounter.truco.ui.components.KountaShapeSmall
import com.gamescounter.truco.ui.components.compactTopBarHeight
import com.gamescounter.truco.ui.components.isLandscape
import com.gamescounter.truco.ui.components.PlayerInitialField
import com.gamescounter.truco.ui.components.claymorphic
import com.gamescounter.truco.ui.components.kountaScreenInsets
import com.gamescounter.truco.ui.theme.KountaBackground
import com.gamescounter.truco.ui.theme.KountaBorder
import com.gamescounter.truco.ui.theme.KountaSurface
import com.gamescounter.truco.ui.theme.KountaSurfaceEmpty
import com.gamescounter.truco.ui.theme.KountaSurfaceVariant
import com.gamescounter.truco.ui.theme.WinAccent
import com.gamescounter.truco.ui.theme.WinAccentContainer
import com.gamescounter.truco.generala.MAX_GENERALA_PLAYERS
import com.gamescounter.truco.generala.MIN_GENERALA_PLAYERS

@Composable
fun GeneralaScreen(
    viewModel: GeneralaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    KeepScreenAwake(resetKey = uiState.score)
    var showResetDialog by remember { mutableStateOf(false) }
    var showTotals by remember { mutableStateOf(false) }

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
            TopAppBar(
                modifier = if (isLandscape()) Modifier.height(compactTopBarHeight()) else Modifier,
                title = {
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                        Text(text = "Generala")
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
                    IconButton(onClick = { showTotals = !showTotals }) {
                        Icon(
                            imageVector = if (showTotals) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showTotals) "Ocultar totales" else "Mostrar totales",
                        )
                    }
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = stringResource(R.string.reset_game))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KountaBackground),
            )
        },
    ) { padding ->
        if (uiState.isLoading) return@Scaffold

        val horizontal = rememberScrollState()
        val vertical = rememberScrollState()

        val labelWidth = 60.dp
        val minCellWidth = 60.dp
        val minRowHeight = 44.dp
        val spacing = 6.dp

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            val playerCount = uiState.score.playerInitials.size.coerceAtLeast(1)
            val requiredWidth = labelWidth + spacing +
                minCellWidth * playerCount + spacing * (playerCount - 1).coerceAtLeast(0)
            val fitsWithoutScroll = requiredWidth <= maxWidth

            // header row + one row per GeneralaRow entry + optional total row
            val rowCount = GeneralaRow.entries.size + 1 + (if (showTotals) 1 else 0)
            val requiredHeight = minRowHeight * rowCount + spacing * (rowCount - 1)
            val fitsVertically = requiredHeight <= maxHeight

            Column(
                modifier = Modifier
                    .let { if (fitsVertically) it.fillMaxSize() else it.fillMaxWidth().verticalScroll(vertical) }
                    .let { if (fitsWithoutScroll) it else it.horizontalScroll(horizontal) },
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val rowModifier = if (fitsVertically) Modifier.weight(1f) else Modifier.height(minRowHeight)

                HeaderRow(
                    initials = uiState.score.playerInitials,
                    onInitialChange = viewModel::updateInitial,
                    fitsWithoutScroll = fitsWithoutScroll,
                    labelWidth = labelWidth,
                    minCellWidth = minCellWidth,
                    spacing = spacing,
                    modifier = rowModifier,
                )

                GeneralaRow.entries.forEachIndexed { rowIndex, row ->
                    ScoreRow(
                        label = row.label,
                        values = uiState.score.board.map { it[rowIndex] },
                        onCellTap = { playerIndex -> viewModel.cycleCell(playerIndex, rowIndex) },
                        highlightedPlayerIndex = uiState.lastEditedCell
                            ?.takeIf { it.second == rowIndex }
                            ?.first,
                        fitsWithoutScroll = fitsWithoutScroll,
                        labelWidth = labelWidth,
                        minCellWidth = minCellWidth,
                        spacing = spacing,
                        modifier = rowModifier,
                    )
                }

                if (showTotals) {
                    TotalRow(
                        totals = uiState.score.board.indices.map { uiState.score.totalForPlayer(it) },
                        fitsWithoutScroll = fitsWithoutScroll,
                        labelWidth = labelWidth,
                        minCellWidth = minCellWidth,
                        spacing = spacing,
                        modifier = rowModifier,
                    )
                }

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
}

@Composable
private fun HeaderRow(
    initials: List<String>,
    onInitialChange: (Int, String) -> Unit,
    fitsWithoutScroll: Boolean,
    labelWidth: Dp,
    minCellWidth: Dp,
    spacing: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .let { if (fitsWithoutScroll) it.fillMaxWidth() else it },
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        CellLabel(text = "#", modifier = Modifier.width(labelWidth))
        initials.forEachIndexed { index, initial ->
            PlayerInitialField(
                value = initial,
                onValueChange = { onInitialChange(index, it) },
                modifier = cellModifier(fitsWithoutScroll, minCellWidth),
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
    highlightedPlayerIndex: Int?,
    fitsWithoutScroll: Boolean,
    labelWidth: Dp,
    minCellWidth: Dp,
    spacing: Dp,
    modifier: Modifier = Modifier,
) {
    // Clickable Surfaces otherwise enforce a minimum touch target, which makes
    // them taller than the non-clickable label/total cells in the same row.
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Row(
            modifier = modifier
                .fillMaxHeight()
                .let { if (fitsWithoutScroll) it.fillMaxWidth() else it },
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            CellLabel(text = label, modifier = Modifier.width(labelWidth))
            values.forEachIndexed { playerIndex, value ->
                val isEmpty = value == null
                // Highlights the most recently tapped cell (across all rows) so players
                // can see who played last and what they scored.
                val isLastEdited = playerIndex == highlightedPlayerIndex
                Surface(
                    onClick = { onCellTap(playerIndex) },
                    shape = KountaShapeSmall,
                    color = when {
                        isLastEdited -> WinAccentContainer
                        isEmpty -> KountaSurfaceEmpty
                        else -> KountaSurface
                    },
                    border = BorderStroke(if (isLastEdited) 2.dp else 1.dp, if (isLastEdited) WinAccent else KountaBorder),
                    modifier = cellModifier(fitsWithoutScroll, minCellWidth)
                        .claymorphic(shape = KountaShapeSmall, elevation = 5.dp),
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = formatCell(value),
                            textAlign = TextAlign.Center,
                            color = when {
                                isLastEdited -> WinAccent
                                isEmpty -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isEmpty && !isLastEdited) FontWeight.Normal else FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalRow(
    totals: List<Int>,
    fitsWithoutScroll: Boolean,
    labelWidth: Dp,
    minCellWidth: Dp,
    spacing: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .let { if (fitsWithoutScroll) it.fillMaxWidth() else it },
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        CellLabel(text = "Total", modifier = Modifier.width(labelWidth))
        totals.forEach { total ->
            Surface(
                shape = KountaShapeSmall,
                color = KountaSurfaceVariant,
                border = BorderStroke(1.dp, KountaBorder),
                modifier = cellModifier(fitsWithoutScroll, minCellWidth)
                    .claymorphic(shape = KountaShapeSmall, elevation = 6.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = total.toString(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CellLabel(text: String, modifier: Modifier = Modifier.width(72.dp)) {
    Surface(
        shape = KountaShapeSmall,
        color = KountaSurfaceVariant,
        border = BorderStroke(1.dp, KountaBorder),
        modifier = modifier
            .fillMaxHeight()
            .claymorphic(shape = KountaShapeSmall, elevation = 4.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun RowScope.cellModifier(fitsWithoutScroll: Boolean, minCellWidth: Dp): Modifier =
    (if (fitsWithoutScroll) Modifier.weight(1f) else Modifier.width(minCellWidth)).fillMaxHeight()

private fun formatCell(value: Int?): String = when (value) {
    null -> "-"
    0 -> "X"
    else -> value.toString()
}
