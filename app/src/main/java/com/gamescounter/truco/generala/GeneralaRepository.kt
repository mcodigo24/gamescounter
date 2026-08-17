package com.gamescounter.truco.generala

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gamescounter.truco.data.SAVE_RETENTION_DAYS
import com.gamescounter.truco.data.normalizePlayerName
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.generalaDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "generala_scores",
)

class GeneralaRepository(private val context: Context) {

    private val keyInitials = stringPreferencesKey("player_initials")
    private val keyBoard = stringPreferencesKey("board")
    private val keyLastUpdated = longPreferencesKey("last_updated")

    suspend fun load(): GeneralaScore? {
        val prefs = context.generalaDataStore.data.map { it }.first()
        val initialsRaw = prefs[keyInitials] ?: return null
        val boardRaw = prefs[keyBoard] ?: return null
        val lastUpdated = prefs[keyLastUpdated] ?: return null

        val ageMs = System.currentTimeMillis() - lastUpdated
        val maxAgeMs = TimeUnit.DAYS.toMillis(SAVE_RETENTION_DAYS)
        if (ageMs > maxAgeMs) {
            clear()
            return null
        }

        val initials = initialsRaw.split(",")
            .map { normalizePlayerName(it).ifBlank { "?" } }
            .take(MAX_GENERALA_PLAYERS)
            .ifEmpty { listOf("A", "B", "C", "D") }

        val playersRows = boardRaw.split("|")
            .take(MAX_GENERALA_PLAYERS)
            .map { playerRaw ->
                val parsed = playerRaw.split(";").map { token ->
                    when (token) {
                        "_" -> null
                        else -> token.toIntOrNull()
                    }
                }
                parsed.take(GeneralaRow.entries.size).toMutableList().apply {
                    while (size < GeneralaRow.entries.size) add(null)
                }.toList()
            }
            .ifEmpty { List(initials.size) { List(GeneralaRow.entries.size) { null } } }

        val board = playersRows.toMutableList().apply {
            while (size < initials.size) add(List(GeneralaRow.entries.size) { null })
        }

        return GeneralaScore(
            playerInitials = initials,
            board = board,
            lastUpdatedEpochMs = lastUpdated,
        ).withPlayerCount(initials.size)
    }

    suspend fun save(score: GeneralaScore) {
        val normalized = score.withPlayerCount(score.playerInitials.size)
        val initialsRaw = normalized.playerInitials.joinToString(",")
        val boardRaw = normalized.board.joinToString("|") { row ->
            row.joinToString(";") { value -> value?.toString() ?: "_" }
        }

        context.generalaDataStore.edit { prefs ->
            prefs[keyInitials] = initialsRaw
            prefs[keyBoard] = boardRaw
            prefs[keyLastUpdated] = System.currentTimeMillis()
        }
    }

    suspend fun clear() {
        context.generalaDataStore.edit { it.clear() }
    }
}
