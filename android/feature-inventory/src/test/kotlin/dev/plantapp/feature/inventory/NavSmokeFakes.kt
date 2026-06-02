package dev.plantapp.feature.inventory

import dev.plantapp.domain.model.AddPlantResult
import dev.plantapp.domain.model.Advisory
import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.model.Container
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.NewPlant
import dev.plantapp.domain.model.Plant
import dev.plantapp.domain.model.PlantProfile
import dev.plantapp.data.reminder.ReminderScheduling
import dev.plantapp.data.reminder.ReminderSync
import dev.plantapp.domain.reminder.ReminderSpec
import dev.plantapp.domain.repository.AuthRepository
import dev.plantapp.domain.repository.InventoryRepository
import java.time.Clock
import java.time.Instant

/** Test-only fakes for the NavHost smoke. Canned data + call-recording flags; no HTTP, no Hilt. */
class FakeInventoryRepository : InventoryRepository {
    val profile = PlantProfile("solanum-lycopersicum", "Solanum lycopersicum", listOf("Tomato"), "fruit")
    val gardenSpace = GardenSpace("00000000-0000-4000-8000-000000000003", "West Balcony", "balcony")
    val container = Container("00000000-0000-4000-8000-000000000002", "Blue barrel", 19.0, "plastic", "good")
    val plant = Plant(
        id = "00000000-0000-4000-8000-000000000001",
        profileId = profile.id,
        containerId = container.id,
        gardenSpaceId = gardenSpace.id,
        growthStage = "vegetative",
        nickname = "Pasi",
        lastWateredAt = "2026-05-26T07:00:00.000Z",
    )
    private val waterTask = CareTask(
        id = "00000000-0000-4000-8000-0000000000aa",
        kind = "water",
        dueAt = "2026-05-28T07:00:00.000Z",
        priority = "normal",
        rationale = "Tomato: base interval 2d",
        engineVersion = "0.1.0",
        inputsHash = "a".repeat(64),
        status = "pending",
    )
    private val repotTask = waterTask.copy(
        id = "00000000-0000-4000-8000-0000000000bb",
        kind = "repot",
        priority = "high",
        rationale = "Container is smaller than recommended",
    )
    val advisory = Advisory(
        kind = "container-size",
        severity = "high",
        plantInstanceId = plant.id,
        profileId = plant.profileId,
        title = "Container is smaller than recommended",
        message = "Passion fruit prefers at least 95 L; this container is 19 L.",
    )

    var lastAccept: Pair<String, String>? = null
    var addPlantCalled = false

    override suspend fun createGardenSpace(name: String, kind: String): GardenSpace = gardenSpace
    override suspend fun createContainer(
        name: String?,
        volumeLiters: Double,
        material: String,
        drainage: String,
    ): Container = container

    override suspend fun addPlant(newPlant: NewPlant): AddPlantResult {
        addPlantCalled = true
        return AddPlantResult(plant = plant, task = waterTask)
    }

    override suspend fun getPlantProfiles(): List<PlantProfile> = listOf(profile)
    override suspend fun getGardenSpaces(): List<GardenSpace> = listOf(gardenSpace)
    override suspend fun getContainers(): List<Container> = listOf(container)
    override suspend fun getPlants(): List<Plant> = listOf(plant)
    override suspend fun getPlantTasks(plantId: String): List<CareTask> = listOf(waterTask)
    override suspend fun getAdvisories(plantId: String): List<Advisory> = listOf(advisory)

    override suspend fun acceptAdvisory(plantId: String, kind: String): CareTask {
        lastAccept = plantId to kind
        return repotTask
    }

    override suspend fun deletePlant(plantId: String) {}
}

/** No-op scheduler so the list ViewModel's app-open reminder sync is inert in the nav smoke. */
private class NoopReminderScheduling : ReminderScheduling {
    override fun schedule(specs: List<ReminderSpec>, now: Instant) {}
}

/** A ReminderSync over the given repo that schedules nowhere — keeps the smoke focused on nav. */
fun reminderSync(repo: InventoryRepository): ReminderSync =
    ReminderSync(repo, NoopReminderScheduling(), Clock.systemUTC())

/** Fake email-OTP auth: records the calls and succeeds (token persistence is out of scope here). */
class FakeAuthRepository : AuthRepository {
    var requested = false
    var verified = false

    override suspend fun requestOtp(email: String) {
        requested = true
    }

    override suspend fun verifyOtp(email: String, code: String) {
        verified = true
    }
}
