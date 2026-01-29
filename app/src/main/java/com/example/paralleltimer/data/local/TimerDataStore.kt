package com.example.paralleltimer.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.paralleltimer.domain.model.TimerItem
import com.example.paralleltimer.domain.model.TimerPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "timers")

class TimerDataStore(private val context: Context) {
    private val TIMERS_KEY = stringPreferencesKey("timers_json")
    private val PRESETS_KEY = stringPreferencesKey("presets_json")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val timersFlow: Flow<List<TimerItem>> = context.dataStore.data
        .map { prefs ->
            val jsonStr = prefs[TIMERS_KEY] ?: "[]"
            TimerSerializer.deserialize(jsonStr)
        }

    val presetsFlow: Flow<List<TimerPreset>> = context.dataStore.data
        .map { prefs ->
            val jsonStr = prefs[PRESETS_KEY]
            if (jsonStr == null) {
                TimerPreset.defaults
            } else {
                try {
                    json.decodeFromString<List<TimerPreset>>(jsonStr)
                } catch (e: Exception) {
                    TimerPreset.defaults
                }
            }
        }

    suspend fun saveTimers(timers: List<TimerItem>) {
        context.dataStore.edit { prefs ->
            prefs[TIMERS_KEY] = TimerSerializer.serialize(timers)
        }
    }

    suspend fun savePresets(presets: List<TimerPreset>) {
        context.dataStore.edit { prefs ->
            prefs[PRESETS_KEY] = json.encodeToString(presets)
        }
    }
}
