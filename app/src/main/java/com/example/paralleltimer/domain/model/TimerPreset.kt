package com.example.paralleltimer.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TimerPreset(
    val label: String,
    val durationMs: Long
) {
    companion object {
        val FIVE_MINUTES = TimerPreset("5 min", 5 * 60 * 1000L)
        val TEN_MINUTES = TimerPreset("10 min", 10 * 60 * 1000L)
        val TWENTY_FIVE_MINUTES = TimerPreset("25 min", 25 * 60 * 1000L)

        val defaults = listOf(FIVE_MINUTES, TEN_MINUTES, TWENTY_FIVE_MINUTES)
    }
}
