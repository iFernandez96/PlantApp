package dev.plantapp.data

import dev.plantapp.domain.model.NewPlant
import dev.plantapp.domain.repository.InventoryRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/** Maps :network DTOs ↔ :domain models. Uses a fake PlantAppApi (no HTTP). */
class InventoryRepositoryImplTest {
    private fun repo(api: FakePlantAppApi): InventoryRepository = InventoryRepositoryImpl(api)

    @Test
    fun addPlantMapsResponseToDomain() = runTest {
        val api = FakePlantAppApi()
        val result = repo(api).addPlant(
            NewPlant(
                profileId = "solanum-lycopersicum",
                containerId = api.container.id,
                gardenSpaceId = api.gardenSpace.id,
                growthStage = "vegetative",
                nickname = "Pasi",
                lastWateredAt = "2026-05-26T07:00:00.000Z",
            ),
        )
        // request mapped NewPlant → AddPlantRequest
        assertEquals("solanum-lycopersicum", api.lastAddPlantRequest?.profileId)
        assertEquals(api.container.id, api.lastAddPlantRequest?.containerId)
        assertEquals("Pasi", api.lastAddPlantRequest?.nickname)
        // response mapped → domain plant + task
        assertEquals(api.plant.id, result.plant.id)
        assertEquals("solanum-lycopersicum", result.plant.profileId)
        assertEquals("water", result.task.kind)
        assertEquals("0.1.0", result.task.engineVersion)
        assertEquals(api.task.inputsHash, result.task.inputsHash)
        assertEquals(api.task.dueAt, result.task.dueAt)
        assertEquals("normal", result.task.priority)
        assertEquals("pending", result.task.status)
        assertTrue(result.task.rationale.isNotEmpty())
    }

    @Test
    fun getPlantsMapsList() = runTest {
        val api = FakePlantAppApi()
        val plants = repo(api).getPlants()
        assertEquals(1, plants.size)
        assertEquals(api.plant.id, plants[0].id)
        assertEquals("vegetative", plants[0].growthStage)
    }

    @Test
    fun getPlantTasksMapsList() = runTest {
        val api = FakePlantAppApi()
        val tasks = repo(api).getPlantTasks(api.plant.id)
        assertEquals(1, tasks.size)
        assertEquals("water", tasks[0].kind)
        assertEquals("0.1.0", tasks[0].engineVersion)
    }

    @Test
    fun createGardenSpaceAndContainerMapToDomain() = runTest {
        val api = FakePlantAppApi()
        val gs = repo(api).createGardenSpace(name = "West Balcony", kind = "balcony")
        assertEquals(api.gardenSpace.id, gs.id)
        assertEquals("balcony", gs.kind)
        val c = repo(api).createContainer(
            name = "Blue barrel",
            volumeLiters = 19.0,
            material = "plastic",
            drainage = "good",
        )
        assertEquals(api.container.id, c.id)
        assertEquals(19.0, c.volumeLiters)
    }

    @Test
    fun deletePlantCallsApi() = runTest {
        val api = FakePlantAppApi()
        repo(api).deletePlant(api.plant.id)
        assertEquals(listOf(api.plant.id), api.deleted)
    }
}
