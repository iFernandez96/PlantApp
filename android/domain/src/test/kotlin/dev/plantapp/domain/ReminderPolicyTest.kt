package dev.plantapp.domain

import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.reminder.computeReminders
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Pure reminder-scheduling policy: filters pending+fresh tasks, applies lead/stale, deterministic. */
class ReminderPolicyTest {
    private val now = Instant.parse("2026-06-02T00:00:00.000Z")

    private fun task(
        id: String,
        dueAt: String,
        status: String = "pending",
        kind: String = "water",
    ) = CareTask(
        id = id,
        kind = kind,
        dueAt = dueAt,
        priority = "normal",
        rationale = "r",
        engineVersion = "0.1.0",
        inputsHash = "a".repeat(64),
        status = status,
    )

    @Test
    fun pendingFutureTaskYieldsOneSpecWithTriggerAtDueAtForZeroLead() {
        val t = task("t1", "2026-06-03T07:00:00.000Z")
        val specs = computeReminders(listOf(t), now)
        assertEquals(1, specs.size)
        assertEquals("t1", specs[0].taskId)
        assertEquals("water", specs[0].kind)
        assertEquals(t.dueAt, specs[0].dueAt) // dueAt echoed verbatim
        // zero lead → trigger at the due instant (triggerAtUtc is canonical ISO, so compare instants)
        assertEquals(Instant.parse(t.dueAt), Instant.parse(specs[0].triggerAtUtc))
    }

    @Test
    fun nonPendingTasksAreExcluded() {
        val tasks = listOf(
            task("done", "2026-06-03T07:00:00.000Z", status = "done"),
            task("skipped", "2026-06-03T07:00:00.000Z", status = "skipped"),
            task("dismissed", "2026-06-03T07:00:00.000Z", status = "dismissed"),
        )
        assertTrue(computeReminders(tasks, now).isEmpty())
    }

    @Test
    fun tasksOlderThanStaleWindowAreExcludedButJustInsideAreIncluded() {
        // staleAfter default = 7 days. now = 2026-06-02T00:00Z.
        val tooOld = task("old", "2026-05-25T00:00:00.000Z") // exactly 8 days before now → excluded
        val justInside = task("fresh", "2026-05-26T00:30:00.000Z") // ~6.98 days before now → included
        val specs = computeReminders(listOf(tooOld, justInside), now)
        assertEquals(listOf("fresh"), specs.map { it.taskId })
    }

    @Test
    fun nonZeroLeadTimeShiftsTriggerEarlier() {
        val t = task("t", "2026-06-03T07:00:00.000Z")
        val specs = computeReminders(listOf(t), now, leadTime = Duration.ofHours(1))
        assertEquals("2026-06-03T06:00:00Z", specs[0].triggerAtUtc)
    }

    @Test
    fun pastTriggerIsStillEmitted() {
        // due 30 min from now, but 1h lead → trigger 30 min in the PAST. Still emitted.
        val t = task("soon", "2026-06-02T00:30:00.000Z")
        val specs = computeReminders(listOf(t), now, leadTime = Duration.ofHours(1))
        assertEquals(1, specs.size)
        assertTrue(Instant.parse(specs[0].triggerAtUtc).isBefore(now))
    }

    @Test
    fun unparseableDueAtIsSkipped() {
        val tasks = listOf(task("bad", "not-a-date"), task("ok", "2026-06-03T07:00:00.000Z"))
        assertEquals(listOf("ok"), computeReminders(tasks, now).map { it.taskId })
    }

    @Test
    fun deterministicAndPreservesInputOrder() {
        val tasks = listOf(
            task("b", "2026-06-05T07:00:00.000Z"),
            task("a", "2026-06-03T07:00:00.000Z"),
        )
        val first = computeReminders(tasks, now)
        val second = computeReminders(tasks, now)
        assertEquals(first, second)
        assertEquals(listOf("b", "a"), first.map { it.taskId }) // input order, not sorted
    }
}
