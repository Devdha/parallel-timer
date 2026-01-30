package com.example.paralleltimer.domain.model

data class TimerStatistics(
    val totalCompletedCount: Int = 0,
    val totalTimeMs: Long = 0L,
    val todayCompletedCount: Int = 0,
    val todayTimeMs: Long = 0L,
    val weekCompletedCount: Int = 0,
    val weekTimeMs: Long = 0L,
    val monthCompletedCount: Int = 0,
    val monthTimeMs: Long = 0L,
    val groupStats: Map<String, GroupStatistics> = emptyMap(),
    val currentStreak: Int = 0,  // consecutive days with completed timers
    val longestStreak: Int = 0
)

data class GroupStatistics(
    val groupId: String,
    val completedCount: Int = 0,
    val totalTimeMs: Long = 0L
)
