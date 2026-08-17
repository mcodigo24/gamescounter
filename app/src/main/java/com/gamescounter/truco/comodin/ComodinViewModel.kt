package com.gamescounter.truco.comodin

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

data class ComodinUiState(
    val score: ComodinScore = ComodinScore(),
    val saveStatus: SaveStatus = SaveStatus.Idle,
    val isLoading: Boolean = true,
    val showSetup: Boolean = false,
)

class ComodinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ComodinRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ComodinUiState())
    val uiState: StateFlow<ComodinUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null
    private var hasUnsavedChanges = false

    init {
        viewModelScope.launch {
            val saved = repository.load()
            _uiState.update {
                it.copy(
                    score = saved ?: ComodinScore(),
                    isLoading = false,
                    showSetup = saved == null,
                    saveStatus = if (saved != null) SaveStatus.Saved else SaveStatus.Idle,
                )
            }
        }
    }

    fun startGame(playerCount: Int, allowNegatives: Boolean, title: String) {
        val score = ComodinScore(
            allowNegatives = allowNegatives,
            title = title.trim().ifBlank { DEFAULT_COMODIN_TITLE },
        )
            .withPlayerCount(playerCount)
            .ensureTrailingEmptyRound()
        updateScore(score)
        _uiState.update { it.copy(showSetup = false) }
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

    fun updateTitle(value: String) {
        val clean = value.trim().take(40)
        updateScore { score ->
            score.copy(title = clean.ifBlank { DEFAULT_COMODIN_TITLE })
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
            if (!score.allowNegatives && parsed != null && parsed < 0) return@updateScore score
            if (!score.allowNegatives && trimmed == "-") return@updateScore score

            val rounds = score.rounds.toMutableList()
            while (rounds.size <= roundIndex) {
                rounds.add(List(score.playerInitials.size) { null as Int? })
            }

            val row = rounds[roundIndex].toMutableList()
            row[playerIndex] = parsed
            rounds[roundIndex] = row

            score.copy(rounds = rounds).ensureTrailingEmptyRound()
        }
    }

    fun addPlayer() {
        updateScore { score -> score.addPlayer() }
    }

    fun resetGame() {
        autoSaveJob?.cancel()
        hasUnsavedChanges = false
        viewModelScope.launch {
            repository.clear()
            _uiState.update {
                it.copy(
                    score = ComodinScore(),
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

    private fun updateScore(transform: (ComodinScore) -> ComodinScore) {
        val current = _uiState.value.score
        val updated = transform(current)
        if (updated == current) return
        markDirty(updated)
    }

    private fun updateScore(score: ComodinScore) {
        if (score == _uiState.value.score) return
        markDirty(score)
    }

    private fun markDirty(score: ComodinScore) {
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
