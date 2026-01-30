package com.example.paralleltimer.ui.viewmodel

import com.example.paralleltimer.domain.model.TimerGroup

sealed interface TimerAction {
    data class Start(val id: String) : TimerAction
    data class Pause(val id: String) : TimerAction
    data class Reset(val id: String) : TimerAction
    data class Delete(val id: String) : TimerAction
    data object UndoDelete : TimerAction
    data class Edit(val id: String, val label: String, val colorIndex: Int, val groupId: String? = null) : TimerAction
    data class CreateFromPreset(val durationMs: Long, val label: String = "") : TimerAction
    data class CreateCustom(val label: String, val colorIndex: Int, val durationMs: Long, val groupId: String? = null) : TimerAction
    data class SelectGroup(val groupId: String?) : TimerAction
    data class AddGroup(val group: TimerGroup) : TimerAction
    data class DeleteGroup(val groupId: String) : TimerAction
    data object ToggleStatistics : TimerAction
}
