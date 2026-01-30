package com.example.paralleltimer.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TimerHistory(
    val id: String = UUID.randomUUID().toString(),
    val timerLabel: String,
    val groupId: String? = null,
    val colorIndex: Int,
    val durationMs: Long,
    val completedAtEpochMs: Long = System.currentTimeMillis()
)
