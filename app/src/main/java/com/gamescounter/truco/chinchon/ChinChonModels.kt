package com.gamescounter.truco.chinchon

const val CHINCHON_LOSE_THRESHOLD = 100
const val MAX_CHINCHON_PLAYERS = 12
const val MIN_CHINCHON_PLAYERS = 2
const val DEFAULT_CHINCHON_PLAYERS = 4

data class ChinChonScore(
    val playerInitials: List<String> = emptyList(),
    val rounds: List<List<Int?>> = emptyList(),
    val segmentStartRound: List<Int> = emptyList(),
    val isConfigured: Boolean = false,
    val lastUpdatedEpochMs: Long = 0L,
) {
    fun withPlayerCount(count: Int): ChinChonScore {
        val capped = count.coerceIn(MIN_CHINCHON_PLAYERS, MAX_CHINCHON_PLAYERS)
        val initials = buildInitials(capped)
        val normalizedRounds = normalizeRounds(rounds, capped)
        val starts = segmentStartRound
            .take(capped)
            .toMutableList()
            .apply { while (size < capped) add(0) }

        return copy(
            playerInitials = initials,
            rounds = normalizedRounds,
            segmentStartRound = starts,
            isConfigured = true,
        )
    }

    fun ensureTrailingEmptyRound(): ChinChonScore {
        if (playerInitials.isEmpty()) return this
        if (rounds.isEmpty()) {
            return copy(rounds = listOf(emptyRound(playerInitials.size)))
        }
        val last = rounds.last()
        if (last.all { it == null }) return this
        return copy(rounds = rounds + listOf(emptyRound(playerInitials.size)))
    }

    fun totalForPlayer(playerIndex: Int): Int {
        if (playerIndex !in playerInitials.indices) return 0
        val start = segmentStartRound.getOrElse(playerIndex) { 0 }
        return rounds.drop(start).sumOf { row ->
            row.getOrNull(playerIndex) ?: 0
        }
    }

    fun isPlayerLost(playerIndex: Int): Boolean =
        totalForPlayer(playerIndex) >= CHINCHON_LOSE_THRESHOLD

    fun firstBustRound(playerIndex: Int): Int? {
        if (playerIndex !in playerInitials.indices) return null
        val start = segmentStartRound.getOrElse(playerIndex) { 0 }
        var running = 0
        rounds.forEachIndexed { index, row ->
            if (index < start) return@forEachIndexed
            val value = row.getOrNull(playerIndex) ?: return@forEachIndexed
            running += value
            if (running >= CHINCHON_LOSE_THRESHOLD) return index
        }
        return null
    }
}

fun buildInitials(count: Int): List<String> {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    return List(count) { index -> alphabet[index % alphabet.length].toString() }
}

private fun emptyRound(playerCount: Int): List<Int?> =
    List(playerCount) { null as Int? }

private fun normalizeRounds(rounds: List<List<Int?>>, playerCount: Int): List<List<Int?>> {
    val normalized = rounds.map { row ->
        row.take(playerCount).toMutableList().apply {
            while (size < playerCount) add(null)
        }
    }.toMutableList<List<Int?>>()

    if (normalized.isEmpty()) {
        normalized.add(emptyRound(playerCount))
    } else {
        val last = normalized.last()
        if (last.none { it == null }) {
            normalized.add(emptyRound(playerCount))
        }
    }
    return normalized
}
