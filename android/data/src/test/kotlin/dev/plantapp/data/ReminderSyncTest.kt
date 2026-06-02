package dev.plantapp.data

import dev.plantapp.data.reminder.ReminderScheduling
import dev.plantapp.data.reminder.ReminderSync
import dev.plantapp.domain.model.AddPlantResult
import dev.plantapp.domain.model.Advisory
import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.model.Container
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.NewPlant
import dev.plantapp.domain.model.Plant
import dev.plantapp.domain.model.PlantProfile
import dev.plantapp.domain.reminder.ReminderSpec
import dev.plantapp.domain.repository.InventoryRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

/** ReminderSync aggregates the caller's pending CareTasks, runs the pure policy, and hands specs
 *  to the scheduler seam — with an injected fixed clock for determinism. */
class ReminderSyncTest {
    private fun task(id: String, dueAt: String, status: String) = CareTask(
        id = id,
        kind = "water",
        dueAt = dueAt,
        priority = "normal",
        rationale = "r",
        engineVersion = "0.1.0",
        inputsHash = "a".repeat(64),
        status = status,
    )

    /** One plant; one pending (future) task + one done task. */
    private class FakeRepo(val tasks: List<CareTask>) : InventoryRepository {
        val plant = Plant("p1", "solanum-lycopersicum", "c1", "g1", "vegetative")
        override suspend fun getPlants(): List<Plant> = listOf(plant)
        override suspend fun getPlantTasks(plantId: String): List<CareTask> = tasks
        override suspend fun createGardenSpace(name: String, kind: String): GardenSpace = error("unused")
        override suspend fun createContainer(name: String?, volumeLiters: Double, material: String, drainage: String): Container = error("unused")
        override suspend fun addPlant(newPlant: NewPlant): AddPlantResult = error("unused")
        override suspend fun getPlantProfiles(): List<PlantProfile> = emptyList()
        override suspend fun getGardenSpaces(): List<GardenSpace> = emptyList()
        override suspend fun getContainers(): List<Container> = emptyList()
        override suspend fun getAdvisories(plantId: String): List<Advisory> = emptyList()
        override suspend fun acceptAdvisory(plantId: String, kind: String): CareTask = error("unused")
        override suspend fun deletePlant(plantId: String) = error("unused")
    }

    private class FakeReminderScheduling : ReminderScheduling {
        var lastSpecs: List<ReminderSpec>? = null
        var lastNow: Instant? = null
        override fun schedule(specs: List<ReminderSpec>, now: Instant) {
            lastSpecs = specs
            lastNow = now
        }
    }

    @Test
    fun syncNowGathersPendingTasksAndSchedulesThem() = runTest {
        val nowInstant = Instant.parse("2026-06-01T00:00:00Z")
        val clock = Clock.fixed(nowInstant, ZoneOffset.UTC)
        val repo = FakeRepo(
            listOf(
                task("pending-1", "2026-06-03T07:00:00.000Z", "pending"),
                task("done-1", "2026-06-03T07:00:00.000Z", "done"),
            ),
        )
        val scheduler = FakeReminderScheduling()

        ReminderSync(repo, scheduler, clock).syncNow()

        assertEquals(nowInstant, scheduler.lastNow)
        assertEquals(listOf("pending-1"), scheduler.lastSpecs?.map { it.taskId }) // only the pending task
    }
}
