package dev.plantapp.feature.inventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.plantapp.designsystem.GlassCard
import dev.plantapp.domain.model.Plant
import dev.plantapp.feature.inventory.addplant.WizardIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(
    state: PlantListUiState,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {},
    onPlantClick: (String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text("My plants", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.testTag(InventoryTestTags.ADD_PLANT_BUTTON),
            ) { Text("+") }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (state) {
                is PlantListUiState.Loading -> CircularProgressIndicator()
                is PlantListUiState.Empty ->
                    Text(
                        text = "No plants yet. Tap + to add your first plant.",
                        modifier = Modifier.testTag(InventoryTestTags.EMPTY_STATE),
                    )
                is PlantListUiState.SignedOut ->
                    Text(
                        text = "Signing you back in…",
                        modifier = Modifier.testTag("list_signed_out"),
                    )
                is PlantListUiState.Error ->
                    Text(text = "Couldn't load plants: ${state.message}")
                is PlantListUiState.Content ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag(InventoryTestTags.PLANT_LIST),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.plants, key = { it.id }) { plant ->
                            PlantRow(plant, state.speciesNames, onPlantClick)
                        }
                    }
            }
        }
    }
}

@Composable
private fun PlantRow(
    plant: Plant,
    speciesNames: Map<String, String>,
    onPlantClick: (String) -> Unit,
) {
    val speciesName = speciesNames[plant.profileId]
    val primary = plant.nickname ?: speciesName
        ?: DisplayText.speciesFallbackName(plant.profileId)
    GlassCard(
        onClick = { onPlantClick(plant.id) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag(InventoryTestTags.PLANT_ROW_PREFIX + plant.id),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 104.dp).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(WizardIcons.speciesIconRes(plant.profileId)),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = primary, style = MaterialTheme.typography.titleMedium)
                if (speciesName != null && speciesName != primary) {
                    Text(
                        text = speciesName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
