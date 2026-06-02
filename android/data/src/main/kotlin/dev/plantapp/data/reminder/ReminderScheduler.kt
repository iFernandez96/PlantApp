package dev.plantapp.data.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.plantapp.domain.reminder.ReminderSpec
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Enqueues one delayed WorkManager request per [ReminderSpec] to post a local notification at its
 *  trigger time. Local path only — no Firebase/FCM. Work is unique per task id, so re-scheduling
 *  replaces the prior request rather than duplicating it. */
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun schedule(specs: List<ReminderSpec>, now: Instant) {
        val workManager = WorkManager.getInstance(context)
        for (spec in specs) {
            val triggerMs = runCatching { Instant.parse(spec.triggerAtUtc).toEpochMilli() }.getOrNull()
                ?: continue
            val delayMs = (triggerMs - now.toEpochMilli()).coerceAtLeast(0L) // past trigger → fire asap
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        ReminderWorker.KEY_TITLE to "Plant care reminder",
                        ReminderWorker.KEY_TEXT to "A '${spec.kind}' task is due",
                        ReminderWorker.KEY_NOTIFICATION_ID to spec.taskId.hashCode(),
                    ),
                )
                .addTag(TAG_REMINDER)
                .build()
            workManager.enqueueUniqueWork("reminder-${spec.taskId}", ExistingWorkPolicy.REPLACE, request)
        }
    }

    companion object {
        const val TAG_REMINDER = "plant-reminder"
    }
}
