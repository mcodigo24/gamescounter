package com.gamescounter.truco.data

const val MIN_SCORE = 0
const val MAX_SCORE = 30
const val AUTO_SAVE_DELAY_MS = 30_000L
const val SAVE_RETENTION_DAYS = 1L

data class TrucoScore(
    val nosotros: Int = 0,
    val ellos: Int = 0,
    val lastUpdatedEpochMs: Long = 0L,
) {
    fun clamped(): TrucoScore = copy(
        nosotros = nosotros.coerceIn(MIN_SCORE, MAX_SCORE),
        ellos = ellos.coerceIn(MIN_SCORE, MAX_SCORE),
    )
}
