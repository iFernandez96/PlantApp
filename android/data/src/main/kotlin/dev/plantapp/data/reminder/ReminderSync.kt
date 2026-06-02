package dev.plantapp.data.reminder

import dev.plantapp.domain.repository.InventoryRepository
import dev.plantapp.domain.reminder.computeReminders
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

/** Coordinates app-open reminder scheduling: gather the caller's pending CareTasks, run the pure
 *  [computeReminders] policy, and hand the specs to the [ReminderScheduling] seam. Local path only
 *  — no Firebase/FCM. Care timing (`dueAt`) is backend-computed (D-09); this only schedules local
 *  delivery (D-13). [clock] is injected so behaviour is testable/deterministic. */
class ReminderSync @Inject constructor(
    private val repository: InventoryRepository,
    private val scheduler: ReminderScheduling,
    private val clock: Clock,
) {
    suspend fun syncNow() {
        val now = Instant.now(clock)
        val tasks = repository.getPlants().flatMap { repository.getPlantTasks(it.id) }
        scheduler.schedule(computeReminders(tasks, now), now)
    }
}
