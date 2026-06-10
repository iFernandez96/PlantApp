package dev.plantapp.feature.inventory.addplant

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.plantapp.designsystem.GlassCard
import dev.plantapp.domain.model.Container
import dev.plantapp.domain.model.GardenSpace
import dev.plantapp.domain.model.PlantProfile
import dev.plantapp.feature.inventory.AddPlantForm
import dev.plantapp.feature.inventory.InventoryTestTags

/** Stable test-tag suffix for a pot tile (label → slug). */
fun potTileTagSuffix(label: String): String =
    label.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

private fun speciesName(profile: PlantProfile): String =
    profile.commonNames.firstOrNull() ?: profile.scientificName

/**
 * Beginner add-plant wizard: 3 plain-language steps (What are you growing? → Where will it live? →
 * What's it planted in?) + a confirm step. Big icon tiles, no jargon — litres/material/drainage/
 * growth-stage/ISO are never shown; the engine gets them from the friendly choices + hidden
 * defaults ([AddPlantWizardModel]). Stateless: data + callbacks are hoisted; step + selections are
 * internal. Created garden-space/container ids are resolved by identity from the re-supplied lists
 * (the VM appends asynchronously), and Add stays disabled until both resolve.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantWizard(
    profiles: List<PlantProfile>,
    gardenSpaces: List<GardenSpace>,
    containers: List<Container>,
    onCreateGardenSpace: (name: String, kind: String) -> Unit,
    onCreateContainer: (name: String?, volumeLiters: Double, material: String, drainage: String) -> Unit,
    onSubmit: (AddPlantForm) -> Unit,
    onCancel: () -> Unit = {},
    error: String? = null,
    modifier: Modifier = Modifier,
) {
    var step by remember { mutableStateOf(1) }
    var selectedProfile by remember { mutableStateOf<PlantProfile?>(null) }

    // Identity of the chosen location / pot (resolve ids from the lists, never assume "last").
    var targetSpaceName by remember { mutableStateOf<String?>(null) }
    var targetSpaceKind by remember { mutableStateOf<String?>(null) }
    var targetSpacePhrase by remember { mutableStateOf<String?>(null) }
    var targetContainerName by remember { mutableStateOf<String?>(null) }
    var selectedGardenSpaceId by remember { mutableStateOf<String?>(null) }
    var selectedContainerId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(gardenSpaces, targetSpaceName, targetSpaceKind) {
        selectedGardenSpaceId = gardenSpaces
            .firstOrNull { it.name == targetSpaceName && it.kind == targetSpaceKind }?.id
    }
    LaunchedEffect(containers, targetContainerName) {
        selectedContainerId = containers.firstOrNull { it.name == targetContainerName }?.id
    }

    val title = when (step) {
        1 -> "What are you growing?"
        2 -> "Where will it live?"
        3 -> "What's it planted in?"
        else -> "All set?"
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    TextButton(
                        onClick = { if (step > 1) step-- else onCancel() },
                        modifier = Modifier.testTag(InventoryTestTags.WIZARD_BACK_BUTTON),
                    ) {
                        Text("Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (error != null) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.WIZARD_ERROR),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Something didn't work. Please try again.",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            when (step) {
                1 -> profiles.forEach { profile ->
                    Tile(
                        label = speciesName(profile),
                        tag = InventoryTestTags.WIZARD_SPECIES_TILE_PREFIX + profile.id,
                        leadingIcon = {
                            Image(
                                painter = painterResource(WizardIcons.speciesIconRes(profile.id)),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                            )
                        },
                    ) {
                        selectedProfile = profile
                        step = 2
                    }
                }

                2 -> AddPlantWizardModel.LOCATION_PRESETS.forEach { preset ->
                    Tile(
                        label = preset.label,
                        tag = InventoryTestTags.WIZARD_LOCATION_TILE_PREFIX + preset.kind,
                        leadingIcon = { TileIcon(WizardIcons.locationIcon(preset.kind)) },
                    ) {
                        targetSpaceName = preset.label
                        targetSpaceKind = preset.kind
                        targetSpacePhrase = preset.phrase
                        // Reuse an existing matching space; only create when none matches.
                        if (gardenSpaces.none { it.name == preset.label && it.kind == preset.kind }) {
                            onCreateGardenSpace(preset.label, preset.kind)
                        }
                        step = 3
                    }
                }

                3 -> AddPlantWizardModel.POT_SIZES.forEach { option ->
                    Tile(
                        label = option.label,
                        tag = InventoryTestTags.WIZARD_POT_TILE_PREFIX + potTileTagSuffix(option.label),
                        leadingIcon = { TileIcon(WizardIcons.potIcon(option.label)) },
                    ) {
                        val name = "${selectedProfile?.let(::speciesName) ?: "Plant"} – ${option.label}"
                        targetContainerName = name
                        if (containers.none { it.name == name }) {
                            onCreateContainer(
                                name,
                                option.volumeLiters,
                                AddPlantWizardModel.DEFAULT_MATERIAL,
                                AddPlantWizardModel.DEFAULT_DRAINAGE,
                            )
                        }
                        step = 4
                    }
                }

                else -> {
                    val species = selectedProfile?.let(::speciesName) ?: "your plant"
                    val phrase = targetSpacePhrase ?: "to its new spot"
                    Text(
                        text = "Add your $species $phrase?",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    val ready = selectedProfile != null &&
                        selectedGardenSpaceId != null && selectedContainerId != null
                    Button(
                        onClick = {
                            val profile = selectedProfile ?: return@Button
                            val spaceId = selectedGardenSpaceId ?: return@Button
                            val containerId = selectedContainerId ?: return@Button
                            onSubmit(
                                AddPlantForm(
                                    profileId = profile.id,
                                    containerId = containerId,
                                    gardenSpaceId = spaceId,
                                    growthStage = AddPlantWizardModel.DEFAULT_GROWTH_STAGE,
                                    lastWateredAt = null,
                                ),
                            )
                        },
                        enabled = ready,
                        modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.WIZARD_ADD_BUTTON),
                    ) { Text("Add") }
                }
            }
        }
    }
}

@Composable
private fun TileIcon(icon: ImageVector) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(48.dp))
}

@Composable
private fun Tile(
    label: String,
    tag: String,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag(tag),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            leadingIcon()
            Text(text = label, style = MaterialTheme.typography.titleLarge)
        }
    }
}
