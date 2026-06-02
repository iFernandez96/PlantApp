package dev.plantapp.feature.inventory

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.model.Plant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Compose UI tests for the list + detail screens, on the JVM via Robolectric (no emulator). The
 *  add-plant flow is now the AddPlantWizard (see AddPlantWizardTest); the old jargon form was
 *  removed in the beginner-UX overhaul. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h2000dp")
class InventoryScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val plant = Plant(
        id = "00000000-0000-4000-8000-000000000001",
        profileId = "solanum-lycopersicum",
        containerId = "00000000-0000-4000-8000-000000000002",
        gardenSpaceId = "00000000-0000-4000-8000-000000000003",
        growthStage = "vegetative",
        nickname = "Pasi",
        lastWateredAt = "2026-05-26T07:00:00.000Z",
    )
    private val task = CareTask(
        id = "00000000-0000-4000-8000-0000000000aa",
        kind = "water",
        dueAt = "2026-05-28T07:00:00.000Z",
        priority = "normal",
        rationale = "Tomato: base interval 2d adjusted by container factor 1; baseline ...",
        engineVersion = "0.1.0",
        inputsHash = "a".repeat(64),
        status = "pending",
    )

    @Test
    fun `#21 list shows empty state when there are no plants`() {
        composeRule.setContent { PlantListScreen(state = PlantListUiState.Empty) }
        composeRule.onNodeWithTag(InventoryTestTags.EMPTY_STATE).assertIsDisplayed()
    }

    @Test
    fun `#23 detail shows the water task with rationale, engineVersion badge, and dueAt`() {
        composeRule.setContent {
            PlantDetailScreen(state = PlantDetailUiState.Content(plant = plant, task = task))
        }
        composeRule.onNodeWithTag(InventoryTestTags.TASK_KIND).assertIsDisplayed()
        composeRule.onNodeWithText("water", substring = true, ignoreCase = true).assertIsDisplayed()
        composeRule.onNodeWithTag(InventoryTestTags.TASK_RATIONALE).assertIsDisplayed()
        composeRule.onNodeWithTag(InventoryTestTags.ENGINE_VERSION_BADGE).assertIsDisplayed()
        composeRule.onNodeWithText("0.1.0", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag(InventoryTestTags.TASK_DUE_AT).assertIsDisplayed()
    }
}
