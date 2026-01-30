package com.example.paralleltimer.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.paralleltimer.domain.model.TimerItem

class TimerAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleAlarm(timer: TimerItem) {
        val endAtMs = timer.endAtEpochMs ?: return

        val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
            putExtra(TimerAlarmReceiver.EXTRA_TIMER_ID, timer.id)
            putExtra(TimerAlarmReceiver.EXTRA_TIMER_LABEL, timer.label)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timer.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                endAtMs,
                pendingIntent
            )
        } else {
            // Fallback for devices without exact alarm permission
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                endAtMs,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(timerId: String) {
        val intent = Intent(context, TimerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timerId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        NotificationHelper.cancelNotification(context, timerId)
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}
