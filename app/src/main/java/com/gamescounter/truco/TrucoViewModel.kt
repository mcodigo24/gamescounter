package com.gamescounter.truco

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamescounter.truco.data.AUTO_SAVE_DELAY_MS
import com.gamescounter.truco.data.MAX_SCORE
import com.gamescounter.truco.data.MIN_SCORE
import com.gamescounter.truco.data.TrucoRepository
import com.gamescounter.truco.data.TrucoScore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrucoUiState(
    val score: TrucoScore = TrucoScore(),
    val saveStatus: SaveStatus = SaveStatus.Idle,
    val isLoading: Boolean = true,
)

class TrucoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TrucoRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(TrucoUiState())
    val uiState: StateFlow<TrucoUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null
    private var hasUnsavedChanges = false

    init {
        viewModelScope.launch {
            val saved = repository.load()
            _uiState.update {
                it.copy(
                    score = saved ?: TrucoScore(),
                    isLoading = false,
                    saveStatus = if (saved != null) SaveStatus.Saved else SaveStatus.Idle,
                )
            }
        }
    }

    fun incrementNosotros() = adjustScore { it.copy(nosotros = it.nosotros + 1) }

    fun decrementNosotros() = adjustScore { it.copy(nosotros = it.nosotros - 1) }

    fun incrementEllos() = adjustScore { it.copy(ellos = it.ellos + 1) }

    fun decrementEllos() = adjustScore { it.copy(ellos = it.ellos - 1) }

    fun resetGame() {
        autoSaveJob?.cancel()
        hasUnsavedChanges = false
        viewModelScope.launch {
            repository.clear()
            _uiState.update {
                it.copy(
                    score = TrucoScore(),
                    saveStatus = SaveStatus.Idle,
                )
            }
        }
    }

    fun flushSave() {
        if (!hasUnsavedChanges) return
        autoSaveJob?.cancel()
        persistNow()
    }

    private fun adjustScore(transform: (TrucoScore) -> TrucoScore) {
        val current = _uiState.value.score
        val updated = transform(current).clamped()

        if (updated.nosotros == current.nosotros && updated.ellos == current.ellos) {
            return
        }

        hasUnsavedChanges = true
        _uiState.update {
            it.copy(
                score = updated,
                saveStatus = SaveStatus.Pending,
            )
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

    private fun TrucoScore.clamped(): TrucoScore = copy(
        nosotros = nosotros.coerceIn(MIN_SCORE, MAX_SCORE),
        ellos = ellos.coerceIn(MIN_SCORE, MAX_SCORE),
    )
}
