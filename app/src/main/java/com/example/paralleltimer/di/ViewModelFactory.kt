package com.example.paralleltimer.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.paralleltimer.data.repository.TimerRepository
import com.example.paralleltimer.notification.TimerAlarmScheduler
import com.example.paralleltimer.ui.viewmodel.TimerListViewModel

class TimerViewModelFactory(
    private val repository: TimerRepository,
    private val alarmScheduler: TimerAlarmScheduler
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerListViewModel::class.java)) {
            return TimerListViewModel(repository, alarmScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
