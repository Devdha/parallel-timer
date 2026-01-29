package com.example.paralleltimer

import android.app.Application
import com.example.paralleltimer.di.AppContainer

class ParallelTimerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
