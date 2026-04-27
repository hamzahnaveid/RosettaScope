package com.example.rosettascope.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class NotificationScheduler {

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleDaily(context: Context, hour: Int, minute: Int) {
        saveTime(context, hour, minute)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

    }

    fun scheduleNext(context: Context) {
        val prefs = context.getSharedPreferences("USER", Context.MODE_PRIVATE)
        val hour = prefs.getInt("notif_hour", 12)
        val minute = prefs.getInt("notif_minute", 0)
        scheduleDaily(context, hour, minute)
    }

    private fun saveTime(context: Context, hour: Int, minute: Int) {
        context.getSharedPreferences("USER", Context.MODE_PRIVATE)
            .edit()
            .putInt("notif_hour", hour)
            .putInt("notif_minute", minute)
            .apply()
    }
}