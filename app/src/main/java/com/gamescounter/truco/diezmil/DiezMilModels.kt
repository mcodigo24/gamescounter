package com.gamescounter.truco.diezmil

const val DIEZMIL_TARGET = 10_000
const val DIEZMIL_ENTRY_THRESHOLD = 750
const val MAX_DIEZMIL_PLAYERS = 12
const val MIN_DIEZMIL_PLAYERS = 2
const val DEFAULT_DIEZMIL_PLAYERS = 4

data class DiezMilScore(
    val playerInitials: List<String> = emptyList(),
    val rounds: List<List<Int?>> = emptyList(),
    val placementOrder: List<Int> = emptyList(),
    val isConfigured: Boolean = false,
    val lastUpdatedEpochMs: Long = 0L,
) {
    fun withPlayerCount(count: Int): DiezMilScore {
        val capped = count.coerceIn(MIN_DIEZMIL_PLAYERS, MAX_DIEZMIL_PLAYERS)
        val initials = playerInitials
            .take(capped)
            .toMutableList()
            .apply {
                while (size < capped) {
                    add(buildDiezMilInitials(capped)[size])
                }
            }
            .ifEmpty { buildDiezMilInitials(capped) }

        return copy(
            playerInitials = initials,
            rounds = normalizeDiezMilRounds(rounds, capped),
            isConfigured = true,
        )
    }

    fun ensureTrailingEmptyRound(): DiezMilScore {
        if (playerInitials.isEmpty()) return this
        if (rounds.isEmpty()) {
            return copy(rounds = listOf(emptyRound(playerInitials.size)))
        }
        val last = rounds.last()
        if (last.all { it == null }) return this
        return copy(rounds = rounds + listOf(emptyRound(playerInitials.size)))
    }

    fun hasStarted(playerIndex: Int): Boolean {
        val firstValue = rounds.firstOrNull()?.getOrNull(playerIndex) ?: return false
        return firstValue >= DIEZMIL_ENTRY_THRESHOLD
    }

    fun totalForPlayer(playerIndex: Int): Int {
        if (playerIndex !in playerInitials.indices) return 0
        if (!hasStarted(playerIndex)) return 0
        return rounds.sumOf { row -> row.getOrNull(playerIndex) ?: 0 }
    }

    fun hasWon(playerIndex: Int): Boolean =
        hasStarted(playerIndex) && totalForPlayer(playerIndex) == DIEZMIL_TARGET

    fun remainingForPlayer(playerIndex: Int): Int =
        (DIEZMIL_TARGET - totalForPlayer(playerIndex)).coerceAtLeast(0)

    fun placementForPlayer(playerIndex: Int): Int? {
        val index = placementOrder.indexOf(playerIndex)
        return if (index >= 0) index + 1 else null
    }

    fun wouldExceedTarget(playerIndex: Int, roundIndex: Int, value: Int?): Boolean {
        if (value == null || value < 0) return false
        val hypothetical = rounds.toMutableList()
        while (hypothetical.size <= roundIndex) {
            hypothetical.add(emptyRound(playerInitials.size))
        }
        val row = hypothetical[roundIndex].toMutableList()
        while (row.size <= playerIndex) row.add(null)
        row[playerIndex] = value
        hypothetical[roundIndex] = row

        val temp = copy(rounds = hypothetical)
        return temp.hasStarted(playerIndex) && temp.totalForPlayer(playerIndex) > DIEZMIL_TARGET
    }
}

fun buildDiezMilInitials(count: Int): List<String> {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    return List(count) { index -> alphabet[index % alphabet.length].toString() }
}

private fun emptyRound(playerCount: Int): List<Int?> =
    List(playerCount) { null as Int? }

private fun normalizeDiezMilRounds(rounds: List<List<Int?>>, playerCount: Int): List<List<Int?>> {
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
