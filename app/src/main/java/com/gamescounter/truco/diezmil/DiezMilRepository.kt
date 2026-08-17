package com.gamescounter.truco.diezmil

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

private val Context.diezMilDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "diezmil_scores",
)

class DiezMilRepository(private val context: Context) {

    private val keyInitials = stringPreferencesKey("player_initials")
    private val keyRounds = stringPreferencesKey("rounds")
    private val keyPlacement = stringPreferencesKey("placement_order")
    private val keyConfigured = stringPreferencesKey("is_configured")
    private val keyLastUpdated = longPreferencesKey("last_updated")

    suspend fun load(): DiezMilScore? {
        val prefs = context.diezMilDataStore.data.map { it }.first()
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
            ?.take(MAX_DIEZMIL_PLAYERS)
            ?: return null

        val rounds = parseRounds(prefs[keyRounds], initials.size)
        val placement = prefs[keyPlacement]
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull() }
            ?.filter { it in initials.indices }
            ?: emptyList()

        return DiezMilScore(
            playerInitials = initials,
            rounds = rounds,
            placementOrder = placement,
            isConfigured = true,
            lastUpdatedEpochMs = lastUpdated,
        ).ensureTrailingEmptyRound()
    }

    suspend fun save(score: DiezMilScore) {
        val normalized = score.ensureTrailingEmptyRound()
        context.diezMilDataStore.edit { prefs ->
            prefs[keyInitials] = normalized.playerInitials.joinToString(",")
            prefs[keyRounds] = serializeRounds(normalized.rounds)
            prefs[keyPlacement] = normalized.placementOrder.joinToString(",")
            prefs[keyConfigured] = if (normalized.isConfigured) "1" else "0"
            prefs[keyLastUpdated] = System.currentTimeMillis()
        }
    }

    suspend fun clear() {
        context.diezMilDataStore.edit { it.clear() }
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
