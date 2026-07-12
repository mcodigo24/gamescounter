package com.gamescounter.truco.chinchon

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gamescounter.truco.data.SAVE_RETENTION_DAYS
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.chinChonDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "chinchon_scores",
)

class ChinChonRepository(private val context: Context) {

    private val keyInitials = stringPreferencesKey("player_initials")
    private val keyRounds = stringPreferencesKey("rounds")
    private val keySegmentStart = stringPreferencesKey("segment_start")
    private val keyConfigured = stringPreferencesKey("is_configured")
    private val keyLastUpdated = longPreferencesKey("last_updated")

    suspend fun load(): ChinChonScore? {
        val prefs = context.chinChonDataStore.data.map { it }.first()
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
            ?.map { it.trim().ifBlank { "?" }.take(1).uppercase() }
            ?.take(MAX_CHINCHON_PLAYERS)
            ?: return null

        val rounds = parseRounds(prefs[keyRounds], initials.size)
        val segmentStart = prefs[keySegmentStart]
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull() }
            ?.take(initials.size)
            ?: List(initials.size) { 0 }

        return ChinChonScore(
            playerInitials = initials,
            rounds = rounds,
            segmentStartRound = segmentStart,
            isConfigured = true,
            lastUpdatedEpochMs = lastUpdated,
        ).ensureTrailingEmptyRound()
    }

    suspend fun save(score: ChinChonScore) {
        val normalized = score.ensureTrailingEmptyRound()
        context.chinChonDataStore.edit { prefs ->
            prefs[keyInitials] = normalized.playerInitials.joinToString(",")
            prefs[keyRounds] = serializeRounds(normalized.rounds)
            prefs[keySegmentStart] = normalized.segmentStartRound.joinToString(",")
            prefs[keyConfigured] = if (normalized.isConfigured) "1" else "0"
            prefs[keyLastUpdated] = System.currentTimeMillis()
        }
    }

    suspend fun clear() {
        context.chinChonDataStore.edit { it.clear() }
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
