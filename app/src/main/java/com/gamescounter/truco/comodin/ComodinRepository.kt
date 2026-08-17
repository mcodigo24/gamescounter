package com.gamescounter.truco.comodin

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

private val Context.comodinDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "comodin_scores",
)

class ComodinRepository(private val context: Context) {

    private val keyInitials = stringPreferencesKey("player_initials")
    private val keyRounds = stringPreferencesKey("rounds")
    private val keyAllowNegatives = stringPreferencesKey("allow_negatives")
    private val keyTitle = stringPreferencesKey("title")
    private val keyConfigured = stringPreferencesKey("is_configured")
    private val keyLastUpdated = longPreferencesKey("last_updated")

    suspend fun load(): ComodinScore? {
        val prefs = context.comodinDataStore.data.map { it }.first()
        val configured = prefs[keyConfigured] == "1"
        if (!configured) return null

        val lastUpdated = prefs[keyLastUpdated] ?: return null
        val ageMs = System.currentTimeMillis() - lastUpdated
        if (ageMs > TimeUnit.DAYS.toMillis(SAVE_RETENTION_DAYS)) {
            clear()
            return null
        }

        val initials = prefs[keyInitials]
            ?.split(",")
            ?.map { normalizePlayerName(it).ifBlank { "?" } }
            ?.take(MAX_COMODIN_PLAYERS)
            ?: return null

        val rounds = parseRounds(prefs[keyRounds], initials.size)
        val allowNegatives = prefs[keyAllowNegatives] == "1"
        val title = prefs[keyTitle]?.ifBlank { DEFAULT_COMODIN_TITLE } ?: DEFAULT_COMODIN_TITLE

        return ComodinScore(
            playerInitials = initials,
            rounds = rounds,
            allowNegatives = allowNegatives,
            title = title,
            isConfigured = true,
            lastUpdatedEpochMs = lastUpdated,
        ).ensureTrailingEmptyRound()
    }

    suspend fun save(score: ComodinScore) {
        val normalized = score.ensureTrailingEmptyRound()
        context.comodinDataStore.edit { prefs ->
            prefs[keyInitials] = normalized.playerInitials.joinToString(",")
            prefs[keyRounds] = serializeRounds(normalized.rounds)
            prefs[keyAllowNegatives] = if (normalized.allowNegatives) "1" else "0"
            prefs[keyTitle] = normalized.title
            prefs[keyConfigured] = if (normalized.isConfigured) "1" else "0"
            prefs[keyLastUpdated] = System.currentTimeMillis()
        }
    }

    suspend fun clear() {
        context.comodinDataStore.edit { it.clear() }
    }

    private fun parseRounds(raw: String?, playerCount: Int): List<List<Int?>> {
        if (raw.isNullOrBlank()) return listOf(List(playerCount) { null as Int? })
        return raw.split("||").map { roundRaw ->
            roundRaw.split(";").map { token ->
                when (token) {
                    "_" -> null
                    else -> token.toIntOrNull()
                }
            }.take(playerCount).toMutableList().apply {
                while (size < playerCount) add(null)
            }
        }
    }

    private fun serializeRounds(rounds: List<List<Int?>>): String =
        rounds.joinToString("||") { row ->
            row.joinToString(";") { value -> value?.toString() ?: "_" }
        }
}
