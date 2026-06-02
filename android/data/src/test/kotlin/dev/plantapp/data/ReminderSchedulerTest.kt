package dev.plantapp.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import dev.plantapp.data.reminder.ReminderScheduler
import dev.plantapp.data.reminder.ReminderScheduler.Companion.TAG_REMINDER
import dev.plantapp.domain.reminder.ReminderSpec
import java.time.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Verifies the local-reminder scheduling behaviour (enqueue + unique-per-task). The actual
 *  notification post needs a device/permission and is out of scope for this unit gate. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReminderSchedulerTest {
    private lateinit var context: Context
    private lateinit var scheduler: ReminderScheduler

    private val now = Instant.parse("2026-06-02T00:00:00.000Z")

    private fun spec(taskId: String, triggerAtUtc: String) =
        ReminderSpec(taskId = taskId, kind = "water", dueAt = triggerAtUtc, triggerAtUtc = triggerAtUtc)

    @BeforeTest
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        scheduler = ReminderScheduler(context)
    }

    private fun reminders(): List<WorkInfo> =
        WorkManager.getInstance(context).getWorkInfosByTag(TAG_REMINDER).get()

    @Test
    fun scheduleEnqueuesOneUniqueWorkPerPendingSpec() {
        scheduler.schedule(
            listOf(
                spec("t1", "2026-06-03T07:00:00Z"),
                spec("t2", "2026-06-04T07:00:00Z"),
            ),
            now,
        )
        val infos = reminders()
        assertEquals(2, infos.size)
        infos.forEach { assertEquals(WorkInfo.State.ENQUEUED, it.state) }
    }

    @Test
    fun reSchedulingTheSameTaskIdReplacesWithoutDuplicate() {
        val s = spec("t1", "2026-06-03T07:00:00Z")
        scheduler.schedule(listOf(s), now)
        scheduler.schedule(listOf(s), now) // unique work → replace, not add
        assertEquals(1, reminders().size)
    }

    @Test
    fun pastTriggerStillEnqueuesWithClampedDelay() {
        scheduler.schedule(listOf(spec("past", "2026-06-01T00:00:00Z")), now) // before now → delay clamped to 0
        val infos = reminders()
        assertEquals(1, infos.size)
        // Zero-delay work is picked up immediately by the test's synchronous executor, so it may be
        // ENQUEUED/RUNNING/SUCCEEDED — the point is it was enqueued and is live (not cancelled/failed).
        assertEquals(
            true,
            infos[0].state in setOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.SUCCEEDED),
        )
    }
}
