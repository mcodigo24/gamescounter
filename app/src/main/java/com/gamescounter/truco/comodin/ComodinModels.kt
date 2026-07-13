package com.gamescounter.truco.comodin

const val MAX_COMODIN_PLAYERS = 12
const val MIN_COMODIN_PLAYERS = 1
const val DEFAULT_COMODIN_PLAYERS = 2
const val DEFAULT_COMODIN_TITLE = "Comodín"

data class ComodinScore(
    val playerInitials: List<String> = emptyList(),
    val rounds: List<List<Int?>> = emptyList(),
    val allowNegatives: Boolean = false,
    val title: String = DEFAULT_COMODIN_TITLE,
    val isConfigured: Boolean = false,
    val lastUpdatedEpochMs: Long = 0L,
) {
    fun withPlayerCount(count: Int): ComodinScore {
        val capped = count.coerceIn(MIN_COMODIN_PLAYERS, MAX_COMODIN_PLAYERS)
        val initials = buildComodinInitials(capped)
        val normalizedRounds = normalizeComodinRounds(rounds, capped)

        return copy(
            playerInitials = initials,
            rounds = normalizedRounds,
            isConfigured = true,
        )
    }

    fun ensureTrailingEmptyRound(): ComodinScore {
        if (playerInitials.isEmpty()) return this
        if (rounds.isEmpty()) {
            return copy(rounds = listOf(emptyComodinRound(playerInitials.size)))
        }
        val last = rounds.last()
        if (last.all { it == null }) return this
        return copy(rounds = rounds + listOf(emptyComodinRound(playerInitials.size)))
    }

    fun totalForPlayer(playerIndex: Int): Int {
        if (playerIndex !in playerInitials.indices) return 0
        return rounds.sumOf { row -> row.getOrNull(playerIndex) ?: 0 }
    }

    fun addPlayer(): ComodinScore {
        if (playerInitials.size >= MAX_COMODIN_PLAYERS) return this

        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val newInitial = alphabet.map { it.toString() }
            .firstOrNull { it !in playerInitials }
            ?: alphabet[playerInitials.size % alphabet.length].toString()

        val newRounds = if (rounds.isEmpty()) {
            listOf(listOf(null as Int?))
        } else {
            rounds.map { row -> row + (null as Int?) }
        }

        return copy(
            playerInitials = playerInitials + newInitial,
            rounds = newRounds,
        ).ensureTrailingEmptyRound()
    }
}

fun buildComodinInitials(count: Int): List<String> {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    return List(count) { index -> alphabet[index % alphabet.length].toString() }
}

private fun emptyComodinRound(playerCount: Int): List<Int?> =
    List(playerCount) { null as Int? }

private fun normalizeComodinRounds(rounds: List<List<Int?>>, playerCount: Int): List<List<Int?>> {
    val normalized = rounds.map { row ->
        row.take(playerCount).toMutableList().apply {
            while (size < playerCount) add(null)
        }
    }.toMutableList<List<Int?>>()

    if (normalized.isEmpty()) {
        normalized.add(emptyComodinRound(playerCount))
    } else {
        val last = normalized.last()
        if (last.none { it == null }) {
            normalized.add(emptyComodinRound(playerCount))
        }
    }
    return normalized
}
