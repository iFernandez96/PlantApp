package dev.plantapp.feature.inventory

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import dev.plantapp.domain.model.Advisory
import dev.plantapp.domain.model.CareTask
import dev.plantapp.domain.model.Plant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Slice 2: PlantDetailScreen surfaces backend advisories (informational, severity-styled). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h2000dp")
class PlantDetailAdvisoriesTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val plant = Plant(
        id = "00000000-0000-4000-8000-000000000001",
        profileId = "passiflora-edulis",
        containerId = "00000000-0000-4000-8000-000000000002",
        gardenSpaceId = "00000000-0000-4000-8000-000000000003",
        growthStage = "vegetative",
        nickname = "Pasi",
    )
    private val task = CareTask(
        id = "t",
        kind = "water",
        dueAt = "2026-05-28T07:00:00.000Z",
        priority = "normal",
        rationale = "r",
        engineVersion = "0.1.0",
        inputsHash = "a".repeat(64),
        status = "pending",
    )
    private val advisory = Advisory(
        kind = "container-size",
        severity = "high",
        plantInstanceId = plant.id,
        profileId = plant.profileId,
        title = "Container is smaller than recommended",
        message = "Passion fruit prefers at least 95 L (ideal 95-190 L); this container is 19 L.",
    )

    @Test
    fun showsAdvisoryTitleMessageAndSeverity() {
        composeRule.setContent {
            PlantDetailScreen(
                state = PlantDetailUiState.Content(plant = plant, task = task, advisories = listOf(advisory)),
            )
        }
        composeRule.onNodeWithTag(InventoryTestTags.ADVISORY_SECTION).assertIsDisplayed()
        composeRule.onNodeWithText(advisory.title, substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(advisory.message, substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("HIGH", substring = true, ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun noAdvisorySectionWhenEmpty() {
        composeRule.setContent {
            PlantDetailScreen(
                state = PlantDetailUiState.Content(plant = plant, task = task, advisories = emptyList()),
            )
        }
        composeRule.onNodeWithTag(InventoryTestTags.ADVISORY_SECTION).assertDoesNotExist()
    }
}
