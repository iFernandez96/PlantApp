package dev.plantapp.feature.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.plantapp.domain.model.Container
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.NewPlant
import dev.plantapp.domain.model.PlantProfile
import dev.plantapp.domain.repository.AuthRepository
import dev.plantapp.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _gardenSpaces = MutableStateFlow<List<GardenSpace>>(emptyList())
    val gardenSpaces: StateFlow<List<GardenSpace>> = _gardenSpaces.asStateFlow()

    private val _containers = MutableStateFlow<List<Container>>(emptyList())
    val containers: StateFlow<List<Container>> = _containers.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _profiles.value = repository.getPlantProfiles()
                _gardenSpaces.value = repository.getGardenSpaces()
                _containers.value = repository.getContainers()
            } catch (e: Exception) {
                _error.value = e.message ?: "Could not load add-plant options"
            }
        }
    }

    fun createGardenSpace(name: String, kind: String) {
        viewModelScope.launch {
            try {
                val gs = repository.createGardenSpace(name, kind)
                _gardenSpaces.value = _gardenSpaces.value + gs
            } catch (e: Exception) {
                _error.value = e.message ?: "Could not create garden space"
            }
        }
    }

    fun createContainer(name: String?, volumeLiters: Double, material: String, drainage: String) {
        viewModelScope.launch {
            try {
                val c = repository.createContainer(name, volumeLiters, material, drainage)
                _containers.value = _containers.value + c
            } catch (e: Exception) {
                _error.value = e.message ?: "Could not create container"
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

/** Drives email-OTP sign-in: requests a code, then verifies it (the token is persisted by the
 *  AuthRepository on success). */
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    fun requestCode(email: String) {
        viewModelScope.launch {
            try {
                auth.requestOtp(email)
                _state.update { it.copy(codeSent = true, error = null) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Could not send code") }
            }
        }
    }

    fun verify(email: String, code: String, onSignedIn: () -> Unit) {
        viewModelScope.launch {
            try {
                auth.verifyOtp(email, code)
                onSignedIn()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Invalid code") }
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
