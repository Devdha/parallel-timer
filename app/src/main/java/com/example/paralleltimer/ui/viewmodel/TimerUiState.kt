package com.example.paralleltimer.ui.viewmodel

import com.example.paralleltimer.domain.model.TimerDisplayItem
import com.example.paralleltimer.domain.model.TimerPreset

data class TimerUiState(
    val timers: List<TimerDisplayItem> = emptyList(),
    val presets: List<TimerPreset> = TimerPreset.defaults,
    val isLoading: Boolean = true
)
