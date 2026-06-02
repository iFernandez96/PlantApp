package dev.plantapp.domain.repository

import dev.plantapp.domain.model.AddPlantResult
import dev.plantapp.domain.model.Advisory
import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.model.Container
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.NewPlant
import dev.plantapp.domain.model.Plant
import dev.plantapp.domain.model.PlantProfile

/** Slice 1 inventory port. Implemented in :data over the backend API; Android holds no
 *  care-scheduling logic (D-09) — tasks come from the backend as opaque values. */
interface InventoryRepository {
    suspend fun createGardenSpace(name: String, kind: String): GardenSpace
    suspend fun createContainer(
        name: String?,
        volumeLiters: Double,
        material: String,
        drainage: String,
    ): Container
    suspend fun addPlant(newPlant: NewPlant): AddPlantResult
    suspend fun getPlantProfiles(): List<PlantProfile>
    suspend fun getGardenSpaces(): List<GardenSpace>
    suspend fun getContainers(): List<Container>
    suspend fun getPlants(): List<Plant>
    suspend fun getPlantTasks(plantId: String): List<CareTask>
    suspend fun getAdvisories(plantId: String): List<Advisory>

    /** Accept a currently-applicable advisory; the backend creates a deterministic CareTask and
     *  returns it. Android holds no care logic (D-09) — the task is computed server-side. */
    suspend fun acceptAdvisory(plantId: String, kind: String): CareTask
    suspend fun deletePlant(plantId: String)
}
