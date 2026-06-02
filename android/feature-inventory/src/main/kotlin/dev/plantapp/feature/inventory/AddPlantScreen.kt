package dev.plantapp.feature.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/** Slice 1 add-plant form. Collects ids for profile/container/garden-space (richer
 *  selectors are a later slice). Validates that a container is provided before calling
 *  [onSubmit]; otherwise shows a field-level error and does not submit. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    onSubmit: (AddPlantForm) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
) {
    var profileId by remember { mutableStateOf("") }
    var containerId by remember { mutableStateOf("") }
    var gardenSpaceId by remember { mutableStateOf("") }
    var growthStage by remember { mutableStateOf("") }
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
            Field("Profile id", profileId, InventoryTestTags.FIELD_PROFILE_ID) { profileId = it }
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
            Field("Garden space id", gardenSpaceId, InventoryTestTags.FIELD_GARDEN_SPACE_ID) { gardenSpaceId = it }
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
                                profileId = profileId.trim(),
                                containerId = containerId.trim(),
                                gardenSpaceId = gardenSpaceId.trim(),
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
