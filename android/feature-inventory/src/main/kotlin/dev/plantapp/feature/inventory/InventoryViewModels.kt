package dev.plantapp.feature.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.plantapp.domain.SessionExpiredException
import dev.plantapp.domain.model.Container
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.NewPlant
import dev.plantapp.domain.model.PlantProfile
import dev.plantapp.data.reminder.ReminderSync
import dev.plantapp.domain.repository.AuthRepository
import dev.plantapp.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Loads the caller's plants for the list screen. On a successful load it also (re)schedules local
 *  watering reminders on app open (fire-and-forget; a scheduling failure never affects the list). */
@HiltViewModel
class PlantListViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val reminderSync: ReminderSync,
) : ViewModel() {
    private val _state = MutableStateFlow<PlantListUiState>(PlantListUiState.Loading)
    val state: StateFlow<PlantListUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        // Quiet refresh: only show the spinner when there is nothing useful on screen yet.
        if (_state.value !is PlantListUiState.Content) _state.value = PlantListUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                val plants = repository.getPlants()
                // Successful load → (re)schedule local reminders; isolated so it can't break the UI.
                viewModelScope.launch { runCatching { reminderSync.syncNow() } }
                // Friendly names only; a profile failure must never break the list.
                val speciesNames = runCatching { repository.getPlantProfiles() }
                    .getOrDefault(emptyList())
                    .mapNotNull { profile -> profile.commonNames.firstOrNull()?.let { profile.id to it } }
                    .toMap()
                if (plants.isEmpty()) {
                    PlantListUiState.Empty
                } else {
                    PlantListUiState.Content(plants, speciesNames = speciesNames)
                }
            } catch (e: Exception) {
                when (e) {
                    is SessionExpiredException -> PlantListUiState.SignedOut
                    else -> PlantListUiState.Error(DisplayText.friendlyError(e, "We couldn't load your plants. Check your connection and try again."))
                }
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
                _error.value = DisplayText.friendlyError(e, "We couldn't load the plant choices. Check your connection and try again.")
            }
        }
    }

    fun createGardenSpace(name: String, kind: String) {
        viewModelScope.launch {
            try {
                val gs = repository.createGardenSpace(name, kind)
                _gardenSpaces.value = _gardenSpaces.value + gs
            } catch (e: Exception) {
                _error.value = DisplayText.friendlyError(e, "We couldn't save your space. Please try again.")
            }
        }
    }

    fun createContainer(name: String?, volumeLiters: Double, material: String, drainage: String) {
        viewModelScope.launch {
            try {
                val c = repository.createContainer(name, volumeLiters, material, drainage)
                _containers.value = _containers.value + c
            } catch (e: Exception) {
                _error.value = DisplayText.friendlyError(e, "We couldn't save your pot. Please try again.")
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
                _error.value = DisplayText.friendlyError(e, "We couldn't add your plant. Please try again.")
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
            _state.update { it.copy(busy = true, error = null) }
            try {
                auth.requestOtp(email)
                _state.update { it.copy(codeSent = true, busy = false) }
            } catch (e: Exception) {
                // Fixed copy only — never surface raw exception text to the sign-in UI.
                _state.update {
                    it.copy(busy = false, error = "We couldn't send the code. Check the email address and try again.")
                }
            }
        }
    }

    fun verify(email: String, code: String, onSignedIn: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            try {
                auth.verifyOtp(email, code)
                onSignedIn()
            } catch (e: Exception) {
                _state.update {
                    it.copy(busy = false, error = "That code didn't work. Check the digits and try again.")
                }
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
                    // Friendly name only; a profile-lookup failure must never fail the screen.
                    val speciesName = runCatching {
                        repository.getPlantProfiles()
                            .firstOrNull { it.id == plant.profileId }
                            ?.commonNames?.firstOrNull()
                    }.getOrNull()
                    PlantDetailUiState.Content(
                        plant = plant,
                        task = task,
                        advisories = advisories,
                        speciesName = speciesName,
                    )
                }
            } catch (e: Exception) {
                PlantDetailUiState.Error(DisplayText.friendlyError(e, "We couldn't load this plant. Check your connection and try again."))
            }
        }
    }

    /** Accept an advisory: the backend creates the deterministic CareTask (D-09), then reload so
     *  the new task + refreshed advisories show. On failure, reload to keep state consistent. */
    fun accept(plantId: String, kind: String) {
        viewModelScope.launch {
            try {
                repository.acceptAdvisory(plantId, kind)
                loadFor(plantId)
            } catch (_: Exception) {
                loadFor(plantId)
            }
        }
    }
}
