package dev.plantapp.data.repository

import dev.plantapp.data.mapper.toDomain
import dev.plantapp.data.mapper.toRequest
import dev.plantapp.domain.SessionExpiredException
import dev.plantapp.domain.model.AddPlantResult
import dev.plantapp.domain.model.Advisory
import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.model.Container
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.NewPlant
import dev.plantapp.domain.model.Plant
import dev.plantapp.domain.model.PlantProfile
import dev.plantapp.domain.repository.InventoryRepository
import dev.plantapp.network.AcceptAdvisoryRequest
import dev.plantapp.network.CreateContainerRequest
import dev.plantapp.network.CreateGardenSpaceRequest
import dev.plantapp.network.PlantAppApi
import javax.inject.Inject

/** InventoryRepository over the backend [PlantAppApi]. Maps DTO↔domain; holds no care
 *  logic (D-09 — CareTask is opaque backend output). */
class InventoryRepositoryImpl @Inject constructor(
    private val api: PlantAppApi,
) : InventoryRepository {

    /** A 401 here means the session is gone (the OkHttp authenticator already tried one
     *  refresh) — surface it as the typed domain signal so the UI can route to sign-in. */
    private suspend fun <T> authed(block: suspend () -> T): T = try {
        block()
    } catch (e: retrofit2.HttpException) {
        if (e.code() == 401) throw SessionExpiredException() else throw e
    }

    override suspend fun createGardenSpace(name: String, kind: String): GardenSpace = authed {
        api.createGardenSpace(CreateGardenSpaceRequest(name = name, kind = kind)).toDomain()
    }

    override suspend fun createContainer(
        name: String?,
        volumeLiters: Double,
        material: String,
        drainage: String,
    ): Container = authed {
        api.createContainer(
            CreateContainerRequest(
                name = name,
                volumeLiters = volumeLiters,
                material = material,
                drainage = drainage,
            ),
        ).toDomain()
    }

    override suspend fun addPlant(newPlant: NewPlant): AddPlantResult = authed {
        api.addPlant(newPlant.toRequest()).toDomain()
    }

    override suspend fun getPlantProfiles(): List<PlantProfile> = authed {
        api.getPlantProfiles().map { it.toDomain() }
    }

    override suspend fun getGardenSpaces(): List<GardenSpace> = authed {
        api.getGardenSpaces().map { it.toDomain() }
    }

    override suspend fun getContainers(): List<Container> = authed {
        api.getContainers().map { it.toDomain() }
    }

    override suspend fun getPlants(): List<Plant> = authed {
        api.listPlants().map { it.toDomain() }
    }

    override suspend fun getPlantTasks(plantId: String): List<CareTask> = authed {
        api.getPlantTasks(plantId).map { it.toDomain() }
    }

    override suspend fun getAdvisories(plantId: String): List<Advisory> = authed {
        api.getAdvisories(plantId).map { it.toDomain() }
    }

    override suspend fun acceptAdvisory(plantId: String, kind: String): CareTask = authed {
        api.acceptAdvisory(plantId, AcceptAdvisoryRequest(kind)).toDomain()
    }

    override suspend fun deletePlant(plantId: String) = authed {
        val response = api.deletePlant(plantId)
        check(response.isSuccessful) { "deletePlant failed: HTTP ${response.code()}" }
    }
}
