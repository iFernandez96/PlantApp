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
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.PlantProfile

/** Slice 1/2 add-plant form. Profile is chosen from the catalog dropdown ([profiles]);
 *  container/garden-space/growth/last-watered remain id/text fields (richer select-or-create
 *  selectors land in a later step). Validates that a container is provided before calling
 *  [onSubmit]; otherwise shows a field-level error and does not submit. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    profiles: List<PlantProfile>,
    gardenSpaces: List<GardenSpace>,
    onCreateGardenSpace: (name: String, kind: String) -> Unit,
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
    var containerId by remember { mutableStateOf("") }
    var growthStage by remember { mutableStateOf("") }

    // Auto-select a freshly created space (VM appends it to gardenSpaces).
    LaunchedEffect(gardenSpaces) {
        if (selectedGardenSpace == null) gardenSpaces.lastOrNull()?.let { selectedGardenSpace = it }
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

            Field("Container id", containerId, InventoryTestTags.FIELD_CONTAINER_ID, isError = containerError) {
                containerId = it
                if (containerError && it.isNotBlank()) containerError = false
            }
            if (containerError) {
                Text(
                    text = "A container is required.",
                    modifier = Modifier.testTag(InventoryTestTags.CONTAINER_ERROR),
                )
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
                    if (containerId.isBlank()) {
                        containerError = true
                    } else {
                        onSubmit(
                            AddPlantForm(
                                profileId = (selectedProfile?.id ?: "").trim(),
                                containerId = containerId.trim(),
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
