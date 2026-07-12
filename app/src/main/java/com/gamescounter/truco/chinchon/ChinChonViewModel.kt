package com.gamescounter.truco.chinchon

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

data class ChinChonUiState(
    val score: ChinChonScore = ChinChonScore(),
    val saveStatus: SaveStatus = SaveStatus.Idle,
    val isLoading: Boolean = true,
    val showSetup: Boolean = false,
)

class ChinChonViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChinChonRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ChinChonUiState())
    val uiState: StateFlow<ChinChonUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null
    private var hasUnsavedChanges = false

    init {
        viewModelScope.launch {
            val saved = repository.load()
            _uiState.update {
                it.copy(
                    score = saved ?: ChinChonScore(),
                    isLoading = false,
                    showSetup = saved == null,
                    saveStatus = if (saved != null) SaveStatus.Saved else SaveStatus.Idle,
                )
            }
        }
    }

    fun startGame(playerCount: Int) {
        val score = ChinChonScore()
            .withPlayerCount(playerCount)
            .ensureTrailingEmptyRound()
        updateScore(score)
        _uiState.update { it.copy(showSetup = false) }
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
        val parsed = when {
            trimmed.isEmpty() || trimmed == "-" -> null
            else -> trimmed.toIntOrNull()
        }

        if (trimmed.isNotEmpty() && trimmed != "-" && parsed == null) return

        updateScore { score ->
            if (playerIndex !in score.playerInitials.indices) return@updateScore score

            val rounds = score.rounds.toMutableList()
            while (rounds.size <= roundIndex) {
                rounds.add(List(score.playerInitials.size) { null as Int? })
            }

            val bustRound = score.firstBustRound(playerIndex)
            val wasEmpty = rounds[roundIndex].getOrNull(playerIndex) == null
            var segmentStart = score.segmentStartRound.toMutableList()

            if (
                bustRound != null &&
                parsed != null &&
                wasEmpty &&
                roundIndex > bustRound
            ) {
                segmentStart[playerIndex] = roundIndex
            }

            val row = rounds[roundIndex].toMutableList()
            row[playerIndex] = parsed
            rounds[roundIndex] = row

            score.copy(
                rounds = rounds,
                segmentStartRound = segmentStart,
            ).ensureTrailingEmptyRound()
        }
    }

    fun resetGame() {
        autoSaveJob?.cancel()
        hasUnsavedChanges = false
        viewModelScope.launch {
            repository.clear()
            _uiState.update {
                it.copy(
                    score = ChinChonScore(),
                    saveStatus = SaveStatus.Idle,
                    showSetup = true,
                )
            }
        }
    }

    fun flushSave() {
        if (!hasUnsavedChanges) return
        autoSaveJob?.cancel()
        persistNow()
    }

    private fun updateScore(transform: (ChinChonScore) -> ChinChonScore) {
        val current = _uiState.value.score
        val updated = transform(current)
        if (updated == current) return
        markDirty(updated)
    }

    private fun updateScore(score: ChinChonScore) {
        if (score == _uiState.value.score) return
        markDirty(score)
    }

    private fun markDirty(score: ChinChonScore) {
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
