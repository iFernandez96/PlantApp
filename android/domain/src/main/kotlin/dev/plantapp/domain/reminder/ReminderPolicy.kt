package dev.plantapp.domain.reminder

import dev.plantapp.domain.model.CareTask
import java.time.Duration
import java.time.Instant

/** A decision to fire a local reminder for one CareTask. */
data class ReminderSpec(
    val taskId: String,
    val kind: String,
    val dueAt: String, // ISO-8601 UTC, echoed from the task
    val triggerAtUtc: String, // when the local reminder should fire (ISO-8601 UTC)
)

/**
 * Pure, deterministic reminder-scheduling policy. This is NOT care computation — `dueAt` is
 * produced by the backend care engine (D-09: Android computes no care logic). This only decides
 * *local reminder timing* (D-13).
 *
 * Given the caller's CareTasks and the current instant, returns one [ReminderSpec] per task that:
 *  - has status == "pending", and
 *  - is not more than [staleAfter] past due (so we don't remind about long-abandoned tasks).
 *
 * `triggerAtUtc = dueAt - [leadTime]` (default zero → remind at due time). The trigger may be in
 * the past (due-soon task with a lead time, or a just-past task inside the stale window); that is
 * intentional — the WorkManager step treats a past trigger as "fire immediately".
 *
 * Deterministic: same inputs → identical output (no `Instant.now()` inside; [now] is passed in).
 * Output order follows input order. Tasks with an unparseable `dueAt` are skipped (defensive).
 */
fun computeReminders(
    tasks: List<CareTask>,
    now: Instant,
    leadTime: Duration = Duration.ZERO,
    staleAfter: Duration = Duration.ofDays(7),
): List<ReminderSpec> {
    val staleBefore = now.minus(staleAfter)
    return tasks.mapNotNull { task ->
        if (task.status != "pending") return@mapNotNull null
        val due = runCatching { Instant.parse(task.dueAt) }.getOrNull() ?: return@mapNotNull null
        if (due.isBefore(staleBefore)) return@mapNotNull null
        ReminderSpec(
            taskId = task.id,
            kind = task.kind,
            dueAt = task.dueAt,
            triggerAtUtc = due.minus(leadTime).toString(),
        )
    }
}
