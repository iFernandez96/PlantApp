package dev.plantapp.feature.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.plantapp.domain.model.Container
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.PlantProfile

/** Add-plant form, fully selector-driven: profile, garden-space (select-or-create), and
 *  container (select-or-create) are chosen from dropdowns; growth-stage and last-watered are
 *  small text fields. A container must be selected before [onSubmit]; otherwise a field-level
 *  error is shown and nothing submits. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    profiles: List<PlantProfile>,
    gardenSpaces: List<GardenSpace>,
    containers: List<Container>,
    onCreateGardenSpace: (name: String, kind: String) -> Unit,
    onCreateContainer: (name: String?, volumeLiters: Double, material: String, drainage: String) -> Unit,
    onSubmit: (AddPlantForm) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
) {
    var selectedProfile by remember { mutableStateOf<PlantProfile?>(null) }
    var profileExpanded by remember { mutableStateOf(false) }
    var selectedGardenSpace by remember { mutableStateOf<GardenSpace?>(null) }
    var gardenSpaceExpanded by remember { mutableStateOf(false) }
    var showCreateGardenSpace by remember { mutableStateOf(false) }
    var newGardenSpaceName by remember { mutableStateOf("") }
    var newGardenSpaceKind by remember { mutableStateOf("") }
    var selectedContainer by remember { mutableStateOf<Container?>(null) }
    var containerExpanded by remember { mutableStateOf(false) }
    var showCreateContainer by remember { mutableStateOf(false) }
    var newContainerName by remember { mutableStateOf("") }
    var newContainerVolume by remember { mutableStateOf("") }
    var newContainerMaterial by remember { mutableStateOf("") }
    var newContainerDrainage by remember { mutableStateOf("") }
    var growthStage by remember { mutableStateOf("") }

    // Auto-select a freshly created space (VM appends it to gardenSpaces).
    LaunchedEffect(gardenSpaces) {
        if (selectedGardenSpace == null) gardenSpaces.lastOrNull()?.let { selectedGardenSpace = it }
    }
    // Auto-select a freshly created container (VM appends it to containers).
    LaunchedEffect(containers) {
        if (selectedContainer == null) containers.lastOrNull()?.let { selectedContainer = it }
    }
    var lastWateredAt by remember { mutableStateOf("") }
    var containerError by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Add a plant") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ExposedDropdownMenuBox(
                expanded = profileExpanded,
                onExpandedChange = { profileExpanded = it },
            ) {
                OutlinedTextField(
                    value = selectedProfile?.let { profileLabel(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Profile") },
                    placeholder = { Text("Select a profile") },
                    supportingText = selectedProfile?.let { { Text(it.scientificName) } },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(profileExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .testTag(InventoryTestTags.FIELD_PROFILE_SELECTOR),
                )
                ExposedDropdownMenu(
                    expanded = profileExpanded,
                    onDismissRequest = { profileExpanded = false },
                ) {
                    profiles.forEach { profile ->
                        DropdownMenuItem(
                            text = { Text(profileLabel(profile)) },
                            onClick = {
                                selectedProfile = profile
                                profileExpanded = false
                            },
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = containerExpanded,
                onExpandedChange = { containerExpanded = it },
            ) {
                OutlinedTextField(
                    value = selectedContainer?.let { containerLabel(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    isError = containerError,
                    label = { Text("Container") },
                    placeholder = { Text("Select a container") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(containerExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .testTag(InventoryTestTags.FIELD_CONTAINER_SELECTOR),
                )
                ExposedDropdownMenu(
                    expanded = containerExpanded,
                    onDismissRequest = { containerExpanded = false },
                ) {
                    containers.forEach { container ->
                        DropdownMenuItem(
                            text = { Text(containerLabel(container)) },
                            onClick = {
                                selectedContainer = container
                                containerError = false
                                showCreateContainer = false
                                containerExpanded = false
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("➕ Create new container") },
                        onClick = {
                            showCreateContainer = true
                            containerExpanded = false
                        },
                        modifier = Modifier.testTag(InventoryTestTags.CONTAINER_CREATE_ITEM),
                    )
                }
            }
            if (containerError) {
                Text(
                    text = "A container is required.",
                    modifier = Modifier.testTag(InventoryTestTags.CONTAINER_ERROR),
                )
            }
            if (showCreateContainer) {
                Field("New container name (optional)", newContainerName, InventoryTestTags.FIELD_NEW_CONTAINER_NAME) {
                    newContainerName = it
                }
                Field("New container volume (L)", newContainerVolume, InventoryTestTags.FIELD_NEW_CONTAINER_VOLUME) {
                    newContainerVolume = it
                }
                Field("New container material", newContainerMaterial, InventoryTestTags.FIELD_NEW_CONTAINER_MATERIAL) {
                    newContainerMaterial = it
                }
                Field("New container drainage", newContainerDrainage, InventoryTestTags.FIELD_NEW_CONTAINER_DRAINAGE) {
                    newContainerDrainage = it
                }
                Button(
                    onClick = {
                        val volume = newContainerVolume.trim().toDoubleOrNull()
                        if (volume != null && volume > 0 &&
                            newContainerMaterial.isNotBlank() && newContainerDrainage.isNotBlank()
                        ) {
                            onCreateContainer(
                                newContainerName.trim().ifBlank { null },
                                volume,
                                newContainerMaterial.trim(),
                                newContainerDrainage.trim(),
                            )
                            showCreateContainer = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.CONTAINER_CREATE_BUTTON),
                ) { Text("Create container") }
            }
            ExposedDropdownMenuBox(
                expanded = gardenSpaceExpanded,
                onExpandedChange = { gardenSpaceExpanded = it },
            ) {
                OutlinedTextField(
                    value = selectedGardenSpace?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Garden space") },
                    placeholder = { Text("Select a garden space") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(gardenSpaceExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .testTag(InventoryTestTags.FIELD_GARDEN_SPACE_SELECTOR),
                )
                ExposedDropdownMenu(
                    expanded = gardenSpaceExpanded,
                    onDismissRequest = { gardenSpaceExpanded = false },
                ) {
                    gardenSpaces.forEach { space ->
                        DropdownMenuItem(
                            text = { Text(space.name) },
                            onClick = {
                                selectedGardenSpace = space
                                showCreateGardenSpace = false
                                gardenSpaceExpanded = false
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("➕ Create new garden space") },
                        onClick = {
                            showCreateGardenSpace = true
                            gardenSpaceExpanded = false
                        },
                        modifier = Modifier.testTag(InventoryTestTags.GARDEN_SPACE_CREATE_ITEM),
                    )
                }
            }
            if (showCreateGardenSpace) {
                Field("New garden space name", newGardenSpaceName, InventoryTestTags.FIELD_NEW_GARDEN_SPACE_NAME) {
                    newGardenSpaceName = it
                }
                Field("New garden space kind", newGardenSpaceKind, InventoryTestTags.FIELD_NEW_GARDEN_SPACE_KIND) {
                    newGardenSpaceKind = it
                }
                Button(
                    onClick = {
                        if (newGardenSpaceName.isNotBlank() && newGardenSpaceKind.isNotBlank()) {
                            onCreateGardenSpace(newGardenSpaceName.trim(), newGardenSpaceKind.trim())
                            showCreateGardenSpace = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.GARDEN_SPACE_CREATE_BUTTON),
                ) { Text("Create garden space") }
            }
            Field("Growth stage", growthStage, InventoryTestTags.FIELD_GROWTH_STAGE) { growthStage = it }
            Field("Last watered (ISO, optional)", lastWateredAt, InventoryTestTags.FIELD_LAST_WATERED_AT) {
                lastWateredAt = it
            }

            Button(
                onClick = {
                    val container = selectedContainer
                    if (container == null) {
                        containerError = true
                    } else {
                        onSubmit(
                            AddPlantForm(
                                profileId = (selectedProfile?.id ?: "").trim(),
                                containerId = container.id,
                                gardenSpaceId = selectedGardenSpace?.id ?: "",
                                growthStage = growthStage.trim(),
                                lastWateredAt = lastWateredAt.trim().ifBlank { null },
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.SUBMIT_BUTTON),
            ) { Text("Add plant") }
        }
    }
}

private fun profileLabel(profile: PlantProfile): String =
    profile.commonNames.firstOrNull() ?: profile.scientificName

private fun containerLabel(container: Container): String =
    container.name ?: "Container ${container.id.take(8)}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Field(
    label: String,
    value: String,
    tag: String,
    isError: Boolean = false,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag(tag),
    )
}
