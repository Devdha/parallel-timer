package com.example.paralleltimer.ui.viewmodel

sealed interface TimerAction {
    data class Start(val id: String) : TimerAction
    data class Pause(val id: String) : TimerAction
    data class Reset(val id: String) : TimerAction
    data class Delete(val id: String) : TimerAction
    data object UndoDelete : TimerAction
    data class Edit(val id: String, val label: String, val colorIndex: Int) : TimerAction
    data class CreateFromPreset(val durationMs: Long, val label: String = "") : TimerAction
    data class CreateCustom(val label: String, val colorIndex: Int, val durationMs: Long) : TimerAction
}
