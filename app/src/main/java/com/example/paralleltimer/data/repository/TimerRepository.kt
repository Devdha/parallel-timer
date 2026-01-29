package com.example.paralleltimer.data.repository

import com.example.paralleltimer.domain.model.TimerItem
import com.example.paralleltimer.domain.model.TimerPreset
import kotlinx.coroutines.flow.Flow

interface TimerRepository {
    val timers: Flow<List<TimerItem>>
    val presets: Flow<List<TimerPreset>>
    suspend fun addTimer(timer: TimerItem)
    suspend fun updateTimer(timer: TimerItem)
    suspend fun deleteTimer(id: String)
    suspend fun deleteAllTimers()
    suspend fun addPreset(preset: TimerPreset)
    suspend fun deletePreset(preset: TimerPreset)
}
