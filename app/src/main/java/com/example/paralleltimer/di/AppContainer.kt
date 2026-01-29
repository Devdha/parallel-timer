package com.example.paralleltimer.di

import android.content.Context
import com.example.paralleltimer.data.local.TimerDataStore
import com.example.paralleltimer.data.repository.TimerRepository
import com.example.paralleltimer.data.repository.TimerRepositoryImpl

class AppContainer(context: Context) {
    private val timerDataStore: TimerDataStore by lazy { TimerDataStore(context) }
    val timerRepository: TimerRepository by lazy { TimerRepositoryImpl(timerDataStore) }
}
