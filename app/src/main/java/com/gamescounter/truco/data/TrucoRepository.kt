package com.gamescounter.truco.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.trucoDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "truco_scores",
)

class TrucoRepository(private val context: Context) {

    private val keyNosotros = intPreferencesKey("nosotros")
    private val keyEllos = intPreferencesKey("ellos")
    private val keyLastUpdated = longPreferencesKey("last_updated")

    suspend fun load(): TrucoScore? {
        val prefs = context.trucoDataStore.data.map { it }.first()
        if (!prefs.contains(keyNosotros) || !prefs.contains(keyEllos)) {
            return null
        }

        val lastUpdated = prefs[keyLastUpdated] ?: return null
        val ageMs = System.currentTimeMillis() - lastUpdated
        val maxAgeMs = TimeUnit.DAYS.toMillis(SAVE_RETENTION_DAYS)

        if (ageMs > maxAgeMs) {
            clear()
            return null
        }

        return TrucoScore(
            nosotros = prefs[keyNosotros] ?: 0,
            ellos = prefs[keyEllos] ?: 0,
            lastUpdatedEpochMs = lastUpdated,
        ).clamped()
    }

    suspend fun save(score: TrucoScore) {
        val updated = score.clamped().copy(lastUpdatedEpochMs = System.currentTimeMillis())
        context.trucoDataStore.edit { prefs ->
            prefs[keyNosotros] = updated.nosotros
            prefs[keyEllos] = updated.ellos
            prefs[keyLastUpdated] = updated.lastUpdatedEpochMs
        }
    }

    suspend fun clear() {
        context.trucoDataStore.edit { it.clear() }
    }
}
