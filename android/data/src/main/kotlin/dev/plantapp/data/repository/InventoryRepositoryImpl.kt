package dev.plantapp.data.repository

import dev.plantapp.data.mapper.toDomain
import dev.plantapp.data.mapper.toRequest
import dev.plantapp.domain.model.AddPlantResult
import dev.plantapp.domain.model.Advisory
import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.model.Container
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.NewPlant
import dev.plantapp.domain.model.Plant
import dev.plantapp.domain.repository.InventoryRepository
import dev.plantapp.network.CreateContainerRequest
import dev.plantapp.network.CreateGardenSpaceRequest
import dev.plantapp.network.PlantAppApi
import javax.inject.Inject

/** InventoryRepository over the backend [PlantAppApi]. Maps DTO↔domain; holds no care
 *  logic (D-09 — CareTask is opaque backend output). */
class InventoryRepositoryImpl @Inject constructor(
    private val api: PlantAppApi,
) : InventoryRepository {

    override suspend fun createGardenSpace(name: String, kind: String): GardenSpace =
        api.createGardenSpace(CreateGardenSpaceRequest(name = name, kind = kind)).toDomain()

    override suspend fun createContainer(
        name: String?,
        volumeLiters: Double,
        material: String,
        drainage: String,
    ): Container =
        api.createContainer(
            CreateContainerRequest(
                name = name,
                volumeLiters = volumeLiters,
                material = material,
                drainage = drainage,
            ),
        ).toDomain()

    override suspend fun addPlant(newPlant: NewPlant): AddPlantResult =
        api.addPlant(newPlant.toRequest()).toDomain()

    override suspend fun getPlants(): List<Plant> =
        api.listPlants().map { it.toDomain() }

    override suspend fun getPlantTasks(plantId: String): List<CareTask> =
        api.getPlantTasks(plantId).map { it.toDomain() }

    override suspend fun getAdvisories(plantId: String): List<Advisory> =
        api.getAdvisories(plantId).map { it.toDomain() }

    override suspend fun deletePlant(plantId: String) {
        val response = api.deletePlant(plantId)
        check(response.isSuccessful) { "deletePlant failed: HTTP ${response.code()}" }
    }
}
