package dev.plantapp.data

import dev.plantapp.network.AddPlantRequest
import dev.plantapp.network.AddPlantResponse
import dev.plantapp.network.AdvisoryDto
import dev.plantapp.network.CareTaskDto
import dev.plantapp.network.ContainerDto
import dev.plantapp.network.CreateContainerRequest
import dev.plantapp.network.CreateGardenSpaceRequest
import dev.plantapp.network.GardenSpaceDto
import dev.plantapp.network.PlantAppApi
import dev.plantapp.network.PlantInstanceDto
import dev.plantapp.network.PlantProfileDto
import dev.plantapp.network.SourceInputsDto
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import retrofit2.Response

/** Hand-written fake of the :network PlantAppApi for repository mapping tests. Records
 *  calls and returns canned DTOs; no real HTTP. */
class FakePlantAppApi : PlantAppApi {
    val deleted = mutableListOf<String>()
    var lastAddPlantRequest: AddPlantRequest? = null

    val gardenSpace = GardenSpaceDto(
        id = "00000000-0000-4000-8000-000000000003",
        userId = "u",
        name = "West Balcony",
        kind = "balcony",
    )
    val container = ContainerDto(
        id = "00000000-0000-4000-8000-000000000002",
        userId = "u",
        volumeLiters = 19.0,
        material = "plastic",
        drainage = "good",
        name = "Blue barrel",
    )
    val plant = PlantInstanceDto(
        id = "00000000-0000-4000-8000-000000000001",
        userId = "u",
        profileId = "solanum-lycopersicum",
        containerId = container.id,
        gardenSpaceId = gardenSpace.id,
        growthStage = "vegetative",
        nickname = "Pasi",
        lastWateredAt = "2026-05-26T07:00:00.000Z",
        createdAt = "2026-05-26T07:00:00.000Z",
    )
    val task = CareTaskDto(
        id = "00000000-0000-4000-8000-0000000000aa",
        plantInstanceId = plant.id,
        kind = "water",
        dueAt = "2026-05-28T07:00:00.000Z",
        priority = "normal",
        rationale = "Tomato: base interval 2d adjusted by container factor 1; baseline ...",
        engineVersion = "0.1.0",
        inputsHash = "a".repeat(64),
        sourceInputs = SourceInputsDto(
            plantInstanceId = plant.id,
            profileId = "solanum-lycopersicum",
            profileVersion = 1,
            containerId = container.id,
            gardenSpaceId = gardenSpace.id,
            clockUtc = "2026-05-26T07:00:00.000Z",
            wateringBaselineAt = "2026-05-26T07:00:00.000Z",
        ),
        status = "pending",
        createdAt = "2026-05-26T07:00:00.000Z",
    )

    override suspend fun createGardenSpace(body: CreateGardenSpaceRequest): GardenSpaceDto = gardenSpace
    override suspend fun createContainer(body: CreateContainerRequest): ContainerDto = container
    override suspend fun addPlant(body: AddPlantRequest): AddPlantResponse {
        lastAddPlantRequest = body
        return AddPlantResponse(plant = plant, task = task)
    }
    val advisory = AdvisoryDto(
        kind = "container-size",
        severity = "high",
        plantInstanceId = plant.id,
        profileId = plant.profileId,
        title = "Container is smaller than recommended",
        message = "Passion fruit prefers at least 95 L (ideal 95-190 L); this container is 19 L.",
    )

    val plantProfile = PlantProfileDto(
        id = "solanum-lycopersicum",
        scientificName = "Solanum lycopersicum",
        commonNames = listOf("Tomato"),
        category = "fruit",
        growthHabit = "vine",
        wateringProfile = buildJsonObject {
            put("baseIntervalDays", 2)
            put("dryingTolerance", "low")
        },
        feedingProfile = buildJsonObject { put("baseIntervalDays", 7) },
        containerProfile = buildJsonObject { put("recommendedMinLiters", 19) },
        lightProfile = buildJsonObject { put("targetSunHours", 8) },
        temperatureProfile = buildJsonObject { put("frostSensitive", true) },
        version = 1,
    )

    override suspend fun getPlantProfiles(): List<PlantProfileDto> = listOf(plantProfile)
    override suspend fun getGardenSpaces(): List<GardenSpaceDto> = listOf(gardenSpace)
    override suspend fun getContainers(): List<ContainerDto> = listOf(container)
    override suspend fun listPlants(): List<PlantInstanceDto> = listOf(plant)
    override suspend fun getPlant(id: String): PlantInstanceDto = plant
    override suspend fun getPlantTasks(id: String): List<CareTaskDto> = listOf(task)
    override suspend fun getAdvisories(id: String): List<AdvisoryDto> = listOf(advisory)
    override suspend fun deletePlant(id: String): Response<Unit> {
        deleted += id
        return Response.success(Unit)
    }
}
