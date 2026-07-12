package com.gamescounter.truco.diezmil

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamescounter.truco.SaveStatus
import com.gamescounter.truco.data.AUTO_SAVE_DELAY_MS
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiezMilUiState(
    val score: DiezMilScore = DiezMilScore(),
    val saveStatus: SaveStatus = SaveStatus.Idle,
    val isLoading: Boolean = true,
    val showSetup: Boolean = false,
    val cellError: String? = null,
)

class DiezMilViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DiezMilRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(DiezMilUiState())
    val uiState: StateFlow<DiezMilUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null
    private var hasUnsavedChanges = false

    init {
        viewModelScope.launch {
            val saved = repository.load()
            _uiState.update {
                it.copy(
                    score = saved ?: DiezMilScore(),
                    isLoading = false,
                    showSetup = saved == null,
                    saveStatus = if (saved != null) SaveStatus.Saved else SaveStatus.Idle,
                )
            }
        }
    }

    fun startGame(playerCount: Int) {
        val score = DiezMilScore()
            .withPlayerCount(playerCount)
            .ensureTrailingEmptyRound()
        updateScore(score)
        _uiState.update { it.copy(showSetup = false, cellError = null) }
    }

    fun increasePlayers() {
        updateScore { it.withPlayerCount(it.playerInitials.size + 1).ensureTrailingEmptyRound() }
    }

    fun decreasePlayers() {
        updateScore { current ->
            val nextCount = (current.playerInitials.size - 1)
                .coerceAtLeast(MIN_DIEZMIL_PLAYERS)
            val next = current.withPlayerCount(nextCount).ensureTrailingEmptyRound()
            next.copy(
                placementOrder = current.placementOrder.filter { it < nextCount },
            )
        }
    }

    fun updateInitial(playerIndex: Int, value: String) {
        val clean = value.trim().uppercase().take(1)
        if (clean.isEmpty()) return
        updateScore { score ->
            val initials = score.playerInitials.toMutableList()
            if (playerIndex !in initials.indices) return@updateScore score
            initials[playerIndex] = clean
            score.copy(playerInitials = initials)
        }
    }

    fun updateCell(roundIndex: Int, playerIndex: Int, rawValue: String) {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            clearCell(roundIndex, playerIndex)
            return
        }

        val parsed = trimmed.toIntOrNull()
        if (parsed == null || parsed < 0) {
            _uiState.update { it.copy(cellError = "Solo se permiten numeros positivos") }
            return
        }

        val current = _uiState.value.score
        if (current.wouldExceedTarget(playerIndex, roundIndex, parsed)) {
            _uiState.update { it.copy(cellError = "Te pasaste de 10000 puntos") }
            return
        }

        updateScore { score ->
            if (playerIndex !in score.playerInitials.indices) return@updateScore score

            val rounds = score.rounds.toMutableList()
            while (rounds.size <= roundIndex) {
                rounds.add(List(score.playerInitials.size) { null as Int? })
            }
            val row = rounds[roundIndex].toMutableList()
            row[playerIndex] = parsed
            rounds[roundIndex] = row

            var placement = score.placementOrder.toMutableList()
            val updated = score.copy(rounds = rounds).ensureTrailingEmptyRound()
            if (updated.hasWon(playerIndex) && playerIndex !in placement) {
                placement.add(playerIndex)
            } else if (!updated.hasWon(playerIndex) && playerIndex in placement) {
                placement = placement.filter { it != playerIndex }.toMutableList()
            }

            updated.copy(placementOrder = placement)
        }

        _uiState.update { it.copy(cellError = null) }
    }

    fun commitFirstRoundCell(playerIndex: Int) {
        val roundIndex = 0
        val value = _uiState.value.score.rounds.getOrNull(roundIndex)?.getOrNull(playerIndex)
        if (value == null || value >= DIEZMIL_ENTRY_THRESHOLD) return

        updateScore { score ->
            val rounds = score.rounds.toMutableList()
            val row = rounds[roundIndex].toMutableList()
            row[playerIndex] = 0
            rounds[roundIndex] = row
            score.copy(rounds = rounds)
        }
        _uiState.update {
            it.copy(cellError = "No has llegado a 750, intenta de nuevo en la próxima ronda")
        }
    }

    private fun clearCell(roundIndex: Int, playerIndex: Int) {
        updateScore { score ->
            if (roundIndex !in score.rounds.indices) return@updateScore score
            val row = score.rounds[roundIndex].toMutableList()
            if (playerIndex !in row.indices) return@updateScore score
            row[playerIndex] = null
            val rounds = score.rounds.toMutableList()
            rounds[roundIndex] = row

            var placement = score.placementOrder.toMutableList()
            val updated = score.copy(rounds = rounds)
            if (!updated.hasWon(playerIndex) && playerIndex in placement) {
                placement = placement.filter { it != playerIndex }.toMutableList()
            }
            updated.copy(placementOrder = placement)
        }
        _uiState.update { it.copy(cellError = null) }
    }

    fun resetGame() {
        autoSaveJob?.cancel()
        hasUnsavedChanges = false
        viewModelScope.launch {
            repository.clear()
            _uiState.update {
                it.copy(
                    score = DiezMilScore(),
                    saveStatus = SaveStatus.Idle,
                    showSetup = true,
                    cellError = null,
                )
            }
        }
    }

    fun flushSave() {
        if (!hasUnsavedChanges) return
        autoSaveJob?.cancel()
        persistNow()
    }

    private fun updateScore(transform: (DiezMilScore) -> DiezMilScore) {
        val current = _uiState.value.score
        val updated = transform(current)
        if (updated == current) return
        markDirty(updated)
    }

    private fun updateScore(score: DiezMilScore) {
        if (score == _uiState.value.score) return
        markDirty(score)
    }

    private fun markDirty(score: DiezMilScore) {
        hasUnsavedChanges = true
        _uiState.update {
            it.copy(score = score, saveStatus = SaveStatus.Pending)
        }
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DELAY_MS)
            persistNow()
        }
    }

    private fun persistNow() {
        if (!hasUnsavedChanges) return
        val snapshot = _uiState.value.score
        viewModelScope.launch {
            repository.save(snapshot)
            hasUnsavedChanges = false
            _uiState.update { it.copy(saveStatus = SaveStatus.Saved) }
        }
    }
}
