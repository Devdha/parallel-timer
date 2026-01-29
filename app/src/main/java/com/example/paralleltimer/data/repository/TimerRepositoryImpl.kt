package com.example.paralleltimer.data.repository

import com.example.paralleltimer.data.local.TimerDataStore
import com.example.paralleltimer.domain.model.TimerItem
import com.example.paralleltimer.domain.model.TimerPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TimerRepositoryImpl(
    private val dataStore: TimerDataStore
) : TimerRepository {

    override val timers: Flow<List<TimerItem>> = dataStore.timersFlow
    override val presets: Flow<List<TimerPreset>> = dataStore.presetsFlow

    override suspend fun addTimer(timer: TimerItem) {
        val currentTimers = timers.first()
        dataStore.saveTimers(currentTimers + timer)
    }

    override suspend fun updateTimer(timer: TimerItem) {
        val currentTimers = timers.first()
        val updatedTimers = currentTimers.map {
            if (it.id == timer.id) timer else it
        }
        dataStore.saveTimers(updatedTimers)
    }

    override suspend fun deleteTimer(id: String) {
        val currentTimers = timers.first()
        dataStore.saveTimers(currentTimers.filter { it.id != id })
    }

    override suspend fun deleteAllTimers() {
        dataStore.saveTimers(emptyList())
    }

    override suspend fun addPreset(preset: TimerPreset) {
        val currentPresets = presets.first()
        dataStore.savePresets(currentPresets + preset)
    }

    override suspend fun deletePreset(preset: TimerPreset) {
        val currentPresets = presets.first()
        dataStore.savePresets(currentPresets.filter { it != preset })
    }
}
