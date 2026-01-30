package com.example.paralleltimer.data.repository

import com.example.paralleltimer.domain.model.TimerItem
import com.example.paralleltimer.domain.model.TimerPreset
import com.example.paralleltimer.domain.model.TimerGroup
import com.example.paralleltimer.domain.model.TimerHistory
import com.example.paralleltimer.domain.model.TimerStatistics
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

    // Group operations
    val groups: Flow<List<TimerGroup>>
    suspend fun addGroup(group: TimerGroup)
    suspend fun updateGroup(group: TimerGroup)
    suspend fun deleteGroup(groupId: String)

    // History operations
    val history: Flow<List<TimerHistory>>
    suspend fun addHistoryEntry(entry: TimerHistory)
    suspend fun clearHistory()
    fun getStatistics(): Flow<TimerStatistics>
}
