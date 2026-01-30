package com.example.paralleltimer.data.repository

import com.example.paralleltimer.data.local.TimerDataStore
import com.example.paralleltimer.domain.model.TimerItem
import com.example.paralleltimer.domain.model.TimerPreset
import com.example.paralleltimer.domain.model.TimerGroup
import com.example.paralleltimer.domain.model.TimerHistory
import com.example.paralleltimer.domain.model.TimerStatistics
import com.example.paralleltimer.domain.model.GroupStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TimerRepositoryImpl(
    private val dataStore: TimerDataStore
) : TimerRepository {

    override val timers: Flow<List<TimerItem>> = dataStore.timersFlow
    override val presets: Flow<List<TimerPreset>> = dataStore.presetsFlow
    override val groups: Flow<List<TimerGroup>> = dataStore.groupsFlow
    override val history: Flow<List<TimerHistory>> = dataStore.historyFlow

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

    // Group operations
    override suspend fun addGroup(group: TimerGroup) {
        val currentGroups = groups.first()
        dataStore.saveGroups(currentGroups + group)
    }

    override suspend fun updateGroup(group: TimerGroup) {
        val currentGroups = groups.first()
        val updatedGroups = currentGroups.map {
            if (it.id == group.id) group else it
        }
        dataStore.saveGroups(updatedGroups)
    }

    override suspend fun deleteGroup(groupId: String) {
        val currentGroups = groups.first()
        dataStore.saveGroups(currentGroups.filter { it.id != groupId })
    }

    // History operations
    override suspend fun addHistoryEntry(entry: TimerHistory) {
        val currentHistory = history.first()
        dataStore.saveHistory(currentHistory + entry)
    }

    override suspend fun clearHistory() {
        dataStore.saveHistory(emptyList())
    }

    override fun getStatistics(): Flow<TimerStatistics> = history.map { historyEntries ->
        val now = Instant.now()
        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()

        // Convert history entries to local dates
        val entriesByDate = historyEntries.groupBy { entry ->
            Instant.ofEpochMilli(entry.completedAtEpochMs)
                .atZone(zoneId)
                .toLocalDate()
        }

        // Calculate time ranges
        val todayEntries = entriesByDate[today] ?: emptyList()
        val weekStart = today.minusDays(6)
        val weekEntries = historyEntries.filter {
            val date = Instant.ofEpochMilli(it.completedAtEpochMs)
                .atZone(zoneId)
                .toLocalDate()
            !date.isBefore(weekStart)
        }
        val monthStart = today.minusDays(29)
        val monthEntries = historyEntries.filter {
            val date = Instant.ofEpochMilli(it.completedAtEpochMs)
                .atZone(zoneId)
                .toLocalDate()
            !date.isBefore(monthStart)
        }

        // Calculate total time
        val totalTimeToday = todayEntries.sumOf { it.durationMs }
        val totalTimeWeek = weekEntries.sumOf { it.durationMs }
        val totalTimeMonth = monthEntries.sumOf { it.durationMs }

        // Calculate streak
        val sortedDates = entriesByDate.keys.sortedDescending()
        var currentStreak = 0
        var expectedDate = today
        for (date in sortedDates) {
            if (date == expectedDate) {
                currentStreak++
                expectedDate = expectedDate.minusDays(1)
            } else if (date.isBefore(expectedDate)) {
                break
            }
        }

        // Calculate group statistics
        val groupStatsMap = historyEntries
            .filter { it.groupId != null }
            .groupBy { it.groupId!! }
            .mapValues { (groupId, entries) ->
                GroupStatistics(
                    groupId = groupId,
                    completedCount = entries.size,
                    totalTimeMs = entries.sumOf { it.durationMs }
                )
            }

        TimerStatistics(
            totalCompletedCount = historyEntries.size,
            totalTimeMs = historyEntries.sumOf { it.durationMs },
            todayCompletedCount = todayEntries.size,
            todayTimeMs = totalTimeToday,
            weekCompletedCount = weekEntries.size,
            weekTimeMs = totalTimeWeek,
            monthCompletedCount = monthEntries.size,
            monthTimeMs = totalTimeMonth,
            groupStats = groupStatsMap,
            currentStreak = currentStreak
        )
    }
}
