package dev.plantapp.data.reminder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Posts a single local watering-reminder notification. All content comes via [inputData] — the
 *  worker is DI-free (no repository/Hilt). Permission-guarded: if POST_NOTIFICATIONS isn't granted
 *  on API 33+, it returns success without posting (no crash). */
class ReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Plant care reminder"
        val text = inputData.getString(KEY_TEXT) ?: "A care task is due"
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, title.hashCode())

        ensureReminderChannel(applicationContext)

        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return Result.success() // can't post without permission; not a failure

        val notification = NotificationCompat.Builder(applicationContext, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_TEXT = "text"
        const val KEY_NOTIFICATION_ID = "notification_id"
    }
}
