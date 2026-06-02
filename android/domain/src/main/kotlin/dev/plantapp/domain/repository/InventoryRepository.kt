package dev.plantapp.domain.repository

import dev.plantapp.domain.model.AddPlantResult
import dev.plantapp.domain.model.Advisory
import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.model.Container
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.NewPlant
import dev.plantapp.domain.model.Plant

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
    suspend fun getPlants(): List<Plant>
    suspend fun getPlantTasks(plantId: String): List<CareTask>
    suspend fun getAdvisories(plantId: String): List<Advisory>
    suspend fun deletePlant(plantId: String)
}
