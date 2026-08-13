package com.shadow.shineyverse.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.shadow.shineyverse.MainActivity
import com.shadow.shineyverse.R

object GenerationNotifications {
    const val CHANNEL_ID = "generation_progress"
    const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Generation progress",
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    fun progress(context: Context, contentText: String): Notification =
        baseBuilder(context, contentText).setOngoing(true).setOnlyAlertOnce(true).build()

    fun done(context: Context, contentText: String): Notification =
        baseBuilder(context, contentText).setAutoCancel(true).build()

    private fun baseBuilder(context: Context, contentText: String): NotificationCompat.Builder {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("ShinyVerse")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
    }
}
