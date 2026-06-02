package dev.plantapp.network

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Schema-valid fixtures for the Slice 1 DTOs (uuids, ISO date-times, semver, etc.). */
object DtoFixtures {
    const val PLANT_ID = "00000000-0000-4000-8000-000000000001"
    const val CONTAINER_ID = "00000000-0000-4000-8000-000000000002"
    const val SPACE_ID = "00000000-0000-4000-8000-000000000003"
    const val TASK_ID = "00000000-0000-4000-8000-0000000000aa"
    const val USER_ID = "11111111-1111-4111-8111-111111111111"
    const val TS = "2026-05-26T07:00:00.000Z"

    val gardenSpace = GardenSpaceDto(
        id = SPACE_ID,
        userId = USER_ID,
        name = "West Balcony",
        kind = "balcony",
        indoor = false,
        createdAt = TS,
    )

    val container = ContainerDto(
        id = CONTAINER_ID,
        userId = USER_ID,
        volumeLiters = 19.0,
        material = "plastic",
        drainage = "good",
        name = "Blue barrel",
        createdAt = TS,
    )

    val plantInstance = PlantInstanceDto(
        id = PLANT_ID,
        userId = USER_ID,
        profileId = "solanum-lycopersicum",
        containerId = CONTAINER_ID,
        gardenSpaceId = SPACE_ID,
        growthStage = "vegetative",
        lastWateredAt = TS,
        nickname = "Pasi",
        createdAt = TS,
    )

    val careTask = CareTaskDto(
        id = TASK_ID,
        plantInstanceId = PLANT_ID,
        kind = "water",
        dueAt = "2026-05-28T07:00:00.000Z",
        priority = "normal",
        rationale = "Tomato: base interval 2d adjusted by container factor 1; baseline $TS",
        engineVersion = "0.1.0",
        inputsHash = "a".repeat(64),
        sourceInputs = SourceInputsDto(
            plantInstanceId = PLANT_ID,
            profileId = "solanum-lycopersicum",
            profileVersion = 1,
            containerId = CONTAINER_ID,
            gardenSpaceId = SPACE_ID,
            clockUtc = TS,
            wateringBaselineAt = TS,
            weatherWindowRef = null,
            feedbackWindowRef = null,
        ),
        status = "pending",
        createdAt = TS,
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
        requiresSupport = true,
        selfFruitful = true,
    )
}
