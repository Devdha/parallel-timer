package com.example.paralleltimer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paralleltimer.data.repository.TimerRepository
import com.example.paralleltimer.domain.model.DefaultGroups
import com.example.paralleltimer.domain.model.TimerDisplayItem
import com.example.paralleltimer.domain.model.TimerGroup
import com.example.paralleltimer.domain.model.TimerHistory
import com.example.paralleltimer.domain.model.TimerItem
import com.example.paralleltimer.domain.model.TimerPreset
import com.example.paralleltimer.domain.model.TimerState
import com.example.paralleltimer.domain.model.TimerStatistics
import com.example.paralleltimer.notification.TimerAlarmScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimerListViewModel(
    private val repository: TimerRepository,
    private val alarmScheduler: TimerAlarmScheduler
) : ViewModel() {

    private val _tickTrigger = MutableStateFlow(System.currentTimeMillis())
    private val _isLoading = MutableStateFlow(true)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _selectedGroupId = MutableStateFlow<String?>(null)
    private val _showStatistics = MutableStateFlow(false)
    private var recentlyDeletedTimer: TimerItem? = null

    val uiState: StateFlow<TimerUiState> = combine(
        repository.timers,
        repository.presets,
        repository.groups,
        repository.getStatistics(),
        _tickTrigger,
        _selectedGroupId,
        _showStatistics
    ) { flows ->
        val timers = flows[0] as List<TimerItem>
        val presets = flows[1] as List<TimerPreset>
        val groups = flows[2] as List<TimerGroup>
        val statistics = flows[3] as TimerStatistics
        val now = flows[4] as Long
        val selectedGroupId = flows[5] as String?
        val showStatistics = flows[6] as Boolean

        val recalculatedTimers = recalculateRunningTimers(timers, now)

        // Filter timers based on selected group
        val filteredTimers = if (selectedGroupId == null) {
            recalculatedTimers
        } else {
            recalculatedTimers.filter { it.groupId == selectedGroupId }
        }

        val displayTimers = filteredTimers.map { timer ->
            val displayRemaining = when (timer.state) {
                TimerState.Running -> {
                    ((timer.endAtEpochMs ?: now) - now).coerceAtLeast(0)
                }
                else -> timer.remainingMs
            }
            TimerDisplayItem(timer, displayRemaining)
        }
        TimerUiState(
            timers = displayTimers,
            presets = presets,
            isLoading = _isLoading.value,
            snackbarMessage = _snackbarMessage.value,
            groups = groups,
            selectedGroupId = selectedGroupId,
            statistics = statistics,
            showStatistics = showStatistics
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimerUiState())

    init {
        // Start UI tick loop (100ms refresh with second-boundary alignment)
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                // Calculate delay until next 100ms boundary
                val nextTick = ((now / 100) + 1) * 100
                val delayMs = nextTick - now
                delay(delayMs)

                _tickTrigger.value = System.currentTimeMillis()
                checkAndUpdateCompletedTimers()
            }
        }

        // Initial load and create default timer if empty
        viewModelScope.launch {
            val timers = repository.timers.first()
            if (timers.isEmpty()) {
                // Create default timer on first launch
                val defaultTimer = TimerItem(
                    durationMs = 5 * 60 * 1000L,
                    label = "Parallel Timer",
                    colorIndex = 4 // Blue
                )
                repository.addTimer(defaultTimer)
            }
            _isLoading.value = false
        }
    }

    private suspend fun checkAndUpdateCompletedTimers() {
        val now = System.currentTimeMillis()
        val timers = repository.timers.first()
        timers.filter { it.state == TimerState.Running }.forEach { timer ->
            val endAt = timer.endAtEpochMs ?: return@forEach
            if (now >= endAt) {
                repository.updateTimer(
                    timer.copy(
                        state = TimerState.Done,
                        remainingMs = 0,
                        endAtEpochMs = null
                    )
                )
                // Record to history
                repository.addHistoryEntry(
                    TimerHistory(
                        timerLabel = timer.label,
                        groupId = timer.groupId,
                        colorIndex = timer.colorIndex,
                        durationMs = timer.durationMs
                    )
                )
            }
        }
    }

    private fun recalculateRunningTimers(timers: List<TimerItem>, now: Long): List<TimerItem> {
        return timers.map { timer ->
            when (timer.state) {
                TimerState.Running -> {
                    val endAt = timer.endAtEpochMs ?: return@map timer
                    val remaining = (endAt - now).coerceAtLeast(0)
                    if (remaining == 0L) {
                        timer.copy(state = TimerState.Done, remainingMs = 0)
                    } else {
                        timer.copy(remainingMs = remaining)
                    }
                }
                else -> timer
            }
        }
    }

    fun onAction(action: TimerAction) {
        viewModelScope.launch {
            when (action) {
                is TimerAction.Start -> startTimer(action.id)
                is TimerAction.Pause -> pauseTimer(action.id)
                is TimerAction.Reset -> resetTimer(action.id)
                is TimerAction.Delete -> deleteTimer(action.id)
                is TimerAction.UndoDelete -> undoDelete()
                is TimerAction.Edit -> editTimer(action.id, action.label, action.colorIndex, action.groupId)
                is TimerAction.CreateFromPreset -> createFromPreset(action.durationMs, action.label)
                is TimerAction.CreateCustom -> createCustomTimer(action.label, action.colorIndex, action.durationMs, action.groupId)
                is TimerAction.SelectGroup -> _selectedGroupId.value = action.groupId
                is TimerAction.AddGroup -> repository.addGroup(action.group)
                is TimerAction.DeleteGroup -> repository.deleteGroup(action.groupId)
                is TimerAction.ToggleStatistics -> _showStatistics.value = !_showStatistics.value
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun addPreset(label: String, durationMs: Long) {
        viewModelScope.launch {
            repository.addPreset(TimerPreset(label, durationMs))
        }
    }

    fun deletePreset(preset: TimerPreset) {
        viewModelScope.launch {
            repository.deletePreset(preset)
        }
    }

    private suspend fun startTimer(id: String) {
        val timer = getTimer(id) ?: return
        val now = System.currentTimeMillis()
        val endAt = now + timer.remainingMs
        val updatedTimer = timer.copy(
            state = TimerState.Running,
            endAtEpochMs = endAt
        )
        repository.updateTimer(updatedTimer)
        alarmScheduler.scheduleAlarm(updatedTimer)
    }

    private suspend fun pauseTimer(id: String) {
        val timer = getTimer(id) ?: return
        val now = System.currentTimeMillis()
        val remaining = ((timer.endAtEpochMs ?: now) - now).coerceAtLeast(0)
        repository.updateTimer(
            timer.copy(
                state = TimerState.Paused,
                remainingMs = remaining,
                endAtEpochMs = null
            )
        )
        alarmScheduler.cancelAlarm(id)
    }

    private suspend fun resetTimer(id: String) {
        val timer = getTimer(id) ?: return
        repository.updateTimer(
            timer.copy(
                state = TimerState.Idle,
                remainingMs = timer.durationMs,
                endAtEpochMs = null
            )
        )
        alarmScheduler.cancelAlarm(id)
    }

    private suspend fun deleteTimer(id: String) {
        val timer = getTimer(id) ?: return
        recentlyDeletedTimer = timer
        alarmScheduler.cancelAlarm(id)
        repository.deleteTimer(id)
        // Just pass the label - UI will format the message with localized string
        _snackbarMessage.value = timer.label
    }

    private suspend fun undoDelete() {
        recentlyDeletedTimer?.let { timer ->
            // Restore as Paused if it was Running, so user can manually resume
            val restoredTimer = if (timer.state == TimerState.Running) {
                timer.copy(
                    state = TimerState.Paused,
                    endAtEpochMs = null
                )
            } else {
                timer
            }
            repository.addTimer(restoredTimer)
            recentlyDeletedTimer = null
            _snackbarMessage.value = null
        }
    }

    private suspend fun editTimer(id: String, label: String, colorIndex: Int, groupId: String? = null) {
        val timer = getTimer(id) ?: return
        repository.updateTimer(
            timer.copy(
                label = label.take(20),
                colorIndex = colorIndex.coerceIn(0, 5),
                groupId = groupId
            )
        )
    }

    private suspend fun createFromPreset(durationMs: Long, label: String) {
        val timer = TimerItem(
            durationMs = durationMs,
            label = label.ifEmpty { formatDuration(durationMs) },
            colorIndex = (0..5).random()
        )
        repository.addTimer(timer)
    }

    private suspend fun createCustomTimer(label: String, colorIndex: Int, durationMs: Long, groupId: String? = null) {
        val timer = TimerItem(
            label = label.take(20),
            colorIndex = colorIndex.coerceIn(0, 5),
            durationMs = durationMs,
            remainingMs = durationMs,
            groupId = groupId
        )
        repository.addTimer(timer)
    }

    private suspend fun getTimer(id: String): TimerItem? {
        return repository.timers.first().find { it.id == id }
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (seconds == 0L) "${minutes}m" else "${minutes}m ${seconds}s"
    }
}
