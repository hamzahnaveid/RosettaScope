package com.example.rosettascope.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.rosettascope.MainActivity
import com.example.rosettascope.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val targetLanguageCode = context.getSharedPreferences("USER", Context.MODE_PRIVATE)
            .getString("target_language", "").toString()

        val targetLanguage = when (targetLanguageCode) {
            "de-DE" -> "German"
            "fr-FR" -> "French"
            "es-ES" -> "Spanish"
            "vi-VN" -> "Vietnamese"
            "zh-CN" -> "Simplified Chinese"
            "ar-SA" -> "Arabic"
            "hi-IN" -> "Hindi"
            "ko-KR" -> "Korean"
            "ja-JP" -> "Japanese"
            "ru-RU" -> "Russian"
            "sv-SE" -> "Swedish"
            "fi-FI" -> "Finnish"
            "pl-PL" -> "Polish"
            "it-IT" -> "Italian"
            "nl-NL" -> "Dutch"
            else -> "Rosetta Scope"
        }

        val channelId = "daily_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.transparent_logo)
            .setContentTitle("Time to explore with $targetLanguage!")
            .setContentText("Explore your current surroundings in $targetLanguage with Rosetta Scope.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) { }
        NotificationManagerCompat.from(context).notify(1001, notification)

        NotificationScheduler().scheduleNext(context)
    }
}