package dev.plantapp.feature.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.plantapp.domain.model.NewPlant
import dev.plantapp.domain.model.PlantProfile
import dev.plantapp.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Loads the caller's plants for the list screen. */
@HiltViewModel
class PlantListViewModel @Inject constructor(
    private val repository: InventoryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<PlantListUiState>(PlantListUiState.Loading)
    val state: StateFlow<PlantListUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = PlantListUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                val plants = repository.getPlants()
                if (plants.isEmpty()) PlantListUiState.Empty else PlantListUiState.Content(plants)
            } catch (e: Exception) {
                PlantListUiState.Error(e.message ?: "unknown error")
            }
        }
    }
}

/** Submits the add-plant form via the repository; reports the new plant id on success. */
@HiltViewModel
class AddPlantViewModel @Inject constructor(
    private val repository: InventoryRepository,
) : ViewModel() {
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _profiles = MutableStateFlow<List<PlantProfile>>(emptyList())
    val profiles: StateFlow<List<PlantProfile>> = _profiles.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _profiles.value = repository.getPlantProfiles()
            } catch (e: Exception) {
                _error.value = e.message ?: "Could not load profiles"
            }
        }
    }

    fun submit(form: AddPlantForm, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.addPlant(
                    NewPlant(
                        profileId = form.profileId,
                        containerId = form.containerId,
                        gardenSpaceId = form.gardenSpaceId,
                        growthStage = form.growthStage,
                        lastWateredAt = form.lastWateredAt,
                    ),
                )
                onSaved(result.plant.id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Could not add plant"
            }
        }
    }
}

/** Loads a single plant + its initial care task for the detail screen. */
@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    private val repository: InventoryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<PlantDetailUiState>(PlantDetailUiState.Loading)
    val state: StateFlow<PlantDetailUiState> = _state.asStateFlow()

    fun loadFor(plantId: String) {
        _state.value = PlantDetailUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                val plant = repository.getPlants().firstOrNull { it.id == plantId }
                if (plant == null) {
                    PlantDetailUiState.Error("Plant not found")
                } else {
                    val task = repository.getPlantTasks(plantId).firstOrNull()
                    val advisories = repository.getAdvisories(plantId)
                    PlantDetailUiState.Content(plant = plant, task = task, advisories = advisories)
                }
            } catch (e: Exception) {
                PlantDetailUiState.Error(e.message ?: "unknown error")
            }
        }
    }
}
