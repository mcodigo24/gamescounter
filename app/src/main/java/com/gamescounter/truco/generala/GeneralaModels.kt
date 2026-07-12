package com.gamescounter.truco.generala

const val MAX_GENERALA_PLAYERS = 12
const val MIN_GENERALA_PLAYERS = 1

enum class GeneralaRow(val label: String, val allowedValues: List<Int>) {
    Ones("1", listOf(1, 2, 3, 4, 5)),
    Twos("2", listOf(2, 4, 6, 8, 10)),
    Threes("3", listOf(3, 6, 9, 12, 15)),
    Fours("4", listOf(4, 8, 12, 16, 20)),
    Fives("5", listOf(5, 10, 15, 20, 25)),
    Sixes("6", listOf(6, 12, 18, 24, 30)),
    Escalera("E", listOf(20, 25)),
    Full("F", listOf(30, 35)),
    Poker("P", listOf(40, 45)),
    Generala("G", listOf(50)),
    DobleGenerala("2G", listOf(100)),
}

data class GeneralaScore(
    val playerInitials: List<String> = listOf("A", "B", "C", "D"),
    val board: List<List<Int?>> = List(4) { List(GeneralaRow.entries.size) { null } },
    val lastUpdatedEpochMs: Long = 0L,
) {
    fun withPlayerCount(newCount: Int): GeneralaScore {
        val capped = newCount.coerceIn(MIN_GENERALA_PLAYERS, MAX_GENERALA_PLAYERS)
        val nextInitials = playerInitials
            .take(capped)
            .toMutableList()
            .apply {
                while (size < capped) {
                    add(defaultInitial(size))
                }
            }

        val nextBoard = board
            .take(capped)
            .toMutableList()
            .apply {
                while (size < capped) {
                    add(List(GeneralaRow.entries.size) { null })
                }
            }

        return copy(playerInitials = nextInitials, board = nextBoard)
    }

    fun totalForPlayer(playerIndex: Int): Int {
        val row = board.getOrNull(playerIndex) ?: return 0
        return row.sumOf { it ?: 0 }
    }
}

fun defaultInitial(index: Int): String {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    return alphabet[index % alphabet.length].toString()
}
