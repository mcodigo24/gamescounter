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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamescounter.truco.comodin.ComodinViewModel
import com.gamescounter.truco.comodin.DEFAULT_COMODIN_PLAYERS
import com.gamescounter.truco.comodin.DEFAULT_COMODIN_TITLE
import com.gamescounter.truco.comodin.MAX_COMODIN_PLAYERS
import com.gamescounter.truco.comodin.MIN_COMODIN_PLAYERS
import com.gamescounter.truco.ui.components.GameTopBar
import com.gamescounter.truco.ui.components.GridLabel
import com.gamescounter.truco.ui.components.GridTotalCell
import com.gamescounter.truco.ui.components.KountaHint
import com.gamescounter.truco.ui.components.KountaPrimaryButton
import com.gamescounter.truco.ui.components.KountaSection
import com.gamescounter.truco.ui.components.PlayerInitialField
import com.gamescounter.truco.ui.components.SignedScoreField
import com.gamescounter.truco.ui.components.UnsignedScoreField
import com.gamescounter.truco.ui.components.formatSignedInput
import com.gamescounter.truco.ui.components.formatUnsignedInput
import com.gamescounter.truco.ui.components.kountaScreenInsets
import com.gamescounter.truco.ui.theme.KountaBackground
import com.gamescounter.truco.ui.theme.KountaPrimary
import com.gamescounter.truco.ui.theme.KountaSecondary

@Composable
fun ComodinScreen(
    viewModel: ComodinViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) return

    if (uiState.showSetup) {
        ComodinSetupScreen(
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
            text = { Text("Se borraran todos los puntajes de Comodín.") },
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

    var showTitleDialog by remember { mutableStateOf(false) }
    if (showTitleDialog) {
        var titleInput by remember(score.title) { mutableStateOf(score.title) }
        AlertDialog(
            onDismissRequest = { showTitleDialog = false },
            title = { Text("Nombre del juego") },
            text = {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    singleLine = true,
                    placeholder = { Text(DEFAULT_COMODIN_TITLE) },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateTitle(titleInput)
                        showTitleDialog = false
                    },
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showTitleDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }

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
                title = score.title,
                saveStatus = uiState.saveStatus,
                onBack = onBack,
                onReset = { showResetDialog = true },
                actions = {
                    IconButton(onClick = { showTitleDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar nombre del juego",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    IconButton(
                        onClick = viewModel::addPlayer,
                        enabled = score.playerInitials.size < MAX_COMODIN_PLAYERS,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar equipo",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
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
                Row(
                    modifier = (if (fitsWithoutScroll) Modifier.fillMaxWidth() else Modifier).height(rowHeight),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    GridLabel(text = "Ronda", modifier = Modifier.width(labelWidth).fillMaxHeight())
                    score.playerInitials.forEachIndexed { index, initial ->
                        PlayerInitialField(
                            value = initial,
                            onValueChange = { viewModel.updateInitial(index, it) },
                            modifier = cellModifier(fitsWithoutScroll, minCellWidth),
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                textAlign = TextAlign.Center,
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
                            if (score.allowNegatives) {
                                SignedScoreField(
                                    value = formatSignedInput(row.getOrNull(playerIndex)),
                                    onValueChange = { viewModel.updateCell(roundIndex, playerIndex, it) },
                                    modifier = cellModifier(fitsWithoutScroll, minCellWidth),
                                )
                            } else {
                                UnsignedScoreField(
                                    value = formatUnsignedInput(row.getOrNull(playerIndex)),
                                    onValueChange = { viewModel.updateCell(roundIndex, playerIndex, it) },
                                    modifier = cellModifier(fitsWithoutScroll, minCellWidth),
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = (if (fitsWithoutScroll) Modifier.fillMaxWidth() else Modifier).height(rowHeight),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    GridLabel(text = "Total", modifier = Modifier.width(labelWidth).fillMaxHeight())
                    score.playerInitials.indices.forEach { playerIndex ->
                        val total = score.totalForPlayer(playerIndex)
                        GridTotalCell(
                            value = total.toString(),
                            modifier = cellModifier(fitsWithoutScroll, minCellWidth),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                KountaHint(
                    text = if (score.allowNegatives) {
                        "Toca el +/- junto al numero para ingresar valores negativos."
                    } else {
                        "Esta planilla solo acepta valores positivos."
                    },
                    modifier = Modifier.width(availableWidth),
                )
            }
        }
    }
}

@Composable
private fun ComodinSetupScreen(
    onBack: () -> Unit,
    onStart: (playerCount: Int, allowNegatives: Boolean, title: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var playerCount by remember { mutableIntStateOf(DEFAULT_COMODIN_PLAYERS) }
    var allowNegatives by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }

    val tonalColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = KountaSecondary.copy(alpha = 0.35f),
        contentColor = KountaPrimary,
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KountaBackground,
        contentWindowInsets = kountaScreenInsets(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Comodín", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = KountaBackground,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = "Cantidad de equipos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Elegi entre $MIN_COMODIN_PLAYERS y $MAX_COMODIN_PLAYERS equipos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            KountaSection(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalIconButton(
                        onClick = { playerCount = (playerCount - 1).coerceAtLeast(MIN_COMODIN_PLAYERS) },
                        enabled = playerCount > MIN_COMODIN_PLAYERS,
                        colors = tonalColors,
                    ) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = playerCount.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    FilledTonalIconButton(
                        onClick = { playerCount = (playerCount + 1).coerceAtMost(MAX_COMODIN_PLAYERS) },
                        enabled = playerCount < MAX_COMODIN_PLAYERS,
                        colors = tonalColors,
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            KountaSection(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aceptar valores negativos",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "Util para juegos donde se restan puntos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = allowNegatives,
                        onCheckedChange = { allowNegatives = it },
                    )
                }
            }

            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                singleLine = true,
                label = { Text("Nombre del juego (opcional)") },
                placeholder = { Text(DEFAULT_COMODIN_TITLE) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
            )

            KountaPrimaryButton(
                text = "Comenzar partida",
                onClick = { onStart(playerCount, allowNegatives, titleInput) },
            )
        }
    }
}

private fun RowScope.cellModifier(fitsWithoutScroll: Boolean, minCellWidth: Dp): Modifier =
    (if (fitsWithoutScroll) Modifier.weight(1f) else Modifier.width(minCellWidth)).fillMaxHeight()
