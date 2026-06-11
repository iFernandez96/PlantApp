package dev.plantapp.data.mapper

import dev.plantapp.domain.model.AddPlantResult
import dev.plantapp.domain.model.Advisory
import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.model.Container
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.NewPlant
import dev.plantapp.domain.model.Plant
import dev.plantapp.domain.model.PlantProfile
import dev.plantapp.network.AddPlantRequest
import dev.plantapp.network.AddPlantResponse
import dev.plantapp.network.AdvisoryDto
import dev.plantapp.network.CareTaskDto
import dev.plantapp.network.ContainerDto
import dev.plantapp.network.GardenSpaceDto
import dev.plantapp.network.PlantInstanceDto
import dev.plantapp.network.PlantProfileDto
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

// :network DTO ↔ :domain mapping for Slice 1.

fun GardenSpaceDto.toDomain(): GardenSpace = GardenSpace(id = id, name = name, kind = kind)

fun PlantProfileDto.toDomain(): PlantProfile = PlantProfile(
    id = id,
    scientificName = scientificName,
    commonNames = commonNames,
    category = category,
    // runCatching keeps the mapper total: jsonPrimitive throws on nested objects/arrays.
    wateringIntervalDays = wateringProfile["baseIntervalDays"]
        ?.let { runCatching { it.jsonPrimitive.doubleOrNull }.getOrNull() },
    feedingIntervalDays = feedingProfile["baseIntervalDays"]
        ?.let { runCatching { it.jsonPrimitive.doubleOrNull }.getOrNull() },
    sunHoursTarget = lightProfile["targetSunHours"]
        ?.let { runCatching { it.jsonPrimitive.doubleOrNull }.getOrNull() },
    frostSensitive = temperatureProfile["frostSensitive"]
        ?.let { runCatching { it.jsonPrimitive.booleanOrNull }.getOrNull() },
    commonIssues = commonIssues ?: emptyList(),
)

fun ContainerDto.toDomain(): Container = Container(
    id = id,
    name = name,
    volumeLiters = volumeLiters,
    material = material,
    drainage = drainage,
)

fun PlantInstanceDto.toDomain(): Plant = Plant(
    id = id,
    profileId = profileId,
    containerId = containerId,
    gardenSpaceId = gardenSpaceId,
    growthStage = growthStage,
    nickname = nickname,
    lastWateredAt = lastWateredAt,
)

fun CareTaskDto.toDomain(): CareTask = CareTask(
    id = id,
    kind = kind,
    dueAt = dueAt,
    priority = priority,
    rationale = rationale,
    engineVersion = engineVersion,
    inputsHash = inputsHash,
    status = status,
)

fun AddPlantResponse.toDomain(): AddPlantResult =
    AddPlantResult(plant = plant.toDomain(), task = task.toDomain())

fun AdvisoryDto.toDomain(): Advisory = Advisory(
    kind = kind,
    severity = severity,
    plantInstanceId = plantInstanceId,
    profileId = profileId,
    title = title,
    message = message,
)

fun NewPlant.toRequest(): AddPlantRequest = AddPlantRequest(
    profileId = profileId,
    containerId = containerId,
    gardenSpaceId = gardenSpaceId,
    growthStage = growthStage,
    lastWateredAt = lastWateredAt,
    nickname = nickname,
    cultivar = cultivar,
    placement = placement,
)
