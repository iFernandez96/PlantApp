package dev.plantapp.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

// Slice 1 network DTOs. camelCase, matching the backend API responses and the
// cross-boundary shared-schemas/*.schema.json (D-06). Optional fields are nullable
// with a null default; the module's Json is configured with encodeDefaults=false +
// explicitNulls=false, so absent optionals are omitted (honoring additionalProperties:false).

@Serializable
data class GardenSpaceDto(
    val id: String,
    val userId: String,
    val name: String,
    val kind: String,
    val indoor: Boolean? = null,
    val postalCode: String? = null,
    val countryCode: String? = null,
    val hardinessZone: String? = null,
    val direction: String? = null,
    val sunHoursEstimate: Double? = null,
    val windExposure: String? = null,
    val shadeFraction: Double? = null,
    val rainReaches: Boolean? = null,
    val photos: List<String>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class ContainerDto(
    val id: String,
    val userId: String,
    val volumeLiters: Double,
    val material: String,
    val drainage: String,
    val name: String? = null,
    val selfWatering: Boolean? = null,
    val saucer: Boolean? = null,
    val soilMix: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class PlantInstanceDto(
    val id: String,
    val userId: String,
    val profileId: String,
    val containerId: String,
    val gardenSpaceId: String,
    val growthStage: String,
    val nickname: String? = null,
    val cultivar: String? = null,
    val placement: String? = null,
    val placementHeightCm: Int? = null,
    val acquiredAt: String? = null,
    val plantedAt: String? = null,
    val lastWateredAt: String? = null,
    val supportRecorded: Boolean? = null,
    val notes: String? = null,
    val photos: List<String>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class SourceInputsDto(
    val plantInstanceId: String,
    val profileId: String,
    val profileVersion: Int,
    val containerId: String,
    val gardenSpaceId: String,
    val clockUtc: String,
    val wateringBaselineAt: String,
    val weatherWindowRef: String? = null,
    val feedbackWindowRef: String? = null,
)

@Serializable
data class CareTaskDto(
    val id: String,
    val plantInstanceId: String,
    val kind: String,
    val dueAt: String,
    val priority: String,
    val rationale: String,
    val engineVersion: String,
    val inputsHash: String,
    val sourceInputs: SourceInputsDto,
    val status: String,
    val completedAt: String? = null,
    val feedback: String? = null,
    val createdAt: String? = null,
)

// ── Request bodies ────────────────────────────────────────────────────────────

@Serializable
data class CreateGardenSpaceRequest(
    val name: String,
    val kind: String,
    val indoor: Boolean? = null,
    val postalCode: String? = null,
    val countryCode: String? = null,
)

@Serializable
data class CreateContainerRequest(
    val volumeLiters: Double,
    val material: String,
    val drainage: String,
    val name: String? = null,
    val selfWatering: Boolean? = null,
    val saucer: Boolean? = null,
    val soilMix: String? = null,
)

@Serializable
data class AddPlantRequest(
    val profileId: String,
    val containerId: String,
    val gardenSpaceId: String,
    val growthStage: String,
    val lastWateredAt: String? = null,
    val nickname: String? = null,
    val cultivar: String? = null,
    val placement: String? = null,
)

@Serializable
data class AddPlantResponse(
    val plant: PlantInstanceDto,
    val task: CareTaskDto,
)

/** Species-level catalog entry (plant-profile.schema.json). The add-plant profile selector
 *  consumes the scalar fields; the nested *Profile sub-objects are carried as opaque
 *  JsonObject (the selector doesn't need their internals). */
@Serializable
data class PlantProfileDto(
    val id: String,
    val scientificName: String,
    val commonNames: List<String>,
    val category: String,
    val growthHabit: String,
    val wateringProfile: JsonObject,
    val feedingProfile: JsonObject,
    val containerProfile: JsonObject,
    val lightProfile: JsonObject,
    val temperatureProfile: JsonObject,
    val version: Int,
    val requiresSupport: Boolean? = null,
    val selfFruitful: Boolean? = null,
    val pollinationPartnersRequired: Int? = null,
    val seasonality: JsonObject? = null,
    val commonIssues: List<String>? = null,
    val verticalSuitability: Double? = null,
    val source: JsonArray? = null,
    val lastReviewedAt: String? = null,
)

/** Slice 2 — a backend-computed, profile-driven advisory (advisory.schema.json). */
@Serializable
data class AdvisoryDto(
    val kind: String,
    val severity: String,
    val plantInstanceId: String,
    val profileId: String,
    val title: String,
    val message: String,
    val details: JsonObject? = null,
    val createdAt: String? = null,
)
