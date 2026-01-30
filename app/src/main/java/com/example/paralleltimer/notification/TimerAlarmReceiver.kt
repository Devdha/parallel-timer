package com.example.paralleltimer.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getStringExtra(EXTRA_TIMER_ID) ?: return
        val timerLabel = intent.getStringExtra(EXTRA_TIMER_LABEL) ?: ""

        NotificationHelper.showTimerCompleteNotification(
            context = context,
            timerId = timerId,
            timerLabel = timerLabel
        )
    }

    companion object {
        const val EXTRA_TIMER_ID = "timer_id"
        const val EXTRA_TIMER_LABEL = "timer_label"
    }
}
