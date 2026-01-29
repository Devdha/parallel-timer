package com.example.paralleltimer.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TimerState {
    Idle,     // Created but never started
    Running,  // Counting down
    Paused,   // Stopped mid-countdown
    Done      // Reached zero
}
