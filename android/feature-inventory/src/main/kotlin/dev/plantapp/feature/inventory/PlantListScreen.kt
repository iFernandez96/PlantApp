package dev.plantapp.feature.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.plantapp.domain.model.Plant

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
        topBar = { TopAppBar(title = { Text("My plants") }) },
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
                is PlantListUiState.Error ->
                    Text(text = "Couldn't load plants: ${state.message}")
                is PlantListUiState.Content ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag(InventoryTestTags.PLANT_LIST),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.plants, key = { it.id }) { plant -> PlantRow(plant, onPlantClick) }
                    }
            }
        }
    }
}

@Composable
private fun PlantRow(plant: Plant, onPlantClick: (String) -> Unit) {
    Text(
        text = plant.nickname ?: plant.profileId,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlantClick(plant.id) }
            .padding(16.dp),
    )
}
