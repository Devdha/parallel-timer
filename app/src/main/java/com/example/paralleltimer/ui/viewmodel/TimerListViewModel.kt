package com.example.paralleltimer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paralleltimer.data.repository.TimerRepository
import com.example.paralleltimer.domain.model.TimerDisplayItem
import com.example.paralleltimer.domain.model.TimerItem
import com.example.paralleltimer.domain.model.TimerPreset
import com.example.paralleltimer.domain.model.TimerState
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
    private var recentlyDeletedTimer: TimerItem? = null

    val uiState: StateFlow<TimerUiState> = combine(
        repository.timers,
        repository.presets,
        _tickTrigger,
        _isLoading,
        _snackbarMessage
    ) { timers, presets, now, isLoading, snackbarMessage ->
        val recalculatedTimers = recalculateRunningTimers(timers, now)
        val displayTimers = recalculatedTimers.map { timer ->
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
            isLoading = isLoading,
            snackbarMessage = snackbarMessage
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
                is TimerAction.Edit -> editTimer(action.id, action.label, action.colorIndex)
                is TimerAction.CreateFromPreset -> createFromPreset(action.durationMs, action.label)
                is TimerAction.CreateCustom -> createCustomTimer(action.label, action.colorIndex, action.durationMs)
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

    private suspend fun editTimer(id: String, label: String, colorIndex: Int) {
        val timer = getTimer(id) ?: return
        repository.updateTimer(
            timer.copy(
                label = label.take(20),
                colorIndex = colorIndex.coerceIn(0, 5)
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

    private suspend fun createCustomTimer(label: String, colorIndex: Int, durationMs: Long) {
        val timer = TimerItem(
            label = label.take(20),
            colorIndex = colorIndex.coerceIn(0, 5),
            durationMs = durationMs,
            remainingMs = durationMs
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
