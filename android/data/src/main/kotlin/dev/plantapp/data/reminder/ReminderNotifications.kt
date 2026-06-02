package dev.plantapp.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/** The notification channel for local watering reminders. */
const val REMINDER_CHANNEL_ID = "plant_care_reminders"

/** Idempotently create the reminder notification channel (no-op below API 26, and re-creating an
 *  existing channel id is a no-op on the platform). */
fun ensureReminderChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(NotificationManager::class.java) ?: return
    val channel = NotificationChannel(
        REMINDER_CHANNEL_ID,
        "Plant care reminders",
        NotificationManager.IMPORTANCE_DEFAULT,
    )
    manager.createNotificationChannel(channel)
}
