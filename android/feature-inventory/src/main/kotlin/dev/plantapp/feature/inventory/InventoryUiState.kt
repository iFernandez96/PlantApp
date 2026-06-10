package dev.plantapp.feature.inventory

import dev.plantapp.domain.model.Advisory
import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.model.Plant

/** UI state for the plant list screen. */
sealed interface PlantListUiState {
    data object Loading : PlantListUiState
    data object Empty : PlantListUiState
    data class Content(val plants: List<Plant>) : PlantListUiState
    data class Error(val message: String) : PlantListUiState
}

/** UI state for the plant detail screen. */
sealed interface PlantDetailUiState {
    data object Loading : PlantDetailUiState
    data class Content(
        val plant: Plant,
        val task: CareTask?,
        val advisories: List<Advisory> = emptyList(),
        /** Friendly species name resolved from the plant's profile; null if lookup failed. */
        val speciesName: String? = null,
    ) : PlantDetailUiState
    data class Error(val message: String) : PlantDetailUiState
}

/** UI state for the email-OTP sign-in screen. */
data class SignInUiState(
    val codeSent: Boolean = false,
    val error: String? = null,
)

/** Collected add-plant form input (ids for Slice 1; richer selectors are a later slice). */
data class AddPlantForm(
    val profileId: String,
    val containerId: String,
    val gardenSpaceId: String,
    val growthStage: String,
    val lastWateredAt: String? = null,
)
