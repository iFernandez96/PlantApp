package dev.plantapp.domain.model

// Pure-Kotlin Slice 1 inventory domain models. No Android, no serialization.
// CareTask is treated as an OPAQUE, backend-computed value (D-09: no care-scheduling
// logic on device); Android only displays it.

data class GardenSpace(
    val id: String,
    val name: String,
    val kind: String,
)

/** Catalog entry for the add-plant profile selector. Subset of plant-profile.schema.json;
 *  the deterministic care fields stay backend-side (D-09). */
data class PlantProfile(
    val id: String,
    val scientificName: String,
    val commonNames: List<String>,
    val category: String,
)

data class Container(
    val id: String,
    val name: String?,
    val volumeLiters: Double,
    val material: String,
    val drainage: String,
)

/** Input to add a plant; optional fields default to null. */
data class NewPlant(
    val profileId: String,
    val containerId: String,
    val gardenSpaceId: String,
    val growthStage: String,
    val nickname: String? = null,
    val cultivar: String? = null,
    val placement: String? = null,
    val lastWateredAt: String? = null,
)

data class Plant(
    val id: String,
    val profileId: String,
    val containerId: String,
    val gardenSpaceId: String,
    val growthStage: String,
    val nickname: String? = null,
    val lastWateredAt: String? = null,
)

/** Backend-computed care task. Opaque to Android — surfaced for display only. */
data class CareTask(
    val id: String,
    val kind: String,
    val dueAt: String,
    val priority: String,
    val rationale: String,
    val engineVersion: String,
    val inputsHash: String,
    val status: String,
)

data class AddPlantResult(
    val plant: Plant,
    val task: CareTask,
)

/** Backend-computed, profile-driven advisory. Informational only — Android displays it
 *  and never computes it or turns it into a CareTask. */
data class Advisory(
    val kind: String,
    val severity: String,
    val plantInstanceId: String,
    val profileId: String,
    val title: String,
    val message: String,
)
