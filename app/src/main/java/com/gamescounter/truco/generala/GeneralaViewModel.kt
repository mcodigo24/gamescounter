package com.gamescounter.truco.generala

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamescounter.truco.SaveStatus
import com.gamescounter.truco.data.AUTO_SAVE_DELAY_MS
import com.gamescounter.truco.data.normalizePlayerName
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GeneralaUiState(
    val score: GeneralaScore = GeneralaScore(),
    val saveStatus: SaveStatus = SaveStatus.Idle,
    val isLoading: Boolean = true,
    val lastEditedCell: Pair<Int, Int>? = null,
)

class GeneralaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GeneralaRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(GeneralaUiState())
    val uiState: StateFlow<GeneralaUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null
    private var hasUnsavedChanges = false

    init {
        viewModelScope.launch {
            val saved = repository.load()
            _uiState.update {
                it.copy(
                    score = saved ?: GeneralaScore(),
                    isLoading = false,
                    saveStatus = if (saved != null) SaveStatus.Saved else SaveStatus.Idle,
                )
            }
        }
    }

    fun increasePlayers() {
        updateScore { it.withPlayerCount(it.playerInitials.size + 1) }
    }

    fun decreasePlayers() {
        updateScore { it.withPlayerCount(it.playerInitials.size - 1) }
    }

    fun updateInitial(playerIndex: Int, value: String) {
        val clean = normalizePlayerName(value)
        if (clean.isEmpty()) return
        updateScore { score ->
            val initials = score.playerInitials.toMutableList()
            if (playerIndex !in initials.indices) return@updateScore score
            initials[playerIndex] = clean
            score.copy(playerInitials = initials)
        }
    }

    fun cycleCell(playerIndex: Int, rowIndex: Int) {
        if (playerIndex !in _uiState.value.score.board.indices || rowIndex !in GeneralaRow.entries.indices) {
            return
        }
        updateScore { score ->
            val board = score.board.toMutableList()
            val playerRow = board[playerIndex].toMutableList()
            val rowRule = GeneralaRow.entries[rowIndex]
            val current = playerRow[rowIndex]
            val cycle = buildList {
                add(null) // empty
                addAll(rowRule.allowedValues)
                add(0) // X
            }
            val next = cycle[(cycle.indexOf(current).let { if (it < 0) 0 else it } + 1) % cycle.size]
            playerRow[rowIndex] = next
            board[playerIndex] = playerRow
            score.copy(board = board)
        }
        _uiState.update { it.copy(lastEditedCell = playerIndex to rowIndex) }
    }

    fun resetGame() {
        autoSaveJob?.cancel()
        hasUnsavedChanges = false
        viewModelScope.launch {
            repository.clear()
            _uiState.update {
                it.copy(score = GeneralaScore(), saveStatus = SaveStatus.Idle)
            }
        }
    }

    fun flushSave() {
        if (!hasUnsavedChanges) return
        autoSaveJob?.cancel()
        persistNow()
    }

    private fun updateScore(transform: (GeneralaScore) -> GeneralaScore) {
        val current = _uiState.value.score
        val updated = transform(current)
        if (updated == current) return

        hasUnsavedChanges = true
        _uiState.update {
            it.copy(score = updated, saveStatus = SaveStatus.Pending)
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
