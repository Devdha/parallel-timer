package com.example.paralleltimer.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TimerItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val colorIndex: Int = 0,
    val groupId: String? = null,
    val durationMs: Long,
    val state: TimerState = TimerState.Idle,
    val remainingMs: Long = durationMs,
    val endAtEpochMs: Long? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
