package com.example.paralleltimer.ui.viewmodel

import com.example.paralleltimer.domain.model.TimerDisplayItem
import com.example.paralleltimer.domain.model.TimerGroup
import com.example.paralleltimer.domain.model.TimerPreset
import com.example.paralleltimer.domain.model.TimerStatistics

data class TimerUiState(
    val timers: List<TimerDisplayItem> = emptyList(),
    val presets: List<TimerPreset> = TimerPreset.defaults,
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null,
    val groups: List<TimerGroup> = emptyList(),
    val selectedGroupId: String? = null,  // null means "all"
    val statistics: TimerStatistics = TimerStatistics(),
    val showStatistics: Boolean = false
)
