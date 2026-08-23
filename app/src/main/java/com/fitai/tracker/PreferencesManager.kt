package com.fitai.tracker

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "fit_ai_prefs")

data class WeightRecord(val timestamp: Long, val weight: Double)
data class ScanRecord(val timestamp: Long, val result: String)

private object PrefsKeys {
    val WEIGHT_HISTORY = stringPreferencesKey("weight_history")
    val SCAN_HISTORY = stringPreferencesKey("scan_history")
    val BASE_TDEE = intPreferencesKey("base_tdee")
}

private const val RECORD_SEP = "\u0001"
private const val FIELD_SEP = "\u0002"

class PreferencesManager(private val context: Context) {

    val baseTdeeFlow: Flow<Int> = context.dataStore.data.map { it[PrefsKeys.BASE_TDEE] ?: 2450 }

    val weightHistoryFlow: Flow<List<WeightRecord>> = context.dataStore.data.map { prefs ->
        decodeWeightHistory(prefs[PrefsKeys.WEIGHT_HISTORY] ?: "")
    }

    val scanHistoryFlow: Flow<List<ScanRecord>> = context.dataStore.data.map { prefs ->
        decodeScanHistory(prefs[PrefsKeys.SCAN_HISTORY] ?: "")
    }

    suspend fun addWeightRecord(weight: Double) {
        val current = weightHistoryFlow.first().toMutableList()
        current.add(WeightRecord(System.currentTimeMillis(), weight))
        val trimmed = current.takeLast(60)
        val encoded = trimmed.joinToString(RECORD_SEP) { "${it.timestamp}$FIELD_SEP${it.weight}" }
        context.dataStore.edit { it[PrefsKeys.WEIGHT_HISTORY] = encoded }
    }

    suspend fun addScanRecord(result: String) {
        val current = scanHistoryFlow.first().toMutableList()
        current.add(ScanRecord(System.currentTimeMillis(), result))
        val trimmed = current.takeLast(30)
        val encoded = trimmed.joinToString(RECORD_SEP) { "${it.timestamp}$FIELD_SEP${it.result}" }
        context.dataStore.edit { it[PrefsKeys.SCAN_HISTORY] = encoded }
    }

    private fun decodeWeightHistory(raw: String): List<WeightRecord> {
        if (raw.isBlank()) return emptyList()
        return raw.split(RECORD_SEP).mapNotNull { entry ->
            val parts = entry.split(FIELD_SEP)
            if (parts.size == 2) {
                val ts = parts[0].toLongOrNull()
                val w = parts[1].toDoubleOrNull()
                if (ts != null && w != null) WeightRecord(ts, w) else null
            } else null
        }
    }

    private fun decodeScanHistory(raw: String): List<ScanRecord> {
        if (raw.isBlank()) return emptyList()
        return raw.split(RECORD_SEP).mapNotNull { entry ->
            val idx = entry.indexOf(FIELD_SEP)
            if (idx > 0) {
                val ts = entry.substring(0, idx).toLongOrNull()
                val result = entry.substring(idx + 1)
                if (ts != null) ScanRecord(ts, result) else null
            } else null
        }
    }
}
