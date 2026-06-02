package dev.plantapp.feature.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.plantapp.domain.model.CareTask
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    state: PlantDetailUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Plant detail") }) },
    ) { padding ->
        when (state) {
            is PlantDetailUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            is PlantDetailUiState.Error ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Couldn't load plant: ${state.message}")
                }
            is PlantDetailUiState.Content ->
                Column(
                    modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = state.plant.nickname ?: state.plant.profileId,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text("Growth stage: ${state.plant.growthStage}")
                    if (state.task != null) CareTaskCard(state.task) else Text("No care task yet.")
                }
        }
    }
}

@Composable
private fun CareTaskCard(task: CareTask) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Next: ${task.kind}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.testTag(InventoryTestTags.TASK_KIND),
        )
        Text(
            text = "Due ${formatDueAt(task.dueAt)}",
            modifier = Modifier.testTag(InventoryTestTags.TASK_DUE_AT),
        )
        Text(
            text = task.rationale,
            modifier = Modifier.testTag(InventoryTestTags.TASK_RATIONALE),
        )
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.testTag(InventoryTestTags.ENGINE_VERSION_BADGE),
        ) {
            Text(
                text = "engine v${task.engineVersion}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private val dueAtFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US).withZone(ZoneId.systemDefault())

private fun formatDueAt(iso: String): String =
    try {
        dueAtFormatter.format(Instant.parse(iso))
    } catch (_: Exception) {
        iso
    }
