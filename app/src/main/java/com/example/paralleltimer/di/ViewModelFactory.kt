package com.example.paralleltimer.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.paralleltimer.data.repository.TimerRepository
import com.example.paralleltimer.ui.viewmodel.TimerListViewModel

class TimerViewModelFactory(
    private val repository: TimerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerListViewModel::class.java)) {
            return TimerListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
